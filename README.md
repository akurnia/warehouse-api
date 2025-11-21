# Warehouse API

Simple shop warehouse management REST API built with Spring Boot.

The system manages items, their variants (for example size / color), pricing, and stock levels, and prevents selling items when there is not enough stock available. It also tracks stock movements over time for auditing.

---

## Tech Stack

- **Language**: Java 17  
- **Framework**: Spring Boot 3.x  
- **Build tool**: Gradle  
- **Persistence**: Spring Data JPA + H2 in-memory database  
- **Validation**: Jakarta Bean Validation  
- **Testing**: Spring Boot Test, JUnit 5  
- **Other**: Lombok (to reduce boilerplate)

---

## How to Run

### Requirements

- Java 17+
- Git

### Steps

```bash
git clone <your-repo-url>.git
cd warehouse-api

./gradlew test
./gradlew bootRun
```

By default the application runs on:

- `http://localhost:8080`

---

## Database

The application uses an in-memory H2 database.

H2 console:

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:warehouse-db`
- User: `sa`
- Password: *(empty)*

Configuration is in `src/main/resources/application.yaml`.

---

## Architecture

Package structure:

```text
com.arief.warehouse.warehouse_api
  ├─ config        # JPA auditing configuration
  ├─ controller    # REST controllers
  ├─ dto           # Request/response DTOs
  ├─ entity        # JPA entities
  ├─ exception     # Custom exceptions + global handler
  ├─ repository    # Spring Data JPA repositories
  ├─ service       # Service interfaces
  │   └─ impl      # Service implementations
  └─ WarehouseApiApplication
```


## API Endpoints

Base URL: `http://localhost:8080`

---

### API Documentation

To explore and test the API interactively, you can use the Postman collection included in this repository:

- **File**: `warehouse-api.postman_collection.json`

The collection covers all main endpoints:

- CRUD operations for **Items**
- CRUD operations for **Item Variants**
- Stock operations: **sell** and **stock adjust**
- Stock movement history (**StockMovement**)

#### How to Use with Postman

1. Open **Postman**.
2. Click **Import**.
3. Select the file `warehouse-api.postman_collection.json` from the project root.
4. (Optional but recommended) Create a new **Environment**, e.g. `Warehouse API Local`, and add:
   - `baseUrl` = `http://localhost:8080`
5. Make sure the application is running:
   ```bash
   ./gradlew bootRun