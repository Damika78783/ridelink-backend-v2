# RideLink – Backend Microservices for a Ride-Sharing Platform

This repository houses the backend microservices for the RideLink platform.

## Microservices Architecture & Responsibility

- **Member 1**: Account Service (Port `8081`)
- **Member 2**: Driver & Vehicle Service (Port `8082`, Directory: `driver-vehicle-service`)
- **Member 3**: Ride Management Service (Port `8083`)
- **Member 4**: Payment & Rating Service (Port `8084`)

---

## 🚗 Driver & Vehicle Service (Member 2)

See [driver-vehicle-service/README.md](file:///d:/RideLink%20%E2%80%93%20Backend/driver-vehicle-service/README.md) for full endpoint specifications, models, and integration guidelines.

### Quick Start
```powershell
cd driver-vehicle-service
.\mvnw.cmd spring-boot:run
```

- **Port**: `8082`
- **Swagger UI**: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)
- **OpenAPI 3 Docs**: [http://localhost:8082/v3/api-docs](http://localhost:8082/v3/api-docs)
- **Database**: Dedicated MongoDB persistence boundary (`ridelink_driver_db`) connected to MongoDB Atlas.
