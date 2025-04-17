package com.cgi.privsense.piidetector.ner;

import com.cgi.privsense.common.exception.PiiDetectionException;
import com.cgi.privsense.common.model.PiiType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Client for communicating with the external Named Entity Recognition (NER) service.
 * Uses WebClient with circuit breaker pattern for resilience.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NerServiceClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${privsense.ner.service.url:http://localhost:5000}")
    private String nerServiceUrl;
    
    @Value("${privsense.ner.service.endpoint:/ner/analyze}")
    private String nerServiceEndpoint;
    
    @Value("${privsense.ner.service.timeout:5}")
    private int timeoutSeconds;
    
    @Value("${privsense.ner.service.retry.attempts:3}")
    private int retryAttempts;
    
    /**
     * Analyzes a list of text samples for named entities that may contain PII.
     * Uses circuit breaker pattern for resilience against service failures.
     *
     * @param samples list of text samples to analyze
     * @return NER analysis result with entity types and confidence scores
     * @throws PiiDetectionException if analysis fails
     */
    @CircuitBreaker(name = "nerService", fallbackMethod = "getAnalysisFallback")
    public NerAnalysisResult analyzeTextSamples(List<String> samples) {
        try {
            log.debug("Sending {} samples to NER service for analysis", samples.size());
            
            NerServiceRequest request = new NerServiceRequest(samples);
            String requestJson = objectMapper.writeValueAsString(request);
            
            return webClient
                    .post()
                    .uri(nerServiceUrl + nerServiceEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(NerServiceResponse.class)
                    .map(this::convertToAnalysisResult)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofMillis(500))
                            .filter(this::isRetryableException)
                            .doBeforeRetry(signal -> 
                                log.warn("Retry attempt {} after error: {}", 
                                    signal.totalRetries() + 1, 
                                    signal.failure().getMessage())))
                    .onErrorMap(ex -> {
                        log.error("Error calling NER service: {}", ex.getMessage());
                        return new PiiDetectionException("Failed to analyze text with NER service", ex);
                    })
                    .block();
            
        } catch (JsonProcessingException e) {
            log.error("Error serializing request to NER service", e);
            throw new PiiDetectionException("Failed to prepare NER service request", e);
        } catch (Exception e) {
            log.error("Unexpected error during NER analysis", e);
            throw new PiiDetectionException("Unexpected error during NER analysis", e);
        }
    }
    
    /**
     * Fallback method when the NER service is unavailable.
     * Returns an empty result indicating the service is down.
     *
     * @param samples the text samples that were being analyzed
     * @param ex the exception that triggered the fallback
     * @return empty analysis result with service unavailability indicated
     */
    private NerAnalysisResult getAnalysisFallback(List<String> samples, Exception ex) {
        log.warn("Using fallback for NER service. Service may be unavailable: {}", ex.getMessage());
        NerAnalysisResult fallbackResult = new NerAnalysisResult();
        fallbackResult.setServiceAvailable(false);
        fallbackResult.setEntitiesByType(Collections.emptyMap());
        fallbackResult.setOverallConfidence(0.0);
        return fallbackResult;
    }
    
    /**
     * Determines if an exception is retryable.
     * Connection errors and certain HTTP errors are retryable.
     *
     * @param throwable the exception to check
     * @return true if the operation should be retried
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException wcre = (WebClientResponseException) throwable;
            int statusCode = wcre.getStatusCode().value();
            // Retry for server errors and certain client errors
            return statusCode >= 500 || statusCode == 429;
        }
        // Retry for connection errors
        return true;
    }
    
    /**
     * Converts the raw service response to an analysis result object.
     *
     * @param response the raw response from the NER service
     * @return structured analysis result
     */
    private NerAnalysisResult convertToAnalysisResult(NerServiceResponse response) {
        NerAnalysisResult result = new NerAnalysisResult();
        result.setServiceAvailable(true);
        result.setOverallConfidence(response.getOverallConfidence());
        
        // Convert NER entity types to PII types and group entities
        Map<PiiType, List<NerEntity>> entitiesByType = response.getEntities().stream()
                .collect(Collectors.groupingBy(entity -> mapEntityTypeToPiiType(entity.getType())));
        
        result.setEntitiesByType(entitiesByType);
        return result;
    }
    
    /**
     * Maps standard NER entity types to PII types.
     * 
     * @param nerEntityType the entity type from NER service
     * @return corresponding PII type
     */
    private PiiType mapEntityTypeToPiiType(String nerEntityType) {
        if (nerEntityType == null) {
            return PiiType.UNKNOWN;
        }
        
        switch (nerEntityType.toUpperCase()) {
            case "PERSON":
                return PiiType.NAME;
            case "EMAIL":
                return PiiType.EMAIL;
            case "PHONE":
            case "PHONE_NUMBER":
                return PiiType.PHONE;
            case "ADDRESS":
            case "LOCATION":
            case "GPE": // Geo-political entity
                return PiiType.ADDRESS;
            case "SSN":
            case "SOCIAL_SECURITY_NUMBER":
                return PiiType.SSN;
            case "CREDIT_CARD":
            case "CREDIT_CARD_NUMBER":
                return PiiType.CREDIT_CARD;
            case "DATE":
            case "DATE_OF_BIRTH":
            case "DOB":
                return PiiType.DATE_OF_BIRTH;
            case "IP_ADDRESS":
                return PiiType.IP_ADDRESS;
            case "DL":
            case "DRIVERS_LICENSE":
            case "DRIVER_LICENSE":
                return PiiType.DRIVERS_LICENSE;
            case "PASSPORT":
                return PiiType.PASSPORT;
            case "ACCOUNT_NUMBER":
            case "BANK_ACCOUNT":
                return PiiType.FINANCIAL_ACCOUNT;
            case "MEDICAL_RECORD":
            case "PATIENT_ID":
                return PiiType.MEDICAL_RECORD;
            case "ID":
            case "ID_NUMBER":
                return PiiType.ID_NUMBER;
            default:
                return PiiType.OTHER;
        }
    }
    
    /**
     * Request object for NER service.
     */
    @Data
    static class NerServiceRequest {
        private final List<String> texts;
    }
    
    /**
     * Response object from NER service.
     */
    @Data
    static class NerServiceResponse {
        private List<NerEntity> entities;
        private double overallConfidence;
    }
    
    /**
     * Entity identified by the NER service.
     */
    @Data
    public static class NerEntity {
        private String text;
        private String type;
        private double confidence;
        private int startIndex;
        private int endIndex;
    }
    
    /**
     * Result of NER analysis with grouped entities by PII type.
     */
    @Data
    public static class NerAnalysisResult {
        private boolean serviceAvailable;
        private Map<PiiType, List<NerEntity>> entitiesByType;
        private double overallConfidence;
    }
}
