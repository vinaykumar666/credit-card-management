# Class Catalog

One-line overview of major classes in each module. Generated for orientation; see source JavaDoc for details.

## cards-common

| Class | Module | What it does |
|-------|--------|--------------|
| CommonExceptionHandlerSupport | cards-common | Shared helpers that build standard `ErrorResponse` HTTP bodies for advice classes |
| YamlPropertySourceFactory | cards-common | Loads YAML into Spring `PropertySource` so error codes can bind |
| CardsCommonAutoConfiguration | cards-common | Spring config that loads `error-codes.yml` and registers error properties |
| ErrorResponseFactory | cards-common | Builds `ErrorResponse` from catalog codes plus correlation/tenant context |
| ErrorResponse | cards-common | Immutable standard API error body (status, code, message, path, ids) |
| BusinessException | cards-common | Sealed base for expected business failures with a catalog error code |
| NotFoundException | cards-common | Business failure when a resource is missing |
| ConflictException | cards-common | Business failure for conflicts (duplicates, etc.) |
| UnauthorizedException | cards-common | Business failure for authn/authz denials |
| ValidationBusinessException | cards-common | Business failure for domain/request validation with optional detail |
| DownstreamException | cards-common | Business failure when a downstream/external call fails |
| ErrorCodes | cards-common | Constants for shared catalog error code strings |
| ErrorCodeProperties | cards-common | Spring `@ConfigurationProperties` holder for the error catalog |
| ErrorCodeDefinition | cards-common | One catalog entry: HTTP status + default message |
| ChannelClientHolder | cards-common | Thread-local store for request-scoped channel/client context |
| ChannelClientContext | cards-common | Immutable channel, client, and correlation identity for a request |
| CorrelationConstants | cards-common | Shared header names and MDC keys for correlation/tenant fields |
| ApiError | cards-common | Simple mutable API error DTO |
| KafkaTopics | cards-common | Canonical Kafka topic name constants |
| PaymentCompletedEvent | cards-common | Kafka payload for a successful payment |
| PaymentFailedEvent | cards-common | Kafka payload for a failed payment |
| NotificationRequestedEvent | cards-common | Kafka payload requesting a notification send |

## cards-authentication-service

| Class | Module | What it does |
|-------|--------|--------------|
| AuthenticationServiceApplication | cards-authentication-service | Spring Boot entry point that starts the auth service |
| CorsConfig | cards-authentication-service | Allows local frontend/BFF origins to call auth APIs with CORS |
| CorrelationIdFilter | cards-authentication-service | Sets correlation/channel/client IDs on each request for logging and tracing |
| AuthController | cards-authentication-service | REST endpoints for register, login, refresh, and token validate |
| GlobalExceptionHandler | cards-authentication-service | Turns business/validation/unexpected errors into standard JSON error responses |
| AuthService | cards-authentication-service | Core auth logic: users, passwords, JWT access tokens, hashed refresh tokens |
| AuthorizationServerConfig | cards-authentication-service | Configures OAuth2/OIDC Authorization Server, clients, JWKS, and JWT encode/decode |
| SecurityConfig | cards-authentication-service | Default security chain: public auth paths, JWT resource server, password encoder |
| OAuth2TokenIssuer | cards-authentication-service | Issues RS256 JWTs for first-party login/register using the AS key material |
| OAuth2ClientProperties | cards-authentication-service | Binds `app.oauth2` TTL and client settings from YAML |
| RsaKeyProperties | cards-authentication-service | Holds/generates the RSA key pair used to sign and verify JWTs |
| RegisterRequest | cards-authentication-service | DTO for new-user registration (email, password, full name) |
| LoginRequest | cards-authentication-service | DTO for email/password login |
| RefreshRequest | cards-authentication-service | DTO carrying a refresh token for renewal |
| TokenResponse | cards-authentication-service | DTO returning access/refresh tokens and user identity |
| ValidateTokenRequest | cards-authentication-service | DTO carrying a JWT to validate |
| ValidateTokenResponse | cards-authentication-service | DTO returning validity plus user id/email/roles when valid |
| User | cards-authentication-service | JPA entity for users (credentials, profile, roles) |
| Role | cards-authentication-service | JPA entity for authorization roles |
| RefreshToken | cards-authentication-service | JPA entity for hashed refresh tokens with expiry and revoke flag |
| UserRepository | cards-authentication-service | Spring Data access for users by case-insensitive email |
| RoleRepository | cards-authentication-service | Spring Data access for roles by name |
| RefreshTokenRepository | cards-authentication-service | Spring Data access for active refresh tokens by hash |

## cards-bff-dashboard-service

| Class | Module | What it does |
|-------|--------|--------------|
| BffDashboardServiceApplication | cards-bff-dashboard-service | Spring Boot entry point for the BFF dashboard service |
| AppProperties | cards-bff-dashboard-service | Binds `app.*` config: gateway URL and channel/client allow-lists |
| CorsConfig | cards-bff-dashboard-service | CORS rules for local UI/gateway origins and exposed headers |
| CorrelationIdFilter | cards-bff-dashboard-service | Ensures every request has a correlation ID in MDC and the response |
| SecurityConfig | cards-bff-dashboard-service | JWT resource-server security; opens health/OpenAPI; protects `/bff/**` |
| TenantHeaderFilter | cards-bff-dashboard-service | Validates channel/client headers against allow-lists and sets tenant context |
| WebClientConfig | cards-bff-dashboard-service | Registers gateway WebClient and enables AppProperties |
| DownstreamClient | cards-bff-dashboard-service | Calls backend APIs via gateway with headers, circuit breaker, and fallbacks |
| DashboardService | cards-bff-dashboard-service | Aggregates dashboard data and proxies accounts/transactions/notifications/payments |
| BffController | cards-bff-dashboard-service | REST API under `/bff/v1` for dashboard and related UI operations |
| GlobalExceptionHandler | cards-bff-dashboard-service | Maps exceptions to consistent ErrorResponse JSON |
| AccountDto | cards-bff-dashboard-service | Credit account summary DTO for UI clients |
| DashboardResponse | cards-bff-dashboard-service | Aggregated dashboard payload (accounts, txs, notifications, tenant IDs) |
| InitiatePaymentRequest | cards-bff-dashboard-service | Validated request body for starting a payment |
| NotificationDto | cards-bff-dashboard-service | Notification record DTO for UI clients |
| PaymentDto | cards-bff-dashboard-service | Payment details DTO after create or lookup |
| TransactionDto | cards-bff-dashboard-service | Single account transaction DTO |
| TransactionHistoryDto | cards-bff-dashboard-service | Paged transaction history for one account |

## cards-api-gateway-service

| Class | Module | What it does |
|-------|--------|--------------|
| ApiGatewayServiceApplication | cards-api-gateway-service | Spring Boot entry point for the API gateway |
| SecurityConfig | cards-api-gateway-service | Reactive JWT security; permits actuator/fallback/auth paths |
| CorrelationIdGatewayFilter | cards-api-gateway-service | Propagates/creates correlation and tenant headers; optional tenant enforcement |
| AuthorizationGatewayFilter | cards-api-gateway-service | Optional Bearer presence check on non-public routes (off by default) |
| TokenValidator | cards-api-gateway-service | Interface for gateway Authorization-header validation |
| JwtTokenValidator | cards-api-gateway-service | Stub validator that only checks for a non-empty Bearer token |
| FallbackController | cards-api-gateway-service | `/fallback/*` endpoints returning GW_001 when downstream routes are down |

## cards-payment-service

| Class | Module | What it does |
|-------|--------|--------------|
| PaymentServiceApplication | cards-payment-service | Spring Boot entry point that starts the payment service |
| Payment | cards-payment-service | JPA entity for a payment row (amount, method, status, refs) |
| LedgerEntry | cards-payment-service | JPA entity for a ledger debit/credit linked to a payment |
| PaymentMethod | cards-payment-service | Enum of supported pay methods (CARD, UPI, NET_BANKING, EXTERNAL) |
| PaymentStatus | cards-payment-service | Enum of payment lifecycle states (PENDING, COMPLETED, FAILED) |
| PaymentRepository | cards-payment-service | Spring Data repo for Payment persistence |
| LedgerEntryRepository | cards-payment-service | Spring Data repo for LedgerEntry persistence |
| InitiatePaymentRequest | cards-payment-service | Validated API request to start a payment |
| PaymentResponse | cards-payment-service | API response DTO with payment state details |
| PaymentStrategy | cards-payment-service | Contract for method-specific payment processing |
| PaymentResult | cards-payment-service | Success/failure outcome from a payment strategy |
| PaymentStrategyFactory | cards-payment-service | Resolves the strategy bean for a payment method |
| CardPaymentStrategy | cards-payment-service | Simulates successful CARD payments |
| UpiPaymentStrategy | cards-payment-service | Simulates successful UPI payments |
| NetBankingPaymentStrategy | cards-payment-service | Simulates successful NET_BANKING payments |
| ExternalNetworkPaymentStrategy | cards-payment-service | Processes EXTERNAL payments via enterprise API |
| EnterpriseApiClient | cards-payment-service | Circuit-breaker HTTP client for enterprise authorize API |
| EnterpriseApiProperties | cards-payment-service | Config record for enterprise API base URL |
| EnterprisePaymentRequest | cards-payment-service | Payload sent to enterprise authorize API |
| EnterprisePaymentResponse | cards-payment-service | Approval/decline response from enterprise API |
| PaymentService | cards-payment-service | Orchestrates initiate/lookup, ledger writes, and Kafka events |
| PaymentController | cards-payment-service | REST endpoints to create and fetch payments |
| GlobalExceptionHandler | cards-payment-service | Maps exceptions to standard ErrorResponse bodies |
| CorrelationIdFilter | cards-payment-service | Sets correlation/channel/client ids on each request |
| SecurityConfig | cards-payment-service | JWT security chain; opens actuator and OpenAPI paths |
| WebClientConfig | cards-payment-service | Builds WebClient for the enterprise API |
| KafkaProducerConfig | cards-payment-service | Kafka producer factory and template (JSON values) |
| KafkaPaymentEventPublisher | cards-payment-service | Publishes completed/failed/notification Kafka events |

## cards-account-details-service

| Class | Module | What it does |
|-------|--------|--------------|
| AccountDetailsServiceApplication | cards-account-details-service | Boots the account details Spring Boot app |
| Account | cards-account-details-service | JPA entity for a credit-card account |
| Transaction | cards-account-details-service | JPA entity for an account transaction |
| AccountRepository | cards-account-details-service | Persists and looks up accounts by user/number |
| TransactionRepository | cards-account-details-service | Persists and pages transactions by account |
| AccountResponse | cards-account-details-service | API DTO returned for account details |
| CreateAccountRequest | cards-account-details-service | Validated request body to create an account |
| TransactionResponse | cards-account-details-service | API DTO for a single transaction |
| TransactionHistoryResponse | cards-account-details-service | Paged transaction history API response |
| AccountMapper | cards-account-details-service | Maps account entities ↔ DTOs |
| TransactionMapper | cards-account-details-service | Maps transaction entities to DTOs |
| AccountReadService | cards-account-details-service | Contract for account/transaction reads |
| AccountWriteService | cards-account-details-service | Contract for account creates |
| AccountReadServiceImpl | cards-account-details-service | Loads accounts and paged transactions |
| AccountWriteServiceImpl | cards-account-details-service | Creates accounts; rejects duplicate numbers |
| AccountController | cards-account-details-service | REST API for accounts and transactions |
| GlobalExceptionHandler | cards-account-details-service | Maps exceptions to standard error responses |
| CorrelationIdFilter | cards-account-details-service | Sets correlation/channel/client ids per request |
| SecurityConfig | cards-account-details-service | JWT security; permits actuator and OpenAPI |

## cards-notification-service

| Class | Module | What it does |
|-------|--------|--------------|
| NotificationServiceApplication | cards-notification-service | Boots the notification Spring Boot app |
| NotificationLog | cards-notification-service | JPA entity for a notification delivery log |
| NotificationLogRepository | cards-notification-service | Persists and lists notification logs |
| NotificationSender | cards-notification-service | Contract for channel-specific sending |
| EmailNotificationSender | cards-notification-service | Logs EMAIL send attempts |
| SmsNotificationSender | cards-notification-service | Logs SMS send attempts |
| PushNotificationSender | cards-notification-service | Logs PUSH send attempts |
| NotificationFactory | cards-notification-service | Resolves a sender by channel name |
| NotificationDispatchService | cards-notification-service | Sends notifications and updates log status |
| NotificationEventListener | cards-notification-service | Kafka listeners for payment/notification events |
| NotificationController | cards-notification-service | REST API to read notification logs |
| NotificationResponse | cards-notification-service | API DTO for a notification log |
| GlobalExceptionHandler | cards-notification-service | Maps exceptions to standard error responses |
| CorrelationIdFilter | cards-notification-service | Sets correlation/channel/client ids per request |
| SecurityConfig | cards-notification-service | JWT security; permits actuator and OpenAPI |

## cards-enterprise-api-service

| Class | Module | What it does |
|-------|--------|--------------|
| EnterpriseApiServiceApplication | cards-enterprise-api-service | Boots the enterprise API Spring Boot app |
| ExternalPaymentRequest | cards-enterprise-api-service | Proprietary external-network payment request |
| ExternalPaymentResponse | cards-enterprise-api-service | Proprietary external-network payment response |
| PaymentAuthorizeRequest | cards-enterprise-api-service | Platform request to authorize a payment |
| PaymentAuthorizeResponse | cards-enterprise-api-service | Platform response for payment authorization |
| ExternalNetworkClient | cards-enterprise-api-service | Simulates external/core-banking authorization |
| EnterprisePaymentAdapter | cards-enterprise-api-service | Translates platform ↔ external payment formats |
| EnterprisePaymentController | cards-enterprise-api-service | REST API for authorize and health-check |
| GlobalExceptionHandler | cards-enterprise-api-service | Maps exceptions to standard error responses |
| CorrelationIdFilter | cards-enterprise-api-service | Sets correlation/channel/client ids per request |
| SecurityConfig | cards-enterprise-api-service | JWT security; permits actuator and OpenAPI |
