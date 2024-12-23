<h1 align="center">
   <br>
   Library Management System
   <br>
</h1>

[![Spring Boot](https://img.shields.io/badge/Spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white)]()
[![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)]()
[![MySQL](https://img.shields.io/badge/MySQL-00000F?style=for-the-badge&logo=mysql&logoColor=white)]()
[![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)]()
[![Maven](https://img.shields.io/badge/apache_maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)]()
[![Bootstrap](https://img.shields.io/badge/Bootstrap-563D7C?style=for-the-badge&logo=bootstrap&logoColor=white)]()

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=emsi-5iir_library-management-system&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=emsi-5iir_library-management-system)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=emsi-5iir_library-management-system&metric=coverage)](https://sonarcloud.io/summary/new_code?id=emsi-5iir_library-management-system)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=emsi-5iir_library-management-system&metric=bugs)](https://sonarcloud.io/summary/new_code?id=emsi-5iir_library-management-system)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=emsi-5iir_library-management-system&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=emsi-5iir_library-management-system)

A full stack Library Management System built with Spring Boot, Angular and MySQL.

## Features

- JWT Authentication and Role-based Authorization (Admin/User)
- Book Management (CRUD operations)
- User Management
- Borrowing System
- Performance Testing
- API Security
- Password Encryption (BCrypt)
- Role-based Access Control

## Tech Stack

### Backend
- Spring Boot 2.7.0
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- JUnit 5
- JMeter
- SonarQube

### Frontend
- Angular 13
- Angular Material
- Bootstrap 5

## Setup & Installation

### Prerequisites

- Java 8+
- Node.js 14+
- MySQL 8+
- Maven 3.6+

### Database Configuration

```yml
server.port = yourPreferredPortNumber
spring.datasource.url = jdbc:mysql://localhost:3306/yourSchemaName
spring.datasource.username = yourUsername
spring.datasource.password = yourPassword
#spring.datasource.password=mysql
spring.jpa.show-sql = true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.hibernate.ddl-auto=update
hibernate.format_sql=true;

app.base-url=http://localhost:4200
spring.main.allow-bean-definition-overriding=true
spring.main.allow-circular-references=true
```

### Backend Setup
```bash
# Install dependencies
mvn install

# Run application
mvn spring-boot:run
```

### Frontend Setup

```bash
# Install dependencies
npm install

# Run application
ng serve
```

### Testing

```bash
#jMeter Performance Test
mvn test -Dtest=LoginPerformanceTest

# Load Test (50 concurrent users)
mvn test -Dtest=BookOperationsPerformanceTest#performBookOperationsLoadTest

# Stress Test (100 concurrent users)
mvn test -Dtest=BookOperationsPerformanceTest#performBookOperationsStressTest

#Seleninum Test
mvn test -Dtest=LoginPageTest

#Unit Tests
mvn test -X -Dtest=com.sos.lms.controller.**
mvn test -Dtest=com.sos.lms.controller.** -Dsurefire.includeBaseDirInNames=true

```

## API Endpoints

### Authentication
```json
POST /authenticate
{
   "username": "user",
   "password": "password"
}
```

### Book Operations
```json
// Get all books
GET /admin/books

// Get book by ID
GET /admin/books/{id}

// Create book
POST /admin/books
{
    "bookName": "Book Name",
    "bookAuthor": "Author Name",
    "bookGenre": "Genre",
    "noOfCopies": 5
}

// Update book
PUT /admin/books/{id}
{
    "bookName": "New Book Name",
    "bookAuthor": "Author Name",
    "bookGenre": "Genre",
    "noOfCopies": 7
}

// Delete book
DELETE /admin/books/{id}
```

### User Operations
```json
// Create user
POST /admin/users
{
    "username": "user",
    "name": "First User",
    "password": "password",
    "role": [
        {
            "roleName": "Admin"
        }
    ]
}

// Update user
PUT /admin/users/{id}
{
    "username": "user",
    "name": "New First User",
    "password": "password",
    "role": [
        {
            "roleName": "User"
        }
    ]
}
```

### Borrow Operations

```json
// Borrow book
POST /borrow
{
    "bookId": 3,
    "userId": 5
}

// Return book
POST /return
{
    "borrowId": 1
}

// Get user's borrowed books
GET /myborrowed

// Get book's borrowing history
GET /borrow/book/{bookId}

// Get user's borrowing history
GET /borrow/user/{userId}
```

## Contributors

- Oussama KORCHI
- Souhail ELKAHZA
- Salma SKALLI
