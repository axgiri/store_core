# OldLab API

## How to Run the Project

1. Install JDK 21+ and Maven 3.8+
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Main Endpoints

### Person (PersonController)
- `POST /api/v1/persons/signup` — user registration
- `POST /api/v1/persons/login` — authentication
- `GET /api/v1/persons/findById/{id}` — find by ID
- `GET /api/v1/persons/getMyColleagues` — get colleagues (paginated)
- and others.

### Activation (ActivateController)
- `POST /api/v1/activate/activate` — account activation via OTP
- `POST /api/v1/activate/send/activate/{phoneNumber}` — send OTP
- `POST /api/v1/activate/login` — login via OTP
- and others.
