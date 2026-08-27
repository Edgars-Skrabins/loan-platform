# Loan Platform Frontend - Setup Guide

Professional Angular frontend for the Loan Platform application.

## Prerequisites

- Node.js (v18 or higher)
- npm (v9 or higher)
- Angular CLI v18

## Installation

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Environment

The application uses environment-specific configurations:

- **Development**: `src/environments/environment.ts`
- **Production**: `src/environments/environment.prod.ts`

Default API endpoint: `http://localhost:8080/api`

Update the `apiBaseUrl` in the environment files if your backend is running on a different URL.

## Development Server

### Start the development server

```bash
ng serve
```

The application will be available at `http://localhost:4200`

### Start with specific port

```bash
ng serve --port 4300
```

## Building

### Build for development

```bash
ng build
```

### Build for production

```bash
ng build --configuration production
```

Build artifacts will be stored in the `dist/` directory.

## Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── models/          # TypeScript models and interfaces
│   │   ├── services/        # Core services (Auth, etc.)
│   │   ├── interceptors/    # HTTP interceptors
│   │   └── guards/          # Route guards
│   ├── features/
│   │   ├── auth/            # Authentication module
│   │   │   ├── components/
│   │   │   └── services/
│   │   └── dashboard/       # Dashboard module
│   ├── app.module.ts        # Main app module
│   └── app-routing.module.ts # App routing
├── environments/            # Environment configurations
└── styles.scss             # Global styles
```

## Key Features

### Authentication Module
- User registration and login
- JWT token management
- HTTP interceptor for token injection
- Route guards for protected pages
- Form validation with Material
- Error handling with snackbars

### Dashboard Module
- Protected authenticated page
- User information display
- Quick stats cards
- Logout functionality

## Architecture Patterns

### Service Layer
- `AuthService`: Handles authentication logic and state management using RxJS BehaviorSubjects
- HTTP service calls with proper error handling

### Interceptors
- `AuthInterceptor`: Automatically adds JWT token to all HTTP requests
- Handles 401 responses by logging out user

### Guards
- `AuthGuard`: Protects routes requiring authentication
- Redirects to login page if not authenticated

### Reactive Forms
- Strong typing with interfaces
- Built-in validation
- Form state management

## Environment Variables

Configure the API endpoint in environment files:

```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api'
};
```

## Common Tasks

### Add a New Module

```bash
ng generate module features/loans --route loans --module app.module
```

### Add a New Component

```bash
ng generate component features/loans/components/loan-list
```

### Add a New Service

```bash
ng generate service core/services/loan
```

## Authentication Flow

1. User navigates to `/auth/login` or `/auth/register`
2. Credentials are sent to backend API
3. Backend returns JWT token and user info
4. Token is stored in localStorage and BehaviorSubject
5. AuthInterceptor adds token to all subsequent requests
6. Protected routes check AuthGuard
7. On 401 response, user is logged out and redirected

## Testing

Run unit tests:

```bash
ng test --run
```

Run e2e tests:

```bash
ng e2e
```

## Linting and Formatting

```bash
ng lint
```

## Troubleshooting

### CORS Errors
Ensure your backend has CORS configuration enabled for `http://localhost:4200`

### Login Not Working
1. Verify backend is running on `http://localhost:8080`
2. Check browser console for API errors
3. Verify credentials in database

### Token Expiration
The app will automatically logout on 401 responses. Consider implementing token refresh logic for production.

## Production Deployment

1. Build the application:
   ```bash
   ng build --configuration production
   ```

2. Serve the dist folder with your web server (nginx, Apache, etc.)

3. Update `environment.prod.ts` with production API endpoint

## Dependencies

### Core
- `@angular/core` - Angular framework
- `@angular/common` - Angular common utilities
- `@angular/router` - Angular routing

### Material Design
- `@angular/material` - Material Design components
- `@angular/cdk` - Component Dev Kit

### Forms & Validation
- `@angular/forms` - Reactive Forms

### HTTP
- `@angular/common/http` - HTTP client

## Code Style

This project follows Angular style guide conventions:
- Camel case for variables and functions
- PascalCase for classes and components
- SCSS for component styles
- Reactive programming with RxJS
- Strong typing with TypeScript

## Additional Resources

- [Angular Documentation](https://angular.io)
- [Angular Material](https://material.angular.io)
- [TypeScript Documentation](https://www.typescriptlang.org)
- [RxJS Documentation](https://rxjs.dev)

## Support

For issues or questions, refer to the backend repository or create an issue in this project.
