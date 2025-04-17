# PrivSense: Database PII Detection and Analysis System

![PrivSense Logo](docs/images/logo.png)

[![Build Status](https://github.com/cgi-privsense/privsense/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/cgi-privsense/privsense/actions)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=privsense&metric=coverage)](https://sonarcloud.io/dashboard?id=privsense)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## Overview

PrivSense is an advanced database scanning and PII (Personally Identifiable Information) detection system designed to help organizations identify, analyze, and manage sensitive data across their database systems. It provides comprehensive tools for database metadata extraction, intelligent data sampling, and multi-strategy PII detection.

![System Architecture](docs/images/architecture.png)

## Key Features

- **Multi-Database Support**: Connect to MySQL, PostgreSQL, Oracle, SQL Server, and more
- **Comprehensive Metadata Extraction**: Automatically extract and analyze database schema information
- **Intelligent Data Sampling**: Multiple sampling strategies optimized for large databases
- **Advanced PII Detection**: Combined approach using:
  - Column name heuristics
  - Regular expression pattern matching
  - Named Entity Recognition with Machine Learning
- **REST API**: Flexible API for integration with other systems
- **Detailed Reporting**: Comprehensive reports on PII findings with confidence scores
- **Monitoring & Metrics**: Built-in Prometheus/Grafana dashboard for system monitoring

## Modules

PrivSense is built using a modular architecture to ensure clean separation of concerns:

- [**privsense-common**](privsense-common/README.md): Core models, interfaces and utilities
- [**privsense-db-scanner**](privsense-db-scanner/README.md): Database connection and metadata extraction
- [**privsense-data-sampler**](privsense-data-sampler/README.md): Data sampling strategies and statistics
- [**privsense-pii-detector**](privsense-pii-detector/README.md): PII detection algorithms and orchestration
- [**privsense-api**](privsense-api/README.md): REST API gateway and service layer
- [**python-ner-service**](python-ner-service/README.md): Python-based Named Entity Recognition service

## Getting Started

### Prerequisites

- JDK 21 or higher
- Maven 3.8+ 
- Docker and Docker Compose (for containerized deployment)
- Python 3.9+ (for NER service)

### Installation

#### Method 1: Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/cgi-privsense/privsense.git
   cd privsense
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Start the Python NER service:
   ```bash
   cd python-ner-service
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   pip install -r requirements.txt
   python -m spacy download en_core_web_sm
   python app.py
   ```

4. Run the API:
   ```bash
   cd ../privsense-api
   mvn spring-boot:run
   ```

5. Access the API at http://localhost:8080 and Swagger UI at http://localhost:8080/swagger-ui.html

#### Method 2: Docker Deployment

1. Clone the repository:
   ```bash
   git clone https://github.com/cgi-privsense/privsense.git
   cd privsense
   ```

2. Build and start the containers:
   ```bash
   docker-compose up -d
   ```

3. Access the API at http://localhost:8080 and Swagger UI at http://localhost:8080/swagger-ui.html

## Configuration

The system can be configured through application properties:

```properties
# Database connection pooling
privsense.db.pool.max-size=10
privsense.db.pool.idle-timeout=300000

# PII detection thresholds
privsense.pii.confidence.threshold=0.7
privsense.pii.regex.match-percent=0.6

# NER service configuration
privsense.ner.service.url=http://localhost:5000
privsense.ner.timeout=5000
```

See each module's README for detailed configuration options.

## Usage Examples

### Scanning a Database for PII

```bash
curl -X POST http://localhost:8080/api/scan \
  -H "Content-Type: application/json" \
  -d '{
    "connectionParams": {
      "databaseType": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "customer_db",
      "username": "root",
      "password": "password"
    },
    "scanOptions": {
      "sampleSize": 1000,
      "detectionStrategies": ["HEURISTIC", "REGEX", "NER_MODEL"],
      "confidenceThreshold": 0.7
    }
  }'
```

### Getting Metadata for a Table

```bash
curl -X POST http://localhost:8080/api/metadata/tables/customers \
  -H "Content-Type: application/json" \
  -d '{
    "connectionParams": {
      "databaseType": "MYSQL",
      "host": "localhost",
      "port": 3306,
      "database": "customer_db",
      "username": "root",
      "password": "password"
    }
  }'
```

## Monitoring

PrivSense provides built-in monitoring through Prometheus and Grafana:

1. Access Prometheus at http://localhost:9090
2. Access Grafana at http://localhost:3000 (default login: admin/admin)
   - A pre-configured dashboard is available at "PrivSense Metrics"

## API Documentation

Explore the API using Swagger UI at http://localhost:8080/swagger-ui.html

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to the project.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- This project uses various open-source libraries and tools
- Special thanks to the spaCy team for their excellent NLP library
- JaCoCo for code coverage
- SonarCloud for code quality analysis
