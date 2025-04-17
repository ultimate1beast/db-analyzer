# PrivSense API Module

## Overview

The API module serves as the gateway for the PrivSense system, providing REST endpoints for database connection, metadata extraction, data sampling, and PII detection. It integrates all other modules into a cohesive system with a clean, well-documented API that follows REST principles.

## Architecture



The module follows a standard Spring MVC architecture:

```
com.cgi.privsense.api
├── controller     # REST controllers for API endpoints
├── dto            # Data Transfer Objects for requests and responses
├── service        # Service layer integrating other modules
├── validation     # Request validation components
└── config         # Application configuration
```

## Key Components

### Controllers

The module provides several REST controllers for different functionality domains:

- **`MetadataController`**: Endpoints for database metadata operations
  - List all tables in a database
  - Get detailed metadata for specific tables
  - Search tables by pattern
  - Retrieve schema information
  
- **`SamplingController`**: Endpoints for data sampling operations
  - Sample data from columns with different strategies
  - Get sample statistics
  - Control sample size and methodology
  
- **`PiiController`**: Endpoints for PII detection
  - Detect PII in specific columns
  - Scan entire tables for PII
  - Get detailed PII analysis reports
  
- **`CombinedController`**: Endpoints for combined operations
  - Connect, extract metadata, sample, and detect PII in one operation
  - Generate comprehensive database PII reports
  - Execute batch operations across multiple tables

- **`ExceptionHandlerController`**: Global exception handling
  - Consistent error responses
  - Detailed error information
  - Security-aware error handling

### Data Transfer Objects (DTOs)

The module uses DTO pattern to separate API contracts from internal models:

#### Request DTOs

- **`ConnectionRequestDto`**: Database connection parameters
  - Database type (MySQL, Oracle, etc.)
  - Connection parameters (host, port, credentials)
  - Connection options

- **`SampleRequestDto`**: Sampling operation parameters
  - Connection request
  - Table and column information
  - Sample size and strategy
  - Additional options

- **`PiiDetectionRequestDto`**: PII detection parameters
  - Connection request
  - Table and column information
  - Detection strategies to use
  - Confidence thresholds

#### Response DTOs

- **`TableMetadataDto`**: Table metadata information
  - Table details (name, schema, etc.)
  - Column information
  - Size and row count estimates
  - Relationship information

- **`ColumnMetadataDto`**: Column details
  - Name and data type
  - Constraints and position
  - Key information

- **`SampleResultDto`**: Sampling results
  - Sample data
  - Statistical information
  - Execution metrics

- **`PiiDetectionResultDto`**: PII detection results
  - PII presence status
  - Confidence score
  - Detected PII type
  - Detection method
  - Detailed analysis information

### Service Layer

- **`DatabaseMetadataService`**: Handles metadata operations
  - Integrates with DB Scanner module
  - Manages connections
  - Caches metadata
  - Handles permission checks

- **`DataSamplingService`**: Manages data sampling
  - Integrates with Data Sampler module
  - Handles sampling strategy selection
  - Manages resource usage
  
- **`PiiDetectionService`**: Orchestrates PII detection
  - Integrates with PII Detector module
  - Manages detection processes
  - Aggregates and enhances results

### Validation

- **`ConnectionValidator`**: Validates database connection parameters
  - Required field validation
  - Format checking
  - Security checks

- **`RequestValidator`**: Validates API requests
  - Parameter validation
  - Business rule validation
  - Permission validation

### Configuration

- **`OpenApiConfig`**: Swagger/OpenAPI documentation
  - API documentation configuration
  - Schema customization
  - Example generation
  
- **`SecurityConfig`**: API security configuration
  - Authentication configuration
  - Authorization rules
  - CORS settings
  
- **`MonitoringConfig`**: Metrics and monitoring
  - Prometheus metrics
  - Health checks
  - Performance indicators

## Features

### RESTful API

- **Resource-Based Endpoints**: Clear resource hierarchy
- **HTTP Verbs**: Proper use of GET, POST, PUT, DELETE
- **Status Codes**: Appropriate HTTP status codes
- **Pagination**: Paginated responses for large result sets
- **Filtering**: Query parameters for filtering results
- **Sorting**: Sort parameters for ordered results

### Security

- **Authentication**: Basic auth and API key support
- **Authorization**: Role-based access control
- **Secure Headers**: Security-related HTTP headers
- **Input Validation**: Thorough request validation
- **Sensitive Information Handling**: Masking of sensitive data in logs and responses

### Documentation

- **OpenAPI/Swagger**: Interactive API documentation
- **Example Requests**: Example requests and responses
- **Schema Documentation**: Detailed schema information
- **Error Documentation**: Comprehensive error documentation

### Monitoring and Metrics

- **Prometheus Integration**: Performance and usage metrics
- **Health Endpoints**: Health check endpoints
- **Audit Logging**: Detailed audit logging

### Error Handling

- **Consistent Error Format**: Standardized error responses
- **Detailed Error Messages**: Clear error descriptions
- **Error Codes**: Unique error codes
- **Validation Errors**: Detailed field validation errors

## Dependencies

```xml
<dependencies>
    <!-- Common module -->
    <dependency>
        <groupId>com.cgi</groupId>
        <artifactId>privsense-common</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- DB Scanner module -->
    <dependency>
        <groupId>com.cgi</groupId>
        <artifactId>privsense-db-scanner</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Data Sampler module -->
    <dependency>
        <groupId>com.cgi</groupId>
        <artifactId>privsense-data-sampler</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- PII Detector module -->
    <dependency>
        <groupId>com.cgi</groupId>
        <artifactId>privsense-pii-detector</artifactId>
        <version>${project.version}</version>
    </dependency>
    
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Spring Boot Actuator for monitoring -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Prometheus for metrics -->
    <dependency>
        <groupId>io.micrometer</groupId>
        <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
    
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- OpenAPI/Swagger documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.0.2</version>
    </dependency>
    
    <!-- MapStruct for DTO mapping -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.3.Final</version>
    </dependency>
</dependencies>
```

## Configuration

The module accepts various configuration properties:

```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Security Configuration
spring.security.user.name=admin
spring.security.user.password=secret

# CORS Configuration
privsense.api.cors.allowed-origins=*
privsense.api.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
privsense.api.cors.allowed-headers=*
privsense.api.cors.max-age=3600

# Rate Limiting
privsense.api.rate-limit.enabled=true
privsense.api.rate-limit.limit=100
privsense.api.rate-limit.refresh-period=60

# OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=when_authorized

# Logging Configuration
logging.level.com.cgi.privsense=INFO
```

## API Endpoints

The module provides the following main API endpoints:

### Metadata Endpoints

- **`POST /metadata/tables`**: List all tables
- **`POST /metadata/tables/{tableName}`**: Get table metadata
- **`POST /metadata/tables/search`**: Search tables by pattern

### Sampling Endpoints

- **`POST /sampling/column`**: Sample data from a column
- **`POST /sampling/table`**: Sample data from all columns in a table
- **`POST /sampling/tables`**: Sample data from multiple tables

### PII Detection Endpoints

- **`POST /pii/detect/column`**: Detect PII in a column
- **`POST /pii/detect/table`**: Detect PII in all columns in a table
- **`POST /pii/detect/database`**: Scan entire database for PII

### Combined Operations

- **`POST /scan`**: Comprehensive database scanning
- **`POST /scan/table`**: Comprehensive table scanning

### Monitoring Endpoints

- **`GET /actuator/health`**: System health information
- **`GET /actuator/prometheus`**: Prometheus metrics
- **`GET /actuator/info`**: System information

## Usage Examples

### Connecting to a Database and Listing Tables

```bash
curl -X POST http://localhost:8080/api/metadata/tables \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46c2VjcmV0" \
  -d '{
    "databaseType": "MYSQL",
    "connectionParams": {
      "host": "localhost",
      "port": 3306,
      "database": "customers",
      "username": "user",
      "password": "password"
    }
  }'
```

Response:
```json
[
  {
    "name": "customers",
    "schema": "public",
    "columns": [
      {
        "name": "id",
        "dataType": "INT",
        "ordinalPosition": 1,
        "nullable": false,
        "isPrimaryKey": true
      },
      {
        "name": "name",
        "dataType": "VARCHAR",
        "ordinalPosition": 2,
        "nullable": true,
        "isPrimaryKey": false,
        "characterMaxLength": 255
      },
      {
        "name": "email",
        "dataType": "VARCHAR",
        "ordinalPosition": 3,
        "nullable": true,
        "isPrimaryKey": false,
        "characterMaxLength": 255
      }
    ],
    "rowCount": 1000,
    "sizeInBytes": 102400
  },
  {
    "name": "orders",
    "schema": "public",
    "columns": [
      // Column details...
    ],
    "rowCount": 5000,
    "sizeInBytes": 307200
  }
]
```

### Sampling a Column

```bash
curl -X POST http://localhost:8080/api/sampling/column \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46c2VjcmV0" \
  -d '{
    "connectionRequest": {
      "databaseType": "MYSQL",
      "connectionParams": {
        "host": "localhost",
        "port": 3306,
        "database": "customers",
        "username": "user",
        "password": "password"
      }
    },
    "tableName": "customers",
    "columnName": "email",
    "sampleSize": 10,
    "samplingStrategy": "RANDOM"
  }'
```

Response:
```json
{
  "tableName": "customers",
  "columnName": "email",
  "samples": [
    "john.doe@example.com",
    "alice.smith@test.org",
    "bob.jones@company.net",
    // More samples...
  ],
  "statistics": {
    "distinctValueCount": 10,
    "nullCount": 0,
    "nullPercentage": 0.0,
    "minValue": null,
    "maxValue": null,
    "valueDistribution": {}
  },
  "totalRowsScanned": 10,
  "executionTimeMs": 145
}
```

### Detecting PII

```bash
curl -X POST http://localhost:8080/api/pii/detect/column \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46c2VjcmV0" \
  -d '{
    "connectionRequest": {
      "databaseType": "MYSQL",
      "connectionParams": {
        "host": "localhost",
        "port": 3306,
        "database": "customers",
        "username": "user",
        "password": "password"
      }
    },
    "tableName": "customers",
    "columnName": "email",
    "detectionStrategies": ["HEURISTIC", "REGEX", "NER_MODEL"],
    "confidenceThreshold": 0.7
  }'
```

Response:
```json
{
  "tableName": "customers",
  "columnName": "email",
  "containsPii": true,
  "confidenceScore": 0.95,
  "piiType": "EMAIL",
  "detectionStrategy": "COMBINED",
  "detailsByStrategy": {
    "HEURISTIC": {
      "isDetected": true,
      "confidence": 0.90,
      "details": "Column name 'email' matched pattern"
    },
    "REGEX": {
      "isDetected": true,
      "confidence": 0.98,
      "details": "98% of samples matched email pattern"
    },
    "NER_MODEL": {
      "isDetected": true,
      "confidence": 0.92,
      "details": "NER model identified as EMAIL type"
    }
  }
}
```

### Full Database Scan

```bash
curl -X POST http://localhost:8080/api/scan \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46c2VjcmV0" \
  -d '{
    "connectionRequest": {
      "databaseType": "MYSQL",
      "connectionParams": {
        "host": "localhost",
        "port": 3306,
        "database": "customers",
        "username": "user",
        "password": "password"
      }
    },
    "scanOptions": {
      "sampleSize": 100,
      "detectionStrategies": ["HEURISTIC", "REGEX", "NER_MODEL"],
      "confidenceThreshold": 0.7
    }
  }'
```

Response:
```json
{
  "summary": {
    "totalTables": 5,
    "totalColumns": 37,
    "columnsWithPii": 12,
    "executionTimeMs": 3245
  },
  "piiFindings": [
    {
      "table": "customers",
      "column": "email",
      "piiType": "EMAIL",
      "confidence": 0.95
    },
    {
      "table": "customers",
      "column": "phone",
      "piiType": "PHONE",
      "confidence": 0.92
    },
    // More findings...
  ],
  "tables": [
    // Detailed table and column information...
  ]
}
```

## Testing

### Unit Tests

```java
@WebMvcTest(MetadataController.class)
class MetadataControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private DatabaseMetadataService metadataService;
    
    @Test
    void testListAllTables() throws Exception {
        // Setup mock data
        List<TableMetadataDto> tables = List.of(
            new TableMetadataDto("users", "public", List.of(), 100L, 10240L),
            new TableMetadataDto("orders", "public", List.of(), 500L, 51200L)
        );
        
        ConnectionRequestDto request = new ConnectionRequestDto(
            DatabaseType.MYSQL,
            Map.of("host", "localhost", "database", "test")
        );
        
        when(metadataService.listAllTables(any())).thenReturn(tables);
        
        // Execute and verify
        mockMvc.perform(post("/metadata/tables")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("users"))
            .andExpect(jsonPath("$[1].name").value("orders"));
    }
    
    private String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Integration Tests

```java
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private DatabaseMetadataService metadataService;
    
    @MockBean
    private DataSamplingService samplingService;
    
    @MockBean
    private PiiDetectionService piiDetectionService;
    
    @Test
    void testCompleteWorkflow() throws Exception {
        // Setup mock data for metadata
        List<TableMetadataDto> tables = /* create test data */;
        when(metadataService.listAllTables(any())).thenReturn(tables);
        
        // Setup mock data for sampling
        SampleResultDto sampleResult = /* create test data */;
        when(samplingService.sampleColumn(any(), any(), any(), anyInt())).thenReturn(sampleResult);
        
        // Setup mock data for PII detection
        PiiDetectionResultDto piiResult = /* create test data */;
        when(piiDetectionService.detectPii(any(), any(), any())).thenReturn(piiResult);
        
        // Execute and verify metadata request
        mockMvc.perform(post("/metadata/tables")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(/* test request */)))
                .andExpect(status().isOk());
                
        // Execute and verify sampling request
        // Execute and verify PII detection request
    }
}
```

## Best Practices

The module follows these best practices:

1. **Clean API Design**: RESTful design principles
2. **Request Validation**: Thorough validation of all inputs
3. **DTO Pattern**: Separation of API models from domain models
4. **Error Handling**: Comprehensive and consistent error handling
5. **Documentation**: Detailed API documentation with OpenAPI/Swagger
6. **Security**: Authentication, authorization, and secure defaults
7. **Monitoring**: Metrics, health checks, and logging
8. **Testing**: Unit tests and integration tests for all endpoints

## API Documentation

API documentation is available through OpenAPI/Swagger:

- Swagger UI: http://localhost:8080/api/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api/api-docs
- OpenAPI YAML: http://localhost:8080/api/api-docs.yaml
