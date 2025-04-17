# PrivSense Common Module

## Overview

The PrivSense Common module serves as the foundation for the entire PrivSense system, providing shared models, interfaces, and utilities used by all other modules. It defines the core domain objects, service contracts, and common functionality, ensuring consistency across the application.

## Architecture

![Common Module Architecture](../docs/images/common-module.png)

This module follows a clean architecture pattern with clear separation of model, interface, and utility components:

```
com.cgi.privsense.common
├── model          # Domain model classes 
├── database       # Database connectivity interfaces
├── service        # Core service interfaces
├── exception      # Custom exception hierarchy
└── util           # Shared utility classes
```

## Key Components

### Domain Models

The module provides comprehensive domain models representing database metadata and analysis results:

#### Database Metadata Models

- **`TableMetadata`**: Represents database table structure including:
  - Name and schema
  - Row count and size statistics
  - Column metadata collection
  - Primary and foreign key information

- **`ColumnMetadata`**: Represents column structure including:
  - Name and data type
  - Position and constraints
  - Size specifications
  - Primary/foreign key flags

- **`ForeignKeyMetadata`**: Tracks relationships between tables:
  - Constraint name
  - Source column information
  - Referenced table and column

#### Analysis Models

- **`SampleResult`**: Contains data sampling results:
  - Table and column references
  - Sample data collection
  - Statistics derived from samples
  - Execution metrics

- **`SampleStatistics`**: Holds statistical analysis of column data:
  - Distinct value counts
  - Null value analysis
  - Min/max values
  - Value distribution metrics

#### PII Detection Models

- **`PiiDetectionResult`**: Encapsulates PII detection findings:
  - Table and column identifiers
  - PII presence status
  - Confidence score
  - Detected PII type
  - Detection strategy used

- **`PiiType` Enum**: Defines supported PII types:
  - NAME, EMAIL, PHONE
  - ADDRESS, SSN, CREDIT_CARD
  - DOB, IP_ADDRESS, etc.

- **`PiiDetectionStrategy` Enum**: Defines supported detection methods:
  - HEURISTIC - based on column names
  - REGEX - based on pattern matching
  - NER_MODEL - Machine learning-based detection

### Core Service Interfaces

The module defines contract interfaces implemented by other modules:

- **`DatabaseConnectionProvider`**: Interface for database connectivity:
  - Connection acquisition
  - Connection lifecycle management
  - Database type identification

- **`MetadataExtractor`**: Interface for schema extraction:
  - Methods to extract table and column metadata
  - Schema filtering capabilities
  - Metadata transformation utilities

- **`DataSampler`**: Interface for data sampling:
  - Random and stratified sampling
  - Sample size determination
  - Statistics generation

- **`PiiDetector`**: Interface for PII detection:
  - Detection method implementation
  - Confidence scoring
  - Result aggregation

### Database Connectivity

- **`DatabaseType` Enum**: Supported database systems:
  - MYSQL, ORACLE, SQL_SERVER, POSTGRESQL, OTHER

### Exception Hierarchy

The module provides a comprehensive exception hierarchy:

- **`PrivSenseException`**: Base exception for all application-specific errors
  - **`DatabaseConnectionException`**: Database connectivity issues
  - **`MetadataExtractionException`**: Schema extraction problems
  - **`SamplingException`**: Data sampling failures
  - **`PiiDetectionException`**: PII detection errors

### Utility Classes

- **`ValidationUtils`**: Parameter validation utilities
- **`DatabaseUtils`**: Common database operations
- **`StringUtils`**: String manipulation for database objects

## Dependencies

The module has minimal external dependencies to ensure portability:

```xml
<dependencies>
    <!-- Lombok for reducing boilerplate code -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
    
    <!-- SLF4J for logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
    
    <!-- JUnit for testing -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito for mocking in tests -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Usage Examples

### Working with TableMetadata

```java
// Create a column metadata instance
ColumnMetadata idColumn = ColumnMetadata.builder()
    .name("id")
    .dataType("INT")
    .ordinalPosition(1)
    .nullable(false)
    .isPrimaryKey(true)
    .build();

ColumnMetadata nameColumn = ColumnMetadata.builder()
    .name("full_name")
    .dataType("VARCHAR")
    .ordinalPosition(2)
    .characterMaxLength(255)
    .nullable(true)
    .isPrimaryKey(false)
    .build();

// Create a table metadata instance
TableMetadata userTable = TableMetadata.builder()
    .name("users")
    .schema("public")
    .rowCount(1000L)
    .sizeInBytes(51200L)
    .columns(List.of(idColumn, nameColumn))
    .build();
```

### Database Connection Interface

```java
public class MySqlConnectionProvider implements DatabaseConnectionProvider {
    @Override
    public Connection getConnection(Map<String, String> connectionParams) {
        // Implementation for MySQL connection
    }
    
    @Override
    public void closeConnection(Connection connection) {
        // Implementation for closing connection
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MYSQL;
    }
}
```

### PII Detection

```java
public class SampleService {
    private final PiiDetector piiDetector;
    
    public SampleService(PiiDetector piiDetector) {
        this.piiDetector = piiDetector;
    }
    
    public PiiDetectionResult analyzeColumn(ColumnMetadata column, SampleResult sampleResult) {
        return piiDetector.detectPii(column, sampleResult);
    }
}
```

## Testing

The module includes comprehensive unit tests for all components:

```java
@Test
void testColumnMetadata() {
    ColumnMetadata column = ColumnMetadata.builder()
        .name("email")
        .dataType("VARCHAR")
        .characterMaxLength(255)
        .build();
    
    assertEquals("email", column.getName());
    assertEquals("VARCHAR", column.getDataType());
    assertEquals(255, column.getCharacterMaxLength());
}
```

## Best Practices

This module adheres to the following best practices:

1. **Immutable Objects**: All model classes use the builder pattern for construction and are immutable
2. **Interface-Driven Design**: Core functionality is defined through interfaces
3. **Clear Exception Hierarchy**: Specific exceptions for different failure scenarios
4. **Comprehensive Documentation**: All classes and methods are properly documented
5. **Thorough Testing**: All components have unit tests
