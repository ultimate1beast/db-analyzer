# PrivSense Data Sampler Module

## Overview

The Data Sampler module is responsible for intelligently extracting representative data samples from database tables. It provides multiple sampling strategies optimized for different database sizes and data distributions, ensuring efficient and accurate sampling while minimizing database load.

## Architecture

![Data Sampler Architecture](../docs/images/data-sampler-module.png)

The module follows a strategy pattern architecture:

```
com.cgi.privsense.datasampler
├── base           # Abstract sampler template and common functionality
├── strategy       # Different sampling strategy implementations
├── service        # High-level services and facade
└── util           # Utility classes for sampling operations
```

## Key Components

### Base Components

- **`AbstractDataSampler`**: Template class for all sampling strategies
  - Implements template method pattern
  - Handles connection management
  - Provides hooks for specific sampling algorithms
  - Implements common statistics calculation

### Sampling Strategies

The module offers several sampling strategies for different use cases:

- **`RandomSampler`**: Basic random sampling implementation
  - Database-agnostic using ORDER BY RAND()
  - Limit/offset pagination for large tables
  - Configurable sample size
  - Resource-efficient for small to medium tables

- **`StratifiedSampler`**: Distribution-aware sampling
  - Analyzes data distribution before sampling
  - Ensures representative samples across value ranges
  - Maintains distribution proportions in samples
  - Optimized for skewed data distributions

- **`ParallelSampler`**: High-performance sampling for large tables
  - Divides work into parallel chunks
  - Configurable thread pool
  - Implements work stealing for load balancing
  - Result aggregation with statistics merging
  - Optimized for very large tables

### Service Layer

- **`SamplerFactory`**: Creates appropriate sampling strategies
  - Factory pattern implementation
  - Strategy selection based on table size and configuration
  - Configuration handling

- **`DataSamplingService`**: High-level service facade
  - Simplified API for sampling operations
  - Caching of sample results
  - Combined sampling and statistics generation
  - Performance optimization

### Utility Classes

- **`DataTypeUtils`**: Utilities for handling different data types
  - Type conversion between database and Java types
  - NULL value handling
  - Special type handling (LOB, XML, JSON)

- **`StatisticsCalculator`**: Statistical analysis of sample data
  - Distinct value calculation
  - NULL value analysis
  - Min/max determination
  - Value distribution calculation
  - Cardinality estimation

- **`QueryBuilder`**: Database-specific query generation
  - Sampling query generation
  - Database dialect handling
  - Query optimization

- **`SampleSizeCalculator`**: Optimal sample size determination
  - Table size-based calculation
  - Confidence interval consideration
  - Memory-aware sizing

## Features

### Multiple Sampling Strategies

The module offers several strategies optimized for different scenarios:
- **Random Sampling**: Basic sampling suitable for most tables
- **Stratified Sampling**: Ensures representative samples across value distributions
- **Parallel Sampling**: High-performance sampling for very large tables

### Statistical Analysis

Each sampling operation includes comprehensive statistical analysis:
- Distinct value counts and percentages
- NULL value analysis
- Min/max values for numeric fields
- Value distribution metrics
- Cardinality estimation

### Database Optimization

The module implements database-specific optimizations:
- **MySQL**: Efficient `ORDER BY RAND()`
- **Oracle**: Optimized sampling using `SAMPLE` clause
- **SQL Server**: Optimized `TABLESAMPLE` system function
- **PostgreSQL**: Efficient `TABLESAMPLE` implementation

### Performance Features

- **Parallel Processing**: Multi-threaded sampling for large tables
- **Result Caching**: Sample results are cached to prevent redundant operations
- **Adaptive Sample Sizing**: Sample size adjusted based on table size
- **Resource Management**: Controlled resource usage during sampling

## Dependencies

```xml
<dependencies>
    <!-- Common module -->
    <dependency>
        <groupId>com.cgi</groupId>
        <artifactId>privsense-common</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Database Scanner module (for connections) -->
    <dependency>
        <groupId>com.cgi</groupId>
        <artifactId>privsense-db-scanner</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Apache Commons Math for statistical calculations -->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-math3</artifactId>
        <version>3.6.1</version>
    </dependency>
    
    <!-- Spring Framework for caching and configuration -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
    </dependency>
    
    <!-- Caffeine for caching -->
    <dependency>
        <groupId>com.github.ben-manes.caffeine</groupId>
        <artifactId>caffeine</artifactId>
    </dependency>
</dependencies>
```

## Configuration

The module accepts various configuration properties:

```properties
# Default Sampling Configuration
privsense.sampler.default-sample-size=1000
privsense.sampler.max-sample-size=10000
privsense.sampler.default-strategy=RANDOM

# Parallel Sampling Configuration
privsense.sampler.parallel.core-threads=4
privsense.sampler.parallel.max-threads=8
privsense.sampler.parallel.queue-size=1000
privsense.sampler.parallel.chunk-size=250

# Stratified Sampling Configuration
privsense.sampler.stratified.min-value-groups=5
privsense.sampler.stratified.max-value-groups=20
privsense.sampler.stratified.threshold-percentage=0.05

# Cache Configuration
privsense.sampler.cache.enabled=true
privsense.sampler.cache.max-size=100
privsense.sampler.cache.expire-after-write=30m
```

## Usage Examples

### Basic Random Sampling

```java
public class SamplingExample {
    
    private final DataSamplingService samplingService;
    
    // Constructor injection
    public SamplingExample(DataSamplingService samplingService) {
        this.samplingService = samplingService;
    }
    
    public SampleResult sampleUserEmails(Connection connection, TableMetadata tableMetadata) {
        // Find email column
        ColumnMetadata emailColumn = tableMetadata.getColumns().stream()
            .filter(col -> col.getName().equalsIgnoreCase("email"))
            .findFirst()
            .orElseThrow(() -> new SamplingException("Email column not found"));
        
        // Sample with default settings
        return samplingService.sampleColumn(connection, "users", emailColumn, 1000);
    }
}
```

### Using Specific Sampling Strategy

```java
public SampleResult sampleLargeTable(Connection connection, String tableName, 
                                     ColumnMetadata column) {
    // Create sampling configuration
    SamplingConfig config = new SamplingConfig.Builder()
        .withStrategy(SamplingStrategy.PARALLEL)
        .withSampleSize(5000)
        .withThreadCount(8)
        .withTimeout(Duration.ofMinutes(5))
        .build();
    
    // Get factory and create specific sampler
    SamplerFactory factory = new SamplerFactory();
    DataSampler sampler = factory.createSampler(config);
    
    // Execute sampling
    return sampler.sampleColumn(connection, tableName, column, config.getSampleSize());
}
```

### Analyzing Sample Results

```java
public void analyzeSampleResults(SampleResult result) {
    System.out.println("Column: " + result.getColumnName());
    System.out.println("Samples collected: " + result.getSamples().size());
    System.out.println("Execution time: " + result.getExecutionTimeMs() + "ms");
    
    // Get statistics
    SampleStatistics stats = result.getStatistics();
    System.out.println("Distinct values: " + stats.getDistinctValueCount());
    System.out.println("Null percentage: " + stats.getNullPercentage() + "%");
    
    if (stats.getMinValue() != null && stats.getMaxValue() != null) {
        System.out.println("Range: " + stats.getMinValue() + " to " + stats.getMaxValue());
    }
    
    // Print first 10 samples
    result.getSamples().stream().limit(10).forEach(System.out::println);
}
```

## Testing

The module includes comprehensive tests:

### Unit Tests

```java
@Test
void testRandomSamplerWithSmallTable() {
    // Setup
    Connection mockConnection = mock(Connection.class);
    Statement mockStatement = mock(Statement.class);
    ResultSet mockResultSet = mock(ResultSet.class);
    
    // Mock behavior
    when(mockConnection.createStatement()).thenReturn(mockStatement);
    when(mockStatement.executeQuery(anyString())).thenReturn(mockResultSet);
    // Setup mock ResultSet to return sample data
    
    // Create sampler
    RandomSampler sampler = new RandomSampler();
    
    // Execute
    ColumnMetadata column = ColumnMetadata.builder()
        .name("email")
        .dataType("VARCHAR")
        .build();
        
    SampleResult result = sampler.sampleColumn(mockConnection, "users", column, 100);
    
    // Verify
    assertNotNull(result);
    assertEquals("users", result.getTableName());
    assertEquals("email", result.getColumnName());
    assertFalse(result.getSamples().isEmpty());
}
```

### Integration Tests

```java
@Testcontainers
class SamplingIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-mysql.sql");
            
    private Connection connection;
    private DataSampler sampler;
    
    @BeforeEach
    void setup() throws SQLException {
        connection = DriverManager.getConnection(
            mysql.getJdbcUrl(), 
            mysql.getUsername(), 
            mysql.getPassword()
        );
        
        sampler = new RandomSampler();
    }
    
    @AfterEach
    void cleanup() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
    
    @Test
    void testRealDatabaseSampling() throws SQLException {
        // Create column metadata
        ColumnMetadata nameColumn = ColumnMetadata.builder()
            .name("name")
            .dataType("VARCHAR")
            .build();
            
        // Sample the column
        SampleResult result = sampler.sampleColumn(connection, "users", nameColumn, 50);
        
        // Verify results
        assertNotNull(result);
        assertFalse(result.getSamples().isEmpty());
        assertTrue(result.getSamples().size() <= 50);
        assertNotNull(result.getStatistics());
    }
}
```

## Best Practices

The module follows these best practices:

1. **Strategy Pattern**: Clean separation of sampling algorithms
2. **Template Method Pattern**: Common code reuse with specialization
3. **Resource Efficiency**: Careful management of database connections and memory
4. **Adaptive Behavior**: Different strategies based on table size and characteristics
5. **Comprehensive Testing**: Both unit and integration tests
6. **Performance Optimization**: Specific optimizations for different database systems
