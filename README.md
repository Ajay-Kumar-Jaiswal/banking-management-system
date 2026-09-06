# Banking Management System

A full-stack banking management application built with **Java 17, Spring Boot, MySQL, Spring Security, JWT, and React**.

## Features

- User registration and JWT-based authentication
- BCrypt password encryption and role-based authorization
- Savings and Current account management
- Deposit and withdrawal operations
- Fund transfers with balance and ownership validation
- Atomic transactions using `@Transactional`
- Optimistic locking using JPA `@Version`
- Beneficiary management
- Transaction history and filtering
- Admin management of customers, accounts, and transactions
- REST APIs with centralized exception handling
- Unit testing using JUnit and Mockito

## Tech Stack

**Backend:** Java 17, Spring Boot, Spring Web, Spring Security, JWT, Spring Data JPA, Hibernate, Maven

**Frontend:** React, JavaScript, Axios, React Router, HTML5, CSS3

**Database:** MySQL

**Testing:** JUnit, Mockito

## Architecture

```text
React Frontend
      |
    Axios
      |
   REST APIs
      |
  Spring Boot
      |
Controller → Service → Repository
      |
 JPA / Hibernate
      |
    MySQL
```

## Project Structure

```text
banking-management-system/
├── backend/       # Spring Boot REST API
├── frontend/      # React application
├── docs/          # ER diagram and database schema
├── postman/       # Postman API collection
├── README.md
└── .gitignore
```

## Setup

### Prerequisites

- Java 17+
- Maven
- MySQL
- Node.js and npm

### Backend

Configure your MySQL username and password in:

```text
backend/src/main/resources/application.properties
```

Then run:

```bash
cd backend
mvn spring-boot:run
```

Backend runs on:

```text
http://localhost:8080
```

### Frontend

Open a new terminal and run:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on:

```text
http://localhost:5173
```

## API Testing

Import the Postman collection:

```text
postman/Banking-Management-System.postman_collection.json
```

The collection includes APIs for authentication, account management, deposits, withdrawals, fund transfers, beneficiaries, transactions, and admin operations.

## Documentation

- `docs/er-diagram.md` — Database ER diagram
- `docs/schema.sql` — Database schema

## Testing

Run backend tests using:

```bash
cd backend
mvn test
```

## Author

**Ajay Kumar Jaiswal**
