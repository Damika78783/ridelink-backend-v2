# RideLink – Driver & Vehicle Microservice (Member 2)

The **Driver & Vehicle Service** is an independently runnable microservice for the RideLink ride-sharing platform backend. It owns and manages driver operational profiles, vehicle records, operational availability, service coverage areas, and simulated GPS locations.

---

## 🚀 Key Features & Responsibilities

- **Driver Operational Profile**: Manage drivers tied to their Account Service identifier (`accountId`).
- **Vehicle Registration & Association**: Register vehicles (`CAR`, `VAN`, `BIKE`, `TUKTUK`) linked to drivers.
- **Availability State Machine**: Manage operational states (`AVAILABLE`, `UNAVAILABLE`, `ON_TRIP`, `OFFLINE`). Requires a registered vehicle to transition to `AVAILABLE`.
- **Simulated Current Location**: Maintain latitude/longitude coordinates and landmark addresses for simulation.
- **Service Area Management**: Set and update operating zones (e.g., `Colombo`, `Kandy`, `Galle`).
- **Eligible Driver Retrieval**: Fast query endpoint consumed by the **Ride Management Service** to filter and sort available drivers with proximity and vehicle category matching.
- **Independent Persistence Boundary**: Owns its dedicated MongoDB database (`ridelink_driver_db`) without direct cross-database coupling.

---

## 🛠️ Tech Stack & Architecture

- **Java Version**: 17
- **Framework**: Spring Boot 3.3.4
- **Persistence**: Spring Data MongoDB (MongoDB Atlas)
- **Documentation**: Springdoc OpenAPI / Swagger UI (`v2.6.0`)
- **Security**: Spring Security 6 + JJWT (`0.12.6`) (Stateless Bearer JWT; toggleable via `JWT_SECURITY_ENABLED`)
- **Port**: `8082`

---

## ⚙️ Configuration & Environment

Environment variables can be provided via `.env` (loaded automatically on startup) or system environment:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `MONGODB_URI` | `mongodb://localhost:27017/ridelink_driver_db` | MongoDB Atlas or local connection string |
| `SERVER_PORT` | `8082` | Service HTTP port |
| `JWT_SECURITY_ENABLED` | `false` | `true` in production; `false` for local development/demos |
| `JWT_SECRET` | `RideLinkSuperSecretKeyForSigningTokens...` | 256-bit secret key for HMAC-SHA256 signature verification |

### Connected MongoDB Atlas Cluster
```text
mongodb+srv://heshanmalaka12_db_user:uZ8keE4G67lkfSLT@cluster0.ixolwe4.mongodb.net/ridelink_driver_db?appName=Cluster0
```

---

## 🏃 Running the Service

### 1. Run via Maven Wrapper
```powershell
.\mvnw.cmd spring-boot:run
```

### 2. Package and Run JAR
```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target/driver-vehicle-service-0.0.1-SNAPSHOT.jar
```

### 3. Run Tests
```powershell
.\mvnw.cmd test
```

---

## 📖 Interactive Documentation (Swagger UI)

When the service is running, open your browser:

- **Swagger UI**: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- **OpenAPI Specification**: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)

---

## 📡 REST API Reference

### 1. Driver Management (`/api/drivers`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/drivers` | Register a new driver operational profile |
| `GET` | `/api/drivers` | List all drivers |
| `GET` | `/api/drivers/{id}` | Get driver details (includes associated vehicle) |
| `GET` | `/api/drivers/account/{accountId}` | Find driver profile by Account Service ID |
| `PUT` | `/api/drivers/{id}` | Update general driver profile (license, service area) |
| `PUT` | `/api/drivers/{id}/availability` | Update availability status (`AVAILABLE`, `UNAVAILABLE`, `ON_TRIP`, `OFFLINE`) |
| `PUT` | `/api/drivers/{id}/location` | Update simulated GPS coordinates and landmark |
| `PUT` | `/api/drivers/{id}/service-area` | Update designated operational zone |
| `GET` | `/api/drivers/available` | **Inter-service API for Ride Management Service** |

### 2. Vehicle Management (`/api/vehicles`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/vehicles` | Register vehicle and assign to a driver |
| `GET` | `/api/vehicles` | List all registered vehicles |
| `GET` | `/api/vehicles/{id}` | Get vehicle details by ID |
| `GET` | `/api/vehicles/driver/{driverId}` | Get vehicle assigned to a specific driver |
| `PUT` | `/api/vehicles/{id}` | Update vehicle information (number, type, model) |
| `DELETE` | `/api/vehicles/{id}` | Remove vehicle |

---

## 🔗 Ride Management Service Integration

To find drivers for a ride request, the **Ride Management Service (Member 3)** invokes:

```http
GET http://localhost:8082/api/drivers/available?serviceArea=Colombo&vehicleType=CAR&latitude=6.9271&longitude=79.8612&radiusKm=10.0
```

### Response Payload:
```json
[
  {
    "driverId": "6ab6b6ba5c798c6bc60505ca",
    "accountId": "acc-atlas-test-001",
    "licenseNumber": "B1234567",
    "serviceArea": "Colombo",
    "currentLocation": {
      "latitude": 6.9147,
      "longitude": 79.8731,
      "address": "Kollupitiya, Colombo 03",
      "updatedAt": "2026-09-25T23:31:25.695"
    },
    "vehicleId": "6ab6b6c55c798c6bc60505cb",
    "vehicleNumber": "WP-CAB-1234",
    "vehicleType": "CAR",
    "vehicleModel": "Toyota Prius 2022",
    "distanceKm": 1.84
  }
]
```
