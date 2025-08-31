# Price Watcher Platform v2.0

A comprehensive microservices-based platform for monitoring product prices across multiple e-commerce stores with real-time notifications and analytics.

## Architecture

The platform is built using a microservices architecture with the following components:

### Core Services

- **API Gateway** (Port 8080) - Spring Cloud Gateway with routing, load balancing, and circuit breakers
- **User Service** (Port 8081) - User management, authentication, and authorization
- **Product Service** (Port 8082) - Product CRUD operations and URL validation
- **Price Monitor Service** (Port 8083) - Scheduled price monitoring and web scraping
- **Notification Service** (Port 8084) - Multi-channel notifications (Email, SMS, Push, Telegram)
- **Analytics Service** (Port 8085) - Data analytics and reporting

### Shared Libraries

- **Common** - Shared DTOs, exceptions, and utilities
- **Events** - Event schemas and RabbitMQ configuration
- **Security** - JWT authentication and shared security configurations

### Infrastructure

- **PostgreSQL** - Primary database for all services
- **Redis** - Caching and session management
- **RabbitMQ** - Message queue for event-driven architecture
- **MailHog** - Email testing in development

## Features

### User Management
- Complete user registration and authentication
- JWT token-based security with refresh tokens
- Password recovery via email
- User profiles (FREE, PREMIUM)
- Notification preferences management
- Rate limiting per user

### Product Management
- Support for major Brazilian e-commerce stores:
  - Amazon (amazon.com, amazon.com.br)
  - Mercado Livre (mercadolivre.com.br)
  - Americanas (americanas.com.br)
  - Magazine Luiza (magazineluiza.com.br)
  - Submarino (submarino.com.br)
  - Casas Bahia (casasbahia.com.br)
- Automatic URL validation and store detection
- Product categorization
- Custom price monitoring rules
- Product image and metadata extraction

### Price Monitoring
- Configurable monitoring schedules
- Intelligent retry mechanisms with exponential backoff
- Rate limiting to avoid IP blocking
- Support for custom CSS selectors
- Price history tracking
- Circuit breakers for resilience

### Notifications
- Email notifications with customizable templates
- Prepared for SMS, Push notifications, and Telegram
- Template engine with Thymeleaf
- Notification queuing with RabbitMQ
- Rate limiting per user

### Analytics & Reporting
- Price history analytics
- User behavior tracking
- Performance metrics
- Prepared for InfluxDB integration

## Technology Stack

- **Java 17** with Spring Boot 3.2+
- **Spring Security 6** for authentication and authorization
- **Spring Data JPA** for database operations
- **Spring Cloud Gateway** for API routing
- **PostgreSQL 15** as primary database
- **Redis 7** for caching and sessions
- **RabbitMQ** for messaging
- **Docker & Docker Compose** for containerization
- **Flyway** for database migrations
- **Prometheus & Micrometer** for metrics
- **Jsoup** for web scraping

## Quick Start

### Prerequisites

- Java 17+
- Docker and Docker Compose
- Maven 3.6+

### Running with Docker Compose

1. Clone the repository:
```bash
git clone https://github.com/ferrazsergio/price-watcher.git
cd price-watcher
```

2. Build the project:
```bash
./mvnw clean package -DskipTests
```

3. Start the infrastructure and services:
```bash
docker-compose up --build
```

### Service URLs

Once running, the services will be available at:

- **API Gateway**: http://localhost:8080
- **User Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **Price Monitor Service**: http://localhost:8083
- **Notification Service**: http://localhost:8084
- **Analytics Service**: http://localhost:8085
- **RabbitMQ Management**: http://localhost:15672 (admin/admin)
- **MailHog Web UI**: http://localhost:8025

### Database Access

- **PostgreSQL**: localhost:5432
  - Database: pricewatcher
  - Username: postgres
  - Password: postgres

- **Redis**: localhost:6379

## API Documentation

### Authentication

All API endpoints (except registration and health checks) require JWT authentication.

#### Register User
```bash
POST /api/users/register
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+5511999999999"
}
```

#### Login
```bash
POST /api/users/auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "securePassword123"
}
```

### Product Management

#### Create Product
```bash
POST /api/products
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "name": "iPhone 15 Pro",
  "description": "Latest iPhone model",
  "url": "https://www.amazon.com.br/dp/B0CHX1W1XY",
  "targetPrice": 5999.99,
  "category": "ELECTRONICS",
  "brand": "Apple",
  "model": "iPhone 15 Pro"
}
```

#### Get User Products
```bash
GET /api/products?page=0&size=20&search=iphone&category=ELECTRONICS
Authorization: Bearer <jwt-token>
```

#### Update Product Status
```bash
PATCH /api/products/{id}/status
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "status": "PAUSED"
}
```

## Development

### Project Structure

```
price-watcher-platform/
├── shared/
│   ├── common/          # Shared utilities and DTOs
│   ├── events/          # Event schemas and messaging
│   └── security/        # JWT and security configuration
├── services/
│   ├── api-gateway/     # Spring Cloud Gateway
│   ├── user-service/    # User management and auth
│   ├── product-service/ # Product CRUD operations
│   ├── price-monitor-service/ # Price monitoring logic
│   ├── notification-service/  # Multi-channel notifications
│   └── analytics-service/     # Analytics and reporting
├── infrastructure/
│   └── docker/          # Docker configurations
└── docs/                # Documentation
```

### Building Individual Services

```bash
# Build all services
./mvnw clean package

# Build specific service
./mvnw clean package -pl services/user-service

# Run tests
./mvnw test

# Run specific service tests
./mvnw test -pl services/product-service
```

### Database Migrations

Each service with a database has Flyway migrations in `src/main/resources/db/migration/`.

To run migrations manually:
```bash
./mvnw flyway:migrate -pl services/user-service
```

### Adding New Stores

To add support for a new e-commerce store:

1. Add the store to `SupportedStore` enum in Product entity
2. Implement URL validation in `ProductValidationService`
3. Add scraping logic in Price Monitor Service
4. Update documentation

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

# Run integration tests
./mvnw test -Dtest="**/*IntegrationTest"
```

### Test Coverage

The project maintains >80% test coverage across all modules.

## Monitoring and Observability

### Health Checks

All services expose health endpoints:
- `/actuator/health` - Basic health status
- `/actuator/metrics` - Prometheus metrics
- `/actuator/info` - Application information

### Metrics

The platform exposes metrics via Prometheus endpoints for:
- Request/response times
- Error rates
- Business metrics (products monitored, notifications sent)
- System metrics (memory, CPU, database connections)

### Logging

Structured JSON logging is configured for all services with:
- Request/response correlation IDs
- User context
- Performance metrics
- Error details

## Security

### Authentication & Authorization

- JWT-based authentication with refresh tokens
- Role-based access control (FREE, PREMIUM users)
- API rate limiting
- Input validation and sanitization

### Security Headers

- CSRF protection
- XSS protection
- Content Security Policy
- Secure cookie settings

### Data Protection

- Password encryption with BCrypt
- Sensitive data encryption at rest
- HTTPS enforcement in production
- API input validation

## Deployment

### Production Deployment

1. Update environment variables in `docker-compose.yml`
2. Set strong passwords for all services
3. Configure SSL certificates
4. Set up monitoring and alerting
5. Configure backup strategies

### Environment Variables

Key environment variables for production:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://db-host:5432/pricewatcher
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=secure_password

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000

# Email
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=app-password

# Redis
SPRING_DATA_REDIS_HOST=redis-host
SPRING_DATA_REDIS_PASSWORD=redis-password

# RabbitMQ
SPRING_RABBITMQ_HOST=rabbitmq-host
SPRING_RABBITMQ_USERNAME=rabbitmq-user
SPRING_RABBITMQ_PASSWORD=rabbitmq-password
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Coding Standards

- Follow Spring Boot best practices
- Maintain test coverage >80%
- Use meaningful commit messages
- Document public APIs
- Add integration tests for new features

## Roadmap

### Phase 1 (Current) - Foundation ✅
- [x] Microservices architecture
- [x] User management and authentication
- [x] Product management
- [x] Basic price monitoring
- [x] Email notifications
- [x] Infrastructure setup

### Phase 2 - Enhanced Features
- [ ] Advanced price monitoring algorithms
- [ ] SMS and push notifications
- [ ] Telegram bot integration
- [ ] Mobile app (React Native)
- [ ] Advanced analytics dashboard

### Phase 3 - Scale and Performance
- [ ] Kubernetes deployment
- [ ] Advanced monitoring (Grafana, ELK stack)
- [ ] Multi-region support
- [ ] Performance optimizations
- [ ] AI-powered price predictions

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:
- Create an issue on GitHub
- Email: support@pricewatcher.com
- Documentation: [Wiki](https://github.com/ferrazsergio/price-watcher/wiki)
