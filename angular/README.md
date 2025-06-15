# Spring Boot Angular Playground - Frontend

This is the Angular frontend for the Spring Boot Angular Playground application.

## Features

- JWT-based authentication
- Login and registration forms with validation
- Protected routes using auth guards
- Material UI components for modern look and feel

## Getting Started

### Prerequisites

- Node.js (v14 or higher)
- npm (v6 or higher)

### Installation

1. Navigate to the angular directory:
   ```
   cd angular
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Start the development server:
   ```
   npm start
   ```

4. The application will be available at `http://localhost:4200`

## Building for Production

To build the application for production:

```
npm run build
```

The build artifacts will be stored in the `dist/angular` directory.

## Project Structure

- `src/app/auth`: Authentication components (login, register)
- `src/app/core`: Core functionality (services, interceptors, guards)
- `src/app/home`: Home page component
- `src/app/shared`: Shared components (header)

## API Integration

The application communicates with the Spring Boot backend API. See the `API.md` file in the spring-boot directory for details on the available endpoints.
