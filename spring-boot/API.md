# API Documentation

This document provides details about the available API endpoints in the Spring Boot application.

## Base URL

All endpoints are prefixed with `/api`.

## Authentication Endpoints

### Register a new user

Register a new user in the system.

- **URL**: `/api/auth/signup`
- **Method**: `POST`
- **Auth required**: No

#### Request Body

```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "password": "securepassword"
}
```

#### Request Constraints

- `username`: Required, must be between 3 and 50 characters
- `email`: Required, must be a valid email format
- `password`: Required, must be between 6 and 100 characters

#### Response

- **Status Code**: 201 Created
- **Body**: None

#### Error Responses

- **Status Code**: 400 Bad Request
  - Username already exists
  - Email already in use
  - Validation errors (invalid email format, username/password length constraints)

---

### User Login

Authenticate a user and obtain a JWT token.

- **URL**: `/api/auth/login`
- **Method**: `POST`
- **Auth required**: No

#### Request Body

```json
{
  "usernameOrEmail": "johndoe",
  "password": "securepassword"
}
```

#### Request Constraints

- `usernameOrEmail`: Required, can be either username or email
- `password`: Required

#### Response

- **Status Code**: 200 OK
- **Body**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

#### Error Responses

- **Status Code**: 401 Unauthorized
  - Invalid username/email or password

---

### Get Current User Information

Retrieve information about the currently authenticated user.

- **URL**: `/api/auth/me`
- **Method**: `GET`
- **Auth required**: Yes

#### Request Headers

- `Authorization`: Bearer {JWT token}

#### Response

- **Status Code**: 200 OK
- **Body**:

```json
{
  "username": "johndoe",
  "email": "john.doe@example.com"
}
```

#### Error Responses

- **Status Code**: 401 Unauthorized
  - Missing or invalid JWT token
- **Status Code**: 403 Forbidden
  - User doesn't have required role

## Authentication

All protected endpoints require JWT authentication using the Bearer token scheme.

### JWT Token

- The JWT token is obtained by authenticating through the `/api/auth/login` endpoint
- Token expiration: 24 hours
- Token type: Bearer

### Using the Token

Include the token in the Authorization header for all protected requests:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```