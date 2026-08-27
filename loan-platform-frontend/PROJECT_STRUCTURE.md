# Loan Platform Frontend - Project Structure

## Complete Directory Overview

```
loan-platform-frontend/
├── src/
│   ├── app/
│   │   ├── core/                          # Core application functionality
│   │   │   ├── models/
│   │   │   │   └── auth.model.ts         # Authentication DTOs and interfaces
│   │   │   ├── services/
│   │   │   │   └── auth.service.ts       # Auth service with state management
│   │   │   ├── interceptors/
│   │   │   │   └── auth.interceptor.ts   # JWT token injection interceptor
│   │   │   └── guards/
│   │   │       └── auth.guard.ts         # Route protection guard
│   │   │
│   │   ├── features/                     # Feature modules
│   │   │   ├── auth/                     # Authentication module
│   │   │   │   ├── components/
│   │   │   │   │   ├── login/
│   │   │   │   │   │   ├── login.component.ts
│   │   │   │   │   │   ├── login.component.html
│   │   │   │   │   │   └── login.component.scss
│   │   │   │   │   └── register/
│   │   │   │   │       ├── register.component.ts
│   │   │   │   │       ├── register.component.html
│   │   │   │   │       └── register.component.scss
│   │   │   │   ├── auth.module.ts
│   │   │   │   └── auth-routing.module.ts
│   │   │   │
│   │   │   └── dashboard/                # Dashboard module
│   │   │       ├── pages/
│   │   │       │   └── dashboard/
│   │   │       │       ├── dashboard.component.ts
│   │   │       │       ├── dashboard.component.html
│   │   │       │       └── dashboard.component.scss
│   │   │       └── dashboard.module.ts
│   │   │
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   ├── app.component.scss
│   │   ├── app.module.ts
│   │   └── app-routing.module.ts
│   │
│   ├── environments/
│   │   ├── environment.ts                # Development config
│   │   └── environment.prod.ts           # Production config
│   │
│   ├── styles.scss                       # Global styles
│   ├── index.html
│   └── main.ts
│
├── dist/                                 # Build output (generated)
├── node_modules/                         # Dependencies (generated)
│
├── angular.json                          # Angular build config
├── tsconfig.json                         # TypeScript global config
├── tsconfig.app.json                     # TypeScript app config
├── tsconfig.spec.json                    # TypeScript test config
├── package.json                          # Dependencies
├── package-lock.json                     # Locked dependency versions
├── README.md                             # Project README
├── SETUP.md                              # Setup guide
└── PROJECT_STRUCTURE.md                  # This file
```

## Module Architecture

### Core Module (Non-exported)

Located in `src/app/core/`, contains:
- **Models**: TypeScript interfaces and DTOs
- **Services**: Business logic and API communication
- **Interceptors**: HTTP request/response processing
- **Guards**: Route protection logic

### Feature Modules

#### Auth Module
- **Purpose**: User authentication (login/register)
- **Route**: `/auth/*`
- **Public**: Yes (accessible without authentication)
- **Components**:
  - `LoginComponent`: User login form
  - `RegisterComponent`: User registration form

#### Dashboard Module
- **Purpose**: Main authenticated user area
- **Route**: `/dashboard/*`
- **Protected**: Yes (requires authentication via AuthGuard)
- **Components**:
  - `DashboardComponent`: Main dashboard page

## Layered Architecture

```
Presentation Layer
├── Components (login, register, dashboard)
└── Templates (HTML) & Styles (SCSS)
         ↓
Application Layer
├── Services (AuthService)
└── Routing (Guards)
         ↓
Infrastructure Layer
├── HTTP Interceptors
└── Core Models
         ↓
API Layer
└── Backend: http://localhost:8080/api
```

## Data Flow

### Authentication Flow
```
User Input (Component)
    ↓
Form Submission (Reactive Form)
    ↓
AuthService.login() / register()
    ↓
HTTP Request via AuthInterceptor
    ↓
Backend API
    ↓
JWT Token + User Data
    ↓
AuthService stores in localStorage & BehaviorSubject
    ↓
Component navigates to dashboard
    ↓
AuthGuard protects routes
```

### API Request Flow
```
Component calls Service
    ↓
Service makes HTTP request
    ↓
AuthInterceptor adds JWT token
    ↓
Request sent to Backend
    ↓
Response received
    ↓
Service processes and returns via Observable
    ↓
Component subscribes and updates view
```

## Routing Structure

```
/ (root)
├── /auth
│   ├── /login
│   └── /register
│
└── /dashboard (protected by AuthGuard)
    └── / (dashboard home)
```

## State Management

### Auth State (BehaviorSubject)
```typescript
AuthService.currentUser$: Observable<AuthUser | null>
```
- Managed by AuthService
- Persisted in localStorage
- Accessible across components via dependency injection
- RxJS-based reactive pattern

## File Naming Conventions

- **Components**: `*.component.ts`, `*.component.html`, `*.component.scss`
- **Modules**: `*.module.ts`
- **Routing**: `*-routing.module.ts`
- **Services**: `*.service.ts`
- **Guards**: `*.guard.ts`
- **Interceptors**: `*.interceptor.ts`
- **Models**: `*.model.ts`

## Dependency Injection

All services use Angular's constructor injection:

```typescript
constructor(
  private authService: AuthService,
  private http: HttpClient,
  private router: Router
) { }
```

## Configuration

### Environment Files
- `environment.ts`: Development settings
- `environment.prod.ts`: Production settings

Configure API endpoint:
```typescript
export const environment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080/api'
};
```

## Best Practices Implemented

1. **Lazy Loading**: Feature modules loaded on demand
2. **Reactive Programming**: RxJS Observables and Operators
3. **Type Safety**: Full TypeScript typing
4. **Single Responsibility**: Clear separation of concerns
5. **DRY Principle**: Reusable services and components
6. **Security**: HTTP interceptor for token management
7. **Error Handling**: Proper error handling in services
8. **Responsive Design**: Mobile-first SCSS styling
9. **Material Design**: Professional UI with Angular Material
10. **Component Isolation**: Each component has isolated styles and logic

## Performance Considerations

- **Tree Shaking**: Unused code removed in production builds
- **Code Splitting**: Lazy-loaded feature modules
- **Change Detection**: OnPush strategy can be added per component
- **Memory Management**: Proper subscription cleanup with takeUntil pattern

## Security Features

1. **JWT Token Management**: Stored in localStorage, added to requests
2. **HTTP Interceptor**: Handles token injection and 401 responses
3. **Route Guards**: Prevents access to protected routes
4. **CORS**: Backend CORS configuration required
5. **Input Validation**: Form validation before submission

## Extensibility

### Adding a New Feature Module

1. Create feature folder in `src/app/features/`
2. Generate module with routing
3. Add route to app routing module with lazy loading
4. Implement components and services

### Adding a New Service

1. Create in `src/app/core/services/`
2. Decorate with `@Injectable({ providedIn: 'root' })`
3. Inject in components as needed

### Adding a New Route Guard

1. Create in `src/app/core/guards/`
2. Implement `CanActivate` interface
3. Apply to routes in routing modules

## Build Artifacts

- **Development Build**: `dist/loan-platform-frontend/`
- **Development Size**: ~110 KB (gzipped)
- **Lazy Chunks**: Separate bundles for each feature module

## TypeScript Configuration

- **Target**: ES2022
- **Module**: ESNext
- **Strict Mode**: Enabled
- **Decorators**: Experimental decorators enabled

## Testing Setup

- **Unit Tests**: Jasmine framework
- **Test Runner**: Karma
- **Commands**:
  - `npm test`: Run tests
  - `ng test --run`: Single run
  - `ng test --code-coverage`: With coverage report

## Next Steps for Extension

1. Add Loan Management module
2. Implement loan application form
3. Add user profile management
4. Create admin dashboard
5. Add notifications module
6. Implement error handling pages (404, 500)
7. Add logging service
8. Setup unit and e2e tests
9. Configure CI/CD pipeline
10. Add PWA capabilities
