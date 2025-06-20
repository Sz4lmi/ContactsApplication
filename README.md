# Contacts Management System

A full-stack application for managing contact information with secure authentication and authorization.

## Overview

This application provides a comprehensive solution for storing and managing contact information. It features a secure authentication system, role-based access control, and a responsive user interface.

## Features

- User authentication with **JWT**
- Role-based access control (Admin and User roles)
- Contact management (create, read, update, delete)
- Store detailed contact information
- Responsive web interface
- RESTful API
- Database migrations with Flyway
- Containerized deployment with Docker

## Technology

### Backend

- Java 17
- Spring Boot 3.5.0
- Spring Security with JWT authentication
- Spring Data JPA
- PostgreSQL
- Flyway for database migrations
- Maven for dependency management
- Lombok for reducing boilerplate code
- Swagger/OpenAPI for API documentation

### Frontend

- Angular 19
- RxJS
- TypeScript
- NGINX for serving static content

### DevOps

- Docker and Docker Compose for containerization
- Multi-stage Docker builds for optimized images

## Getting Started

### Prerequisites

Docker and Docker Compose

### Running the Application

1. Clone the repository:
```
git clone https://github.com/Sz4lmi/ContactsApplication
cd <contacts>
```
2. Start the application using Docker Compose:
```
docker compose up -d
```
### Access the application:

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html
- Admin user:
  - username: admin
  - password: admin
- Normal users:
  - username: "test1", "test2", "test3", etc...
  - password: "123456" for all normal users

### Development Setup

Backend
1. Install JDK 17
2. Install Maven
3. Run PostgreSQL locally or use the Docker container
4. Configure application properties for local development
5. Run the Spring Boot application:
```
mvn spring-boot:run
```
Frontend

1. Install Node.js and npm
2. Navigate to the client directory:
```
cd <client>
```
3. Install dependencies:
```
npm install
```
5. Start the development server:
```
npm start
```
6. Access the frontend at http://localhost:4200

## API Endpoints

### Authentication

- POST /api/auth/login - Authenticate user and get JWT token
- POST /api/auth/users - Create a new user (admin only)
- GET /api/auth/users - Get all users (admin only)
- PUT /api/auth/users/{id} - Update a user (admin only)
- DELETE /api/auth/users/{id} - Delete a user (admin only)

### Contacts

- GET /api/contacts - Get all contacts for the authenticated user
- GET /api/contacts/{id} - Get a specific contact
- POST /api/contacts - Create a new contact
- PUT /api/contacts/{id} - Update a contact
- DELETE /api/contacts/{id} - Delete a contact

## Database Schema

The application uses the following main entities:

- User - Stores user authentication and authorization information
- Contact - Stores basic contact information
- PhoneNumber - Stores phone numbers associated with contacts
- Address - Stores addresses associated with contacts

## License
This project is licensed under the MIT License - see the LICENSE file for details.
