# 📦 Order-Inventory Microservices Assignment

## 📖 Overview
This project demonstrates a **microservices architecture** with two independent services:
- **Inventory Service** – Manages product batches, expiry dates, and stock updates.
- **Order Service** – Accepts and processes product orders, communicates with Inventory Service to check availability, and updates stock.

Both services are built with **Spring Boot**, **Spring Data JPA**, and **H2 in-memory databases**, following clean architecture principles and extensibility patterns.

---

## 🛠️ Requirements Implemented
- Maintain inventory of products with multiple batches and expiry dates.
- Endpoints to fetch batches sorted by expiry date and update inventory after orders.
- Order Service communicates with Inventory Service via **RestTemplate/WebClient**.
- Factory Design Pattern in Inventory Service for extensibility.
- Controller, Service, Repository layers in both services.
- Unit tests with **JUnit 5 + Mockito**.
- Integration tests with **@SpringBootTest + H2**.
- Optional: Lombok for boilerplate reduction, Swagger/OpenAPI for API docs.

---

## 🚀 Project Setup Instructions

### Prerequisites
- Java 17+
- Maven (or Gradle)
- Git

### Clone the Repository
```bash
https://github.com/yashvarshney007/order-inventory-microservices-assignment.git
cd order-inventory-microservices-assignment

```


---

## 🌐 API Documentation

### Inventory Service
- **Swagger UI:** [http://localhost:8081/swagger-ui/index.html#/Inventory](http://localhost:8081/swagger-ui/index.html#/Inventory)  
- **Endpoints:**
  - `GET /inventory/{productId}` → Returns batches sorted by expiry date.
  - `POST /inventory/update` → Updates inventory after an order.

### Order Service
- **Swagger UI:** [http://localhost:8082/swagger-ui/index.html#/Order](http://localhost:8082/swagger-ui/index.html#/Order)  
- **Endpoints:**
  - `POST /order` → Places an order and updates inventory.

---

## 🗄️ H2 Database Consoles
- **Inventory Service DB:** [http://localhost:8081/h2-console/](http://localhost:8081/h2-console/)  
  - `spring.datasource.url=jdbc:h2:mem:inventorydb`
- **Order Service DB:** [http://localhost:8082/h2-console/](http://localhost:8082/h2-console/)  
  - `spring.datasource.url=jdbc:h2:mem:orderdb`

---

## 🧪 Testing Instructions
- Run unit tests:
  ```bash
  mvn test
```
