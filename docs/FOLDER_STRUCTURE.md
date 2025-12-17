# Finance Wallet API - Package Structure

## Complete Directory Layout

```
finance-wallet-api/
│
├── src/main/kotlin/com/financewallet/api/
│   │
│   ├── FinanceWalletApiApplication.kt          # Main application class
│   │
│   ├── config/                                  # Configuration classes
│   │   ├── SecurityConfig.kt                   # Spring Security configuration
│   │   ├── JwtConfig.kt                        # JWT configuration
│   │   ├── CorsConfig.kt                       # CORS configuration
│   │   ├── OpenApiConfig.kt                    # Swagger/OpenAPI config
│   │   ├── ModelMapperConfig.kt                # ModelMapper bean
│   │   └── WebConfig.kt                        # General web config
│   │
│   ├── entity/                                  # JPA Entities (already created)
│   │   ├── User.kt
│   │   ├── RefreshToken.kt
│   │   ├── Account.kt
│   │   ├── AccountType.kt
│   │   ├── Transaction.kt
│   │   ├── TransactionAttachment.kt
│   │   ├── Category.kt
│   │   ├── Currency.kt
│   │   ├── ExchangeRate.kt
│   │   ├── Tag.kt
│   │   ├── Budget.kt
│   │   ├── Goal.kt
│   │   ├── RecurringTransaction.kt
│   │   ├── UserPreference.kt
│   │   └── SyncLog.kt
│   │
│   ├── dto/                                     # Data Transfer Objects
│   │   ├── request/                            # Request DTOs
│   │   │   ├── auth/
│   │   │   │   ├── LoginRequest.kt
│   │   │   │   ├── RegisterRequest.kt
│   │   │   │   ├── RefreshTokenRequest.kt
│   │   │   │   └── OAuthLoginRequest.kt
│   │   │   ├── account/
│   │   │   │   ├── CreateAccountRequest.kt
│   │   │   │   └── UpdateAccountRequest.kt
│   │   │   ├── transaction/
│   │   │   │   ├── CreateTransactionRequest.kt
│   │   │   │   ├── UpdateTransactionRequest.kt
│   │   │   │   └── TransferRequest.kt
│   │   │   ├── category/
│   │   │   │   └── CreateCategoryRequest.kt
│   │   │   └── budget/
│   │   │       └── CreateBudgetRequest.kt
│   │   │
│   │   └── response/                           # Response DTOs
│   │       ├── auth/
│   │       │   ├── LoginResponse.kt
│   │       │   ├── RegisterResponse.kt
│   │       │   └── UserResponse.kt
│   │       ├── account/
│   │       │   ├── AccountResponse.kt
│   │       │   └── AccountSummaryResponse.kt
│   │       ├── transaction/
│   │       │   ├── TransactionResponse.kt
│   │       │   └── TransactionListResponse.kt
│   │       ├── category/
│   │       │   └── CategoryResponse.kt
│   │       ├── budget/
│   │       │   └── BudgetResponse.kt
│   │       ├── common/
│   │       │   ├── ApiResponse.kt              # Generic API response wrapper
│   │       │   ├── PageResponse.kt             # Paginated response
│   │       │   └── ErrorResponse.kt            # Error response structure
│   │       └── dashboard/
│   │           ├── DashboardResponse.kt
│   │           └── StatisticsResponse.kt
│   │
│   ├── repository/                              # JPA Repositories
│   │   ├── UserRepository.kt
│   │   ├── RefreshTokenRepository.kt
│   │   ├── AccountRepository.kt
│   │   ├── AccountTypeRepository.kt
│   │   ├── TransactionRepository.kt
│   │   ├── CategoryRepository.kt
│   │   ├── CurrencyRepository.kt
│   │   ├── ExchangeRateRepository.kt
│   │   ├── TagRepository.kt
│   │   ├── BudgetRepository.kt
│   │   ├── GoalRepository.kt
│   │   ├── RecurringTransactionRepository.kt
│   │   └── SyncLogRepository.kt
│   │
│   ├── service/                                 # Business Logic
│   │   ├── auth/
│   │   │   ├── AuthService.kt
│   │   │   ├── JwtService.kt
│   │   │   ├── UserDetailsServiceImpl.kt
│   │   │   └── OAuth2Service.kt
│   │   ├── account/
│   │   │   └── AccountService.kt
│   │   ├── transaction/
│   │   │   ├── TransactionService.kt
│   │   │   └── TransferService.kt
│   │   ├── category/
│   │   │   └── CategoryService.kt
│   │   ├── currency/
│   │   │   ├── CurrencyService.kt
│   │   │   └── ExchangeRateService.kt
│   │   ├── budget/
│   │   │   └── BudgetService.kt
│   │   ├── goal/
│   │   │   └── GoalService.kt
│   │   ├── recurring/
│   │   │   └── RecurringTransactionService.kt
│   │   ├── sync/
│   │   │   └── SyncService.kt
│   │   ├── dashboard/
│   │   │   └── DashboardService.kt
│   │   └── file/
│   │       └── FileStorageService.kt
│   │
│   ├── controller/                              # REST Controllers
│   │   ├── AuthController.kt                   # /api/auth/*
│   │   ├── AccountController.kt                # /api/accounts/*
│   │   ├── TransactionController.kt            # /api/transactions/*
│   │   ├── CategoryController.kt               # /api/categories/*
│   │   ├── CurrencyController.kt               # /api/currencies/*
│   │   ├── BudgetController.kt                 # /api/budgets/*
│   │   ├── GoalController.kt                   # /api/goals/*
│   │   ├── RecurringTransactionController.kt   # /api/recurring/*
│   │   ├── DashboardController.kt              # /api/dashboard/*
│   │   ├── SyncController.kt                   # /api/sync/*
│   │   └── UserController.kt                   # /api/users/*
│   │
│   ├── security/                                # Security components
│   │   ├── JwtAuthenticationFilter.kt          # JWT filter
│   │   ├── JwtAuthenticationEntryPoint.kt      # Handle auth errors
│   │   ├── UserPrincipal.kt                    # Current user details
│   │   └── OAuth2UserService.kt                # OAuth2 user service
│   │
│   ├── exception/                               # Exception handling
│   │   ├── GlobalExceptionHandler.kt           # @ControllerAdvice
│   │   ├── ResourceNotFoundException.kt
│   │   ├── BadRequestException.kt
│   │   ├── UnauthorizedException.kt
│   │   ├── InsufficientBalanceException.kt
│   │   └── DuplicateResourceException.kt
│   │
│   ├── util/                                    # Utility classes
│   │   ├── DateUtils.kt
│   │   ├── CurrencyUtils.kt
│   │   ├── ValidationUtils.kt
│   │   └── PasswordEncoder.kt
│   │
│   └── constant/                                # Constants
│       ├── AppConstants.kt
│       ├── SecurityConstants.kt
│       └── MessageConstants.kt
│
├── src/main/resources/
│   ├── application.properties                   # Main config (already created)
│   ├── application-dev.properties              # Development profile
│   ├── application-prod.properties             # Production profile
│   ├── db/
│   │   └── migration/                          # Flyway migrations (optional)
│   │       └── V1__initial_schema.sql
│   └── static/
│       └── uploads/                            # File upload directory
│
├── src/test/kotlin/com/financewallet/api/
│   ├── controller/                              # Controller tests
│   ├── service/                                 # Service tests
│   ├── repository/                              # Repository tests
│   └── integration/                             # Integration tests
│
├── build.gradle.kts                             # Gradle build (already created)
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
└── README.md
```

## Key Package Explanations

### 1. **entity/** - Domain Models
- JPA entities representing database tables
- Include relationships, constraints, and validations
- Use Kotlin data classes for conciseness

### 2. **dto/** - Data Transfer Objects
- **request/**: DTOs for incoming API requests
- **response/**: DTOs for outgoing API responses
- Separate from entities to control what data is exposed
- Include validation annotations (@NotNull, @Email, etc.)

### 3. **repository/** - Data Access Layer
- Extend JpaRepository or CrudRepository
- Custom query methods using Spring Data JPA
- @Query annotations for complex queries

### 4. **service/** - Business Logic
- Contains all business rules and logic
- Orchestrates between repositories
- Handles transactions with @Transactional
- Separated by domain (auth, account, transaction, etc.)

### 5. **controller/** - REST API Layer
- Exposes REST endpoints
- Handles HTTP requests/responses
- Uses @RestController, @RequestMapping
- Validates input, delegates to services
- Returns ResponseEntity with proper status codes

### 6. **security/** - Authentication & Authorization
- JWT token generation and validation
- OAuth2 integration
- Spring Security filters
- Custom user details service

### 7. **exception/** - Error Handling
- Custom exception classes
- Global exception handler with @ControllerAdvice
- Consistent error response structure

### 8. **config/** - Configuration Beans
- Spring configuration classes
- Security, CORS, JWT, Swagger, etc.
- All @Configuration annotated classes

### 9. **util/** - Helper Classes
- Reusable utility methods
- Date formatting, currency conversion, etc.
- No business logic

### 10. **constant/** - Application Constants
- All constant values in one place
- Error messages, status codes, etc.

## Naming Conventions

### Files
- **Entity**: `User.kt`, `Account.kt`
- **DTO**: `LoginRequest.kt`, `AccountResponse.kt`
- **Repository**: `UserRepository.kt`
- **Service**: `AuthService.kt`, `AccountService.kt`
- **Controller**: `AuthController.kt`

### API Endpoints Pattern
```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/oauth2/{provider}

GET    /api/accounts
POST   /api/accounts
GET    /api/accounts/{id}
PUT    /api/accounts/{id}
DELETE /api/accounts/{id}

GET    /api/transactions
POST   /api/transactions
GET    /api/transactions/{id}
PUT    /api/transactions/{id}
DELETE /api/transactions/{id}
POST   /api/transactions/transfer

GET    /api/categories
POST   /api/categories
GET    /api/categories/{id}
PUT    /api/categories/{id}
DELETE /api/categories/{id}

GET    /api/dashboard
GET    /api/dashboard/statistics
```

## Development Order Recommendation

1. ✅ **Database Schema** (Done)
2. ✅ **Entities** (Done)
3. ✅ **Configuration** (Done)
4. **Repositories** - Simple interfaces
5. **Security Configuration** - JWT + OAuth2
6. **DTOs** - Request and Response objects
7. **Services** - Business logic
8. **Controllers** - REST endpoints
9. **Exception Handling** - Global error handler
10. **Testing** - Unit and integration tests

This structure follows **Clean Architecture** and **SOLID principles**, making it easy to maintain and scale!