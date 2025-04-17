# PrivSense DB Scanner Module

## Overview

The DB Scanner module is responsible for establishing connections to various database systems and extracting comprehensive metadata about tables, columns, relationships, and constraints. It acts as the foundation layer for the PrivSense system, enabling other modules to interact with database systems using standardized models and interfaces.

## Architecture

![DB Scanner Architecture](../docs/images/db-scanner-module.png)

The module follows a layered architecture pattern:

```
com.cgi.privsense.dbscanner
├── connector     # Database-specific connection providers 
├── factory       # Factories for creating connections and extractors
├── metadata      # Metadata extraction implementations
└── service       # High-level services for metadata operations
```

## Key Components

### Database Connectors

The module provides specialized connection providers for various database systems:

- **`MySqlConnectionProvider`**: Optimized for MySQL and MariaDB connections
  - Configurable connection pooling
  - MySQL-specific parameter handling
  - Error translation

- **`OracleConnectionProvider`**: Specialized for Oracle databases
  - Support for TNS and service name connections
  - Oracle-specific connection parameters
  - Connection pool optimization for Oracle

- **`SqlServerConnectionProvider`**: Optimized for Microsoft SQL Server
  - Windows authentication support
  - SQL Server-specific connection parameters
  - Connection pooling

- **`PostgreSqlConnectionProvider`**: PostgreSQL-specific connector
  - Schema handling specific to PostgreSQL
  - Connection pooling with HikariCP

Each connector implements the `DatabaseConnectionProvider` interface from the common module and handles:
- Connection string construction
- Credential management
- Connection lifecycle
- Error handling and translation

### Connection Factory

- **`DatabaseConnectionFactory`**: Creates appropriate connection providers
  - Factory pattern implementation
  - Provider selection based on database type
  - Connection pool configuration

### Metadata Extractors

The module implements metadata extraction using the template method pattern:

- **`AbstractMetadataExtractor`**: Base class for all extractors
  - Common extraction logic and workflows
  - Resource management and cleanup
  - Error handling and logging

Database-specific implementations:

- **`JdbcMetadataExtractor`**: Generic JDBC-based metadata extraction
  - Uses JDBC DatabaseMetaData for extraction
  - Database-agnostic implementation

- **`MySqlMetadataExtractor`**: MySQL-specific optimizations
  - Uses INFORMATION_SCHEMA for better performance
  - MySQL-specific data type mappings
  - Size calculations optimized for MySQL/InnoDB

- **`OracleMetadataExtractor`**: Oracle optimizations
  - Uses data dictionary views for extraction
  - Oracle-specific schema concepts
  - Handles Oracle data types and properties

- **`SqlServerMetadataExtractor`**: SQL Server optimizations
  - Uses system catalogs for metadata
  - Handles SQL Server schema/catalog concepts
  - SQL Server-specific data type mapping

### Service Layer

- **`MetadataExtractionService`**: High-level service for metadata operations
  - Caching for performance optimization
  - Parallel extraction for large schemas
  - Retry mechanisms for transient failures
  - Result enrichment and transformation

## Features

### Multi-Database Support

The module supports multiple database systems through specialized connectors:
- MySQL/MariaDB
- Oracle
- Microsoft SQL Server
- PostgreSQL
- Extensible for other systems

### Connection Pooling

All connectors implement connection pooling using HikariCP:
- Configurable pool sizes
- Connection validation
- Connection timeout handling
- Statement caching

### Comprehensive Metadata Extraction

The extractors gather detailed information about:
- Tables and views
- Columns with data types and constraints
- Primary keys
- Foreign key relationships
- Indexes and constraints
- Table statistics (row count, size)

### Performance Optimizations

- **Metadata Caching**: Frequently accessed metadata is cached
- **Parallel Processing**: Multiple tables are processed concurrently
- **Optimized Queries**: Database-specific optimized queries instead of generic JDBC metadata
- **Connection Reuse**: Connection pooling to minimize connection overhead

### Error Handling

- **Comprehensive Exception Hierarchy**: Specialized exceptions for different error scenarios
- **Retry Mechanisms**: Automatic retries for transient errors
- **Detailed Logging**: Extensive logging with appropriate log levels

## Dependencies

```xml
<dependencies>
    <!-- Common module -->
    <dependency>
        <groupId>com.cgi</groupId>
        <artifactId>privsense-common</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Connection pooling -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
    </dependency>
    
    <!-- Database drivers -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <dependency>
        <groupId>com.oracle.database.jdbc</groupId>
        <artifactId>ojdbc8</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>mysql</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Configuration

The module accepts various configuration properties:

```properties
# Database Connection Pool Configuration
privsense.db.pool.max-size=10
privsense.db.pool.min-idle=5
privsense.db.pool.idle-timeout=300000
privsense.db.pool.connection-timeout=10000
privsense.db.pool.max-lifetime=1800000

# Metadata Extraction Configuration
privsense.metadata.cache.enabled=true
privsense.metadata.cache.ttl=3600
privsense.metadata.parallel.max-threads=4
privsense.metadata.retry.max-attempts=3
privsense.metadata.retry.delay=1000
```

## Usage Examples

### Connecting to a Database

```java
// Create connection parameters
Map<String, String> connectionParams = new HashMap<>();
connectionParams.put("host", "localhost");
connectionParams.put("port", "3306");
connectionParams.put("database", "customers");
connectionParams.put("username", "root");
connectionParams.put("password", "password");

// Create connection provider using factory
DatabaseConnectionFactory factory = new DatabaseConnectionFactory();
DatabaseConnectionProvider provider = factory.createConnectionProvider(DatabaseType.MYSQL);

// Get connection
try (Connection connection = provider.getConnection(connectionParams)) {
    // Use connection
} catch (DatabaseConnectionException e) {
    log.error("Failed to connect to database", e);
}
```

### Extracting Table Metadata

```java
// Create metadata extractor
DatabaseConnectionProvider connectionProvider = /* get connection provider */;
MetadataExtractor extractor = new MySqlMetadataExtractor(connectionProvider);

// Extract metadata for all tables
List<TableMetadata> tables = extractor.extractAllTablesMetadata();

// Extract metadata for specific table
TableMetadata userTable = extractor.extractTableMetadata("users");

// Extract metadata using pattern
List<TableMetadata> customerTables = extractor.extractTablesMetadata("customer%");
```

### Using the Service Layer

```java
@Service
public class DatabaseExplorerService {
    private final MetadataExtractionService metadataService;
    
    @Autowired
    public DatabaseExplorerService(MetadataExtractionService metadataService) {
        this.metadataService = metadataService;
    }
    
    public TableMetadata getTableDetails(DatabaseType dbType, 
                                        Map<String, String> connectionParams,
                                        String tableName) {
        return metadataService.extractTableMetadata(dbType, connectionParams, tableName);
    }
}
```

## Testing

The module includes comprehensive tests:

### Unit Tests

Unit tests for individual components using mocks:

```java
@Test
void testMySqlConnectionProvider() {
    // Setup
    MySqlConnectionProvider provider = new MySqlConnectionProvider();
    Map<String, String> params = Map.of(
        "host", "localhost",
        "port", "3306",
        "database", "test",
        "username", "user",
        "password", "pass"
    );
    
    // Verify database type
    assertEquals(DatabaseType.MYSQL, provider.getDatabaseType());
    
    // Test connection method is tested in integration tests
}
```

### Integration Tests

Integration tests using TestContainers with Podman support:

```java
@Testcontainers
class MySqlMetadataExtractorIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init-mysql.sql");
            
    private DatabaseConnectionProvider connectionProvider;
    private MetadataExtractor extractor;
    
    @BeforeEach
    void setup() {
        Map<String, String> params = Map.of(
            "host", mysql.getHost(),
            "port", String.valueOf(mysql.getFirstMappedPort()),
            "database", mysql.getDatabaseName(),
            "username", mysql.getUsername(),
            "password", mysql.getPassword()
        );
        
        connectionProvider = new MySqlConnectionProvider();
        extractor = new MySqlMetadataExtractor(connectionProvider);
    }
    
    @Test
    void testExtractAllTablesMetadata() {
        List<TableMetadata> tables = extractor.extractAllTablesMetadata();
        
        assertFalse(tables.isEmpty());
        assertTrue(tables.stream().anyMatch(t -> t.getName().equals("users")));
    }
}
```

## Best Practices

The module follows these best practices:

1. **Resource Management**: Proper handling of JDBC resources with try-with-resources
2. **Connection Pooling**: Efficient connection reuse with HikariCP
3. **Abstraction**: Clean separation between interface and implementation
4. **Testing**: Both unit and integration tests with real databases
5. **Error Handling**: Comprehensive exception handling with proper error messages
6. **Performance**: Optimized queries for each database system
