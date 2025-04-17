package com.cgi.privsense.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for data sampling requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleRequestDto {

    /**
     * Connection request parameters
     */
    @NotNull(message = "Connection request must not be null")
    private ConnectionRequestDto connectionRequest;

    /**
     * Name of the table to sample data from
     */
    @NotBlank(message = "Table name must not be blank")
    private String tableName;

    /**
     * Name of the column to sample data from
     */
    @NotBlank(message = "Column name must not be blank")
    private String columnName;

    /**
     * Number of samples to retrieve
     */
    @Min(value = 1, message = "Sample size must be at least 1")
    @Builder.Default
    private int sampleSize = 100;

    /**
     * Sampling strategy to use (e.g., RANDOM, STRATIFIED, etc.)
     */
    @Builder.Default
    private String samplingStrategy = "RANDOM";
}
