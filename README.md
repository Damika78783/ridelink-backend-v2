# RideLink Backend

Backend microservices for the IT3130 RideLink group assignment.

## Services

| Service | Directory | Port | Persistence | Source branch |
| --- | --- | ---: | --- | --- |
| Account | `account-service` | 8081 | MySQL | `feature/account-service-setup` |
| Driver & Vehicle | `driver-vehicle-service` | 8082 | MongoDB | `feature/driver-vehicle-service-setup` |
| Ride Management | `ride-management-service` | 8083 | MySQL | `feature/ride-management-service-setup` |
| Fare & Payment | `fare-payment-service` | 8084 | PostgreSQL | `fare-payment` |

Each service owns its database. Services must not query or modify another service's database.

## Prerequisites

- JDK 21 (also supports the Java 17 services)
- MySQL for Account and Ride Management
- MongoDB for Driver & Vehicle
- PostgreSQL for Fare & Payment

Maven installation is not required; every service includes its own Maven wrapper.

## Configuration

Copy each service's `.env.example` values into your local environment or IDE run configuration. Do not commit `.env` files or real credentials.

Use the same `JWT_SECRET` value for all four services. It must contain at least 32 characters so tokens issued by Account Service can be verified by the other services.

## Start-up order

1. Start the databases.
2. Start `account-service`.
3. Start `driver-vehicle-service`.
4. Start `ride-management-service`.
5. Start `fare-payment-service`.

From each service directory on Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Tests

Run the following command from each service directory:

```powershell
.\mvnw.cmd test
```

## API locations

| Service | Base API | Swagger UI |
| --- | --- | --- |
| Account | `http://localhost:8081/api/accounts` | Not currently configured |
| Driver & Vehicle | `http://localhost:8082/api/drivers`, `http://localhost:8082/api/vehicles` | `http://localhost:8082/swagger-ui/index.html` |
| Ride Management | `http://localhost:8083/api/rides` | Not currently configured |
| Fare & Payment | `http://localhost:8084/api/fares`, `http://localhost:8084/api/payments` | `http://localhost:8084/swagger-ui/index.html` |

## Branching workflow

- `main`: stable, demo-ready releases only.
- `develop`: integration branch for completed service work.
- `feature/<description>`: service and task branches merged through reviewed pull requests.

The current local integration is intentionally based on `develop`; `main` is not modified.
