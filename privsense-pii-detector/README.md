# PrivSense PII Detector Module

## Overview

The PII Detector module is responsible for identifying Personally Identifiable Information (PII) in database columns. It combines multiple detection strategies including heuristic analysis, regular expression pattern matching, and machine learning-based Named Entity Recognition (NER) to provide high-confidence PII detection with minimal false positives.

## Architecture

![PII Detector Architecture](../docs/images/pii-detector-module.png)

The module uses a composite strategy pattern architecture:

```
com.cgi.privsense.piidetector
├── heuristic      # Column name-based detection
├── regex          # Pattern-based detection
├── ner            # Machine learning NER detection
├── facade         # Orchestration and aggregation
└── config         # Configuration and constants
```

## Key Components

### Detection Strategies

The module implements three complementary detection approaches:

#### Heuristic-based Detection

- **`HeuristicPiiDetector`**: Analyzes column names and metadata
  - Based on common naming conventions for PII fields
  - Multi-language support for column names
  - Confidence scoring based on naming patterns
  - Schema relationship analysis
  
- **`ColumnPatternLoader`**: Loads patterns from configuration
  - JSON-based pattern configuration
  - Support for custom patterns
  - Internationalization for column names

#### Regex-based Detection

- **`PiiPatternLibrary`**: Collection of regex patterns for PII types
  - Comprehensive patterns for emails, phones, SSNs, etc.
  - International format support (e.g., phone numbers)
  - Validation logic beyond simple pattern matching
  - Confidence scoring for regex matches
  
- **`RegexPiiDetector`**: Sample data analysis using regex patterns
  - Efficient pattern matching algorithms
  - Match percentage calculation
  - False positive reduction techniques
  - Cross-validation of matches

#### NER-based Detection

- **`NerServiceClient`**: Client for Python-based NER service
  - REST API communication
  - Error handling and retries
  - Circuit breaker pattern
  - Response parsing and mapping
  
- **`NerModelPiiDetector`**: Machine learning detection using NER
  - Sample preparation for NER analysis
  - Entity analysis and classification
  - Confidence score calculation
  - Fallback mechanisms when service unavailable

### Orchestration

- **`PiiDetectionFacade`**: Orchestrates multiple detection strategies
  - Parallel execution of detectors
  - Result aggregation and ranking
  - Weighted confidence calculation
  - Detailed detection reports

### Configuration

- **`PiiDetectionConfig`**: Configuration for detection processes
  - Strategy enabling/disabling
  - Confidence thresholds
  - Timeout settings
  - Cache configuration

## Features

### Multi-Strategy Detection

The module combines three complementary approaches:
- **Heuristic Detection**: Fast column name analysis
- **Regex Detection**: Pattern-based sample data analysis
- **NER Detection**: Advanced machine learning detection

### Comprehensive PII Type Coverage

Detection for a wide range of PII types:
- **Personal Identifiers**: Names, emails, phone numbers
- **Financial Information**: Credit card numbers, bank accounts
- **Government IDs**: SSN, passport numbers, driver's licenses
- **Location Data**: Addresses, coordinates, postal codes
- **Demographic Data**: Age, gender, date of birth
- **Online Identifiers**: IP addresses, device IDs, cookies

### Confidence Scoring

Each detection includes confidence scoring:
- Percentage-based confidence (0-100%)
- Strategy-specific scoring algorithms
- Aggregate confidence across strategies
- Confidence thresholds for reporting

### Performance Optimizations

- **Parallel Detection**: Strategies execute concurrently
- **Early Termination**: High-confidence matches can skip further processing
- **Caching**: Previously analyzed columns are cached
- **Progressive Analysis**: Fastest strategies run first

### International Support

- Multi-language column name patterns
- International format detection (phone numbers, postal codes)
- Region-specific PII types (EU, US, Asia)

## Dependencies

```xml
<dependencies>
    <!-- Common module -->
    <dependency>
        <groupId>com.cgi</groupId>
        <artifactId>privsense-common</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Spring Web for REST client -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-web</artifactId>
    </dependency>
    
    <!-- Jackson for JSON processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- Regular expression library -->
    <dependency>
        <groupId>com.googlecode.libphonenumber</groupId>
        <artifactId>libphonenumber</artifactId>
        <version>8.13.0</version>
    </dependency>
    
    <!-- Resilience4j for circuit breaking -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-circuitbreaker</artifactId>
        <version>1.7.0</version>
    </dependency>
</dependencies>
```

## Configuration

The module accepts configuration properties:

```properties
# General PII Detection Configuration
privsense.pii.enabled-strategies=HEURISTIC,REGEX,NER_MODEL
privsense.pii.confidence-threshold=0.7
privsense.pii.min-sample-size=20
privsense.pii.max-sample-size=1000

# Heuristic Detection Configuration
privsense.pii.heuristic.enabled=true
privsense.pii.heuristic.patterns-file=classpath:column-patterns.json
privsense.pii.heuristic.min-confidence=0.6

# Regex Detection Configuration
privsense.pii.regex.enabled=true
privsense.pii.regex.match-percent=0.5
privsense.pii.regex.validation-enabled=true

# NER Service Configuration
privsense.pii.ner.enabled=true
privsense.pii.ner.service-url=http://localhost:5000/ner/analyze
privsense.pii.ner.timeout-ms=5000
privsense.pii.ner.retry-attempts=3
privsense.pii.ner.circuit-breaker.failure-threshold=5
privsense.pii.ner.circuit-breaker.wait-duration-ms=30000
```

## Column Patterns File

Here's a sample of the `column-patterns.json` configuration file:

```json
{
  "patterns": {
    "EMAIL": {
      "names": ["email", "email_address", "mail", "e_mail", "courriel"],
      "confidence": 0.9
    },
    "NAME": {
      "names": ["name", "full_name", "first_name", "last_name", "given_name", "surname", "nom", "prenom"],
      "confidence": 0.7
    },
    "PHONE": {
      "names": ["phone", "phone_number", "telephone", "mobile", "cell", "tel", "telefono"],
      "confidence": 0.8
    },
    "SSN": {
      "names": ["ssn", "social_security", "social_security_number", "tax_id", "ssnum"],
      "confidence": 0.95
    },
    "CREDIT_CARD": {
      "names": ["cc", "credit_card", "card_number", "payment_card", "cc_number"],
      "confidence": 0.9
    },
    "ADDRESS": {
      "names": ["address", "street_address", "mailing_address", "adresse", "direccion"],
      "confidence": 0.8
    }
  }
}
```

## Usage Examples

### Basic PII Detection

```java
public class PiiDetectionExample {
    
    private final PiiDetectionFacade piiDetector;
    
    // Constructor injection
    public PiiDetectionExample(PiiDetectionFacade piiDetector) {
        this.piiDetector = piiDetector;
    }
    
    public PiiDetectionResult detectPiiInColumn(TableMetadata table, 
                                               String columnName, 
                                               SampleResult sampleResult) {
        // Find column metadata
        ColumnMetadata column = table.getColumns().stream()
            .filter(col -> col.getName().equalsIgnoreCase(columnName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Column not found: " + columnName));
        
        // Perform PII detection
        return piiDetector.detectPii(column, sampleResult);
    }
}
```

### Custom Detection Configuration

```java
public PiiDetectionResult detectWithCustomConfig(ColumnMetadata column, 
                                              SampleResult sampleResult) {
    // Create custom detection configuration
    PiiDetectionConfig config = new PiiDetectionConfig.Builder()
        .enableStrategy(PiiDetectionStrategy.HEURISTIC)
        .enableStrategy(PiiDetectionStrategy.REGEX)
        .disableStrategy(PiiDetectionStrategy.NER_MODEL)  // Disable NER
        .withConfidenceThreshold(0.8)  // Higher threshold
        .withValidation(true)
        .build();
    
    // Perform detection with custom config
    return piiDetector.detectPiiWithConfig(column, sampleResult, config);
}
```

### Analyzing Results

```java
public void analyzePiiResults(PiiDetectionResult result) {
    System.out.println("Column: " + result.getColumnName());
    System.out.println("Contains PII: " + result.isContainsPii());
    System.out.println("Confidence: " + result.getConfidenceScore());
    
    if (result.isContainsPii()) {
        System.out.println("PII Type: " + result.getPiiType());
        System.out.println("Detection Strategy: " + result.getDetectionStrategy());
        
        // Get strategy-specific details
        Map<PiiDetectionStrategy, Double> confidenceByStrategy = result.getConfidenceByStrategy();
        confidenceByStrategy.forEach((strategy, confidence) -> {
            System.out.println(strategy + ": " + confidence);
        });
    }
}
```

### Batch PII Scanning

```java
public List<PiiDetectionResult> scanTableForPii(TableMetadata table, 
                                               Map<String, SampleResult> samples) {
    List<CompletableFuture<PiiDetectionResult>> futures = new ArrayList<>();
    
    // Start parallel detection for each column with samples
    for (ColumnMetadata column : table.getColumns()) {
        SampleResult sample = samples.get(column.getName());
        if (sample != null) {
            CompletableFuture<PiiDetectionResult> future = CompletableFuture
                .supplyAsync(() -> piiDetector.detectPii(column, sample));
            futures.add(future);
        }
    }
    
    // Wait for all detection to complete and collect results
    return futures.stream()
        .map(CompletableFuture::join)
        .collect(Collectors.toList());
}
```

## Python NER Service Integration

The module integrates with a Python-based NER service for advanced entity recognition:

### NER Service Request/Response Format

```json
// Request
{
  "samples": [
    "John Smith",
    "jane.doe@example.com",
    "123 Main St, New York",
    "555-123-4567"
  ],
  "columnName": "contact_info",
  "dataType": "VARCHAR"
}

// Response
{
  "entities": [
    {
      "type": "PERSON",
      "count": 1,
      "confidence": 0.92,
      "mappedPiiType": "NAME"
    },
    {
      "type": "EMAIL",
      "count": 1,
      "confidence": 0.99,
      "mappedPiiType": "EMAIL"
    },
    {
      "type": "ADDRESS",
      "count": 1,
      "confidence": 0.87,
      "mappedPiiType": "ADDRESS"
    },
    {
      "type": "PHONE",
      "count": 1,
      "confidence": 0.95,
      "mappedPiiType": "PHONE"
    }
  ],
  "dominantEntity": {
    "type": "EMAIL",
    "confidence": 0.99,
    "mappedPiiType": "EMAIL"
  }
}
```

## Testing

### Unit Tests

```java
@Test
void testRegexPiiDetection() {
    // Setup
    RegexPiiDetector detector = new RegexPiiDetector();
    
    ColumnMetadata column = ColumnMetadata.builder()
        .name("email")
        .dataType("VARCHAR")
        .build();
    
    List<String> samples = List.of(
        "john.doe@example.com",
        "jane.smith@company.org",
        "bob-123@mail.co.uk"
    );
    
    SampleResult sampleResult = SampleResult.builder()
        .tableName("users")
        .columnName("email")
        .samples(samples)
        .build();
    
    // Execute
    PiiDetectionResult result = detector.detectPii(column, sampleResult);
    
    // Verify
    assertTrue(result.isContainsPii());
    assertEquals(PiiType.EMAIL, result.getPiiType());
    assertEquals(PiiDetectionStrategy.REGEX, result.getDetectionStrategy());
    assertTrue(result.getConfidenceScore() > 0.9);
}
```

### Integration Tests

```java
@SpringBootTest
class NerPiiDetectorIntegrationTest {
    @MockBean
    private RestTemplate restTemplate;
    
    @Autowired
    private NerModelPiiDetector detector;
    
    @Test
    void testNerServiceIntegration() {
        // Setup mock response
        NerResponse mockResponse = new NerResponse();
        // Set up mock response entities
        
        when(restTemplate.postForObject(
            anyString(),
            any(NerRequest.class),
            eq(NerResponse.class)
        )).thenReturn(mockResponse);
        
        // Create test data
        ColumnMetadata column = ColumnMetadata.builder()
            .name("customer_name")
            .dataType("VARCHAR")
            .build();
        
        List<String> samples = List.of(
            "John Smith",
            "Jane Doe",
            "Robert Johnson"
        );
        
        SampleResult sampleResult = SampleResult.builder()
            .tableName("customers")
            .columnName("customer_name")
            .samples(samples)
            .build();
        
        // Execute
        PiiDetectionResult result = detector.detectPii(column, sampleResult);
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isContainsPii());
        assertEquals(PiiType.NAME, result.getPiiType());
    }
}
```

## Best Practices

The module follows these best practices:

1. **Defense in Depth**: Multiple detection strategies for higher accuracy
2. **Fail Safe**: Graceful degradation when components are unavailable
3. **Performance Optimization**: Parallel processing and early termination
4. **Internationalization**: Support for multiple languages and formats
5. **Configurability**: Extensive configuration options for different scenarios
6. **Circuit Breaking**: Protection from dependent service failures
7. **Comprehensive Testing**: Both unit and integration tests
8. **Clear Result Reporting**: Detailed results with confidence metrics
