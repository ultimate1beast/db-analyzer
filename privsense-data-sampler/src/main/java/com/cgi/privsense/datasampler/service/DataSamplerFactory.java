package com.cgi.privsense.datasampler.service;

import com.cgi.privsense.common.model.ColumnMetadata;
import com.cgi.privsense.common.service.DataSampler;
import com.cgi.privsense.datasampler.strategy.RandomSampler;
import com.cgi.privsense.datasampler.strategy.StratifiedSampler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

/**
 * Factory for creating data sampler instances based on sampling strategy.
 * Uses the Factory pattern to provide different sampling implementations.
 */
@Slf4j
@Component
public class DataSamplerFactory extends SamplerFactory {

    /**
     * Enumeration of supported sampling strategies.
     */
    public enum SamplingStrategy {
        RANDOM,
        STRATIFIED,
        // Add more strategies as they're implemented
    }    private final DataSource dataSource;
    
    /**
     * Constructor for DataSamplerFactory.
     *
     * @param dataSource the data source to use for samplers
     */
    public DataSamplerFactory(DataSource dataSource) {
        super(new RandomSampler(dataSource), new StratifiedSampler(dataSource), null);
        this.dataSource = dataSource;
    }

    /**
     * Creates a data sampler with the specified strategy.
     *
     * @param strategy the sampling strategy to use
     * @return a DataSampler implementation
     */
    public DataSampler createSampler(SamplingStrategy strategy) {
        log.debug("Creating sampler with strategy: {}", strategy);
        switch (strategy) {
            case RANDOM:
                return new RandomSampler(dataSource);
            case STRATIFIED:
                return new StratifiedSampler(dataSource);
            default:
                log.warn("Unknown sampling strategy: {}, using default Random sampler", strategy);
                return new RandomSampler(dataSource);
        }
    }

    /**
     * Creates a data sampler with the default strategy (RANDOM).
     *
     * @return a default DataSampler implementation
     */
    public DataSampler createDefaultSampler() {
        log.debug("Creating default sampler (RANDOM)");
        return createSampler(SamplingStrategy.RANDOM);
    }

    /**
     * Creates a stratified sampler with a custom number of strata.
     *
     * @param numStrata the number of strata to divide the data into
     * @return a StratifiedSampler instance
     */
    public DataSampler createStratifiedSampler(int numStrata) {
        log.debug("Creating stratified sampler with {} strata", numStrata);
        return new StratifiedSampler(dataSource, numStrata);
    }
}
