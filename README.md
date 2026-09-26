# \# RideLink Backend (IT3130 Group Assignment)

# 

# Backend microservices for a fictional ride-sharing platform.

# 

# \## Services and Owners

# 

# \- Account Service

# &#x20; - Folder: account-service

# &#x20; - Port: 8081

# &#x20; - Owner: <Damika / Damika 78783>



# \- Driver \& Vehicle Service

# &#x20; - Folder: driver-vehicle-service

# &#x20; - Port: 8082

# &#x20; - Owner: <Malaka / Malaka786>



# \- Ride Management Service

# &#x20; - Folder: ride-management-service

# &#x20; - Port: 8083

# &#x20; - Owner: <Chamodi / Wijesinghe2004>



# \- Fare \& Payment Service

# &#x20; - Folder: fare-payment-service

# &#x20; - Port: 8084

# &#x20; - Owner: <Nirman / binadith821>

# 

# \## Branching Workflow

# 

# \- `main`: stable, demo-ready version only. No direct pushes.

# \- `develop`: integration branch. All feature branches merge here through a pull request.

# \- `feature/<short-description>`: one branch per task, created from `develop`.

# \- Every pull request needs at least one review from another member before merging.

# 

# \## Tech Stack

# 

# \- Java 21

# \- Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Spring Security, Validation)

# \- MySQL 8

# \- JWT (jjwt 0.12.6) for authentication

# \- Maven

# 

# \## Prerequisites

# 

# \- JDK 21

# \- Maven (or the included mvnw wrapper)

# \- MySQL 8 running locally on port 3306

# 

# \## Configuration

# 

# Secrets are never committed. Each service reads them from environment variables.

# 

# \- DB\_USERNAME: MySQL username (default: root), used by all services

# \- DB\_PASSWORD: MySQL password, used by all services

# \- JWT\_SECRET: secret key used to sign JWT tokens, used by account-service

# 

# Windows (cmd) example, run in the same terminal before starting a service:

# 

# &#x20;   set DB\_PASSWORD=your\_password

# &#x20;   set JWT\_SECRET=your\_long\_random\_secret

# 

# \## Databases

# 

# Each service owns its own database. No service accesses another service's database.

# 

# \- account-service: ridelink\_account\_db

# \- driver-vehicle-service: ridelink\_driver\_vehicle\_db

# \- ride-management-service: ridelink\_ride\_db

# \- fare-payment-service: ridelink\_fare\_payment\_db

# 

# \## Running a Service

# 

# &#x20;   cd account-service

&#x20;        

# &#x20;   mvnw spring-boot:run

# 

# 

# \## Endpoints and Swagger UI

# 

# To be added when each service is completed.

