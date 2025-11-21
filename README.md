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
  ├─ entity        # JPA entities + AuditableEntity
  ├─ exception     # Custom exceptions + global handler
  ├─ repository    # Spring Data JPA repositories
  ├─ service       # Service interfaces
  │   └─ impl      # Service implementations
  └─ WarehouseApiApplication
```


## API Endpoints

Base URL: `http://localhost:8080`

---

### Items

#### Create item

`POST /api/items`

**Request**

```json
{
  "name": "T-Shirt",
  "description": "Basic cotton T-Shirt",
  "active": true
}
```

**Response – 201 Created**

```json
{
  "id": 1,
  "name": "T-Shirt",
  "description": "Basic cotton T-Shirt",
  "active": true
}
```

#### Get item by id

`GET /api/items/{id}` → `200 OK` with `ItemResponse`  
If not found → `404 Not Found` with JSON error.

#### List all items

`GET /api/items` → `200 OK` with array of items.

#### Update item

`PUT /api/items/{id}` → `200 OK` with updated item.

#### Delete item

`DELETE /api/items/{id}` → `204 No Content`.

---

### Variants – CRUD

#### Create variant for an item

`POST /api/items/{itemId}/variants`

**Request**

```json
{
  "sku": "TSHIRT-BLACK-M",
  "color": "Black",
  "size": "M",
  "price": 99000.0,
  "initialStock": 20
}
```

**Response – 201 Created**

```json
{
  "id": 1,
  "itemId": 1,
  "sku": "TSHIRT-BLACK-M",
  "color": "Black",
  "size": "M",
  "price": 99000.0,
  "stockQuantity": 20
}
```

#### List variants for an item

`GET /api/items/{itemId}/variants`

**Response – 200 OK**

```json
[
  {
    "id": 1,
    "itemId": 1,
    "sku": "TSHIRT-BLACK-M",
    "color": "Black",
    "size": "M",
    "price": 99000.0,
    "stockQuantity": 20
  },
  {
    "id": 2,
    "itemId": 1,
    "sku": "TSHIRT-BLACK-L",
    "color": "Black",
    "size": "L",
    "price": 99000.0,
    "stockQuantity": 10
  }
]
```

#### Get a variant

`GET /api/variants/{id}` → `200 OK` with `ItemVariantResponse`  
If not found → `404 Not Found`.

#### Update a variant

`PUT /api/variants/{id}`

**Request**

```json
{
  "sku": "TSHIRT-BLACK-M-NEW",
  "color": "Black",
  "size": "M",
  "price": 105000.0
}
```

**Response – 200 OK**

```json
{
  "id": 1,
  "itemId": 1,
  "sku": "TSHIRT-BLACK-M-NEW",
  "color": "Black",
  "size": "M",
  "price": 105000.0,
  "stockQuantity": 20
}
```

#### Delete a variant

`DELETE /api/variants/{id}` → `204 No Content`.

---

### Variants – stock operations

#### Sell

`POST /api/variants/{id}/sell`

**Request**

```json
{
  "quantity": 3
}
```

**Response – 200 OK** (empty body)

If stock is not enough:

**Response – 400 Bad Request**

```json
{
  "status": 400,
  "error": "OUT_OF_STOCK",
  "message": "Not enough stock for variant 1. Requested: 5, available: 2",
  "path": "/api/variants/1/sell"
}
```

#### Adjust stock

`POST /api/variants/{id}/stock/adjust`

**Request**

```json
{
  "quantityChange": 10,
  "reason": "PURCHASE_ORDER_RECEIPT"
}
```

**Response – 200 OK** (empty body)

Validation errors return `400` with `error = "VALIDATION_ERROR"` and `details` field.

#### Get stock movements

`GET /api/variants/{id}/movements`

**Response – 200 OK**

```json
[
  {
    "id": 1,
    "type": "OUT",
    "quantityChange": -3,
    "reason": "SALE",
    "createdAt": "2025-01-01T10:05:00Z",
    "updatedAt": "2025-01-01T10:05:00Z"
  },
  {
    "id": 2,
    "type": "ADJUSTMENT",
    "quantityChange": 10,
    "reason": "PURCHASE_ORDER_RECEIPT",
    "createdAt": "2025-01-01T11:00:00Z",
    "updatedAt": "2025-01-01T11:00:00Z"
  }
]
```

---
