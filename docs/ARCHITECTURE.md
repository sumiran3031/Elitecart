# EliteCart — System Architecture

```mermaid
flowchart TB
    subgraph Client["Client"]
        Browser["Browser<br/>React 19 SPA (Vite)"]
    end

    subgraph Frontend["elitecart-frontend"]
        Nginx["Nginx<br/>(serves static build,<br/>SPA fallback routing)"]
    end

    subgraph Backend["elitecart-backend (Spring Boot 3)"]
        direction TB
        Filter["JwtAuthenticationFilter"]
        Controllers["REST Controllers<br/>(Auth, Product, Category, Cart,<br/>Wishlist, Order, Review, Admin*)"]
        Services["Service Layer<br/>(business logic, transactions)"]
        Repos["Spring Data JPA Repositories"]
        Mail["Spring Mail<br/>(async email sending)"]
    end

    subgraph Data["Data Layer"]
        MySQL[("MySQL 8<br/>elitecart_db")]
    end

    subgraph External["External"]
        SMTP["SMTP Provider<br/>(Gmail / SES / etc.)"]
    end

    Browser -->|"HTTPS"| Nginx
    Browser -->|"REST calls (Axios)<br/>Authorization: Bearer JWT"| Filter
    Filter --> Controllers
    Controllers --> Services
    Services --> Repos
    Repos -->|"Hibernate / JDBC"| MySQL
    Services -.->|"async"| Mail
    Mail --> SMTP

    style Browser fill:#EEF0FF,stroke:#6366F1
    style Nginx fill:#EEF0FF,stroke:#6366F1
    style MySQL fill:#FEF3C7,stroke:#F59E0B
    style SMTP fill:#FEF3C7,stroke:#F59E0B
```

## Request flow: authentication

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend (React)
    participant B as Backend (Spring Boot)
    participant DB as MySQL

    U->>F: Submit login form
    F->>B: POST /auth/login {email, password}
    B->>DB: Look up user, verify BCrypt hash
    DB-->>B: User row
    B->>B: Generate JWT access + refresh tokens
    B-->>F: 200 {accessToken, refreshToken, user}
    F->>F: Persist tokens to localStorage
    F-->>U: Redirect to intended page

    Note over F,B: On any 401 later, the Axios interceptor<br/>calls /auth/refresh-token once, retries the<br/>original request, and only redirects to /login<br/>if the refresh itself fails.
```

## Request flow: checkout

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant DB as MySQL
    participant PS as PaymentService (mock)
    participant Mail as EmailService (async)

    U->>F: Click "Place order"
    F->>B: POST /orders/checkout {shippingAddressId, billingAddressId, paymentMethod}
    B->>DB: Load cart, validate stock per item
    alt insufficient stock
        B-->>F: 400 Bad Request
    else stock OK
        B->>DB: Create Order + OrderItems (price snapshot)
        B->>DB: Deduct stock, increment unitsSold
        B->>PS: processPayment(order, method)
        PS-->>B: Payment (SUCCESS or PENDING for COD)
        B->>DB: Save payment, update order status
        B->>DB: Clear cart
        B-->>Mail: sendOrderConfirmationEmail (async)
        B-->>F: 201 Created {order}
        F-->>U: Redirect to order detail page
    end
```

## Layered architecture (per request)

```
Controller  →  validates input (Jakarta Validation), delegates to Service
    ↓
Service     →  business rules, transactions (@Transactional),
                orchestrates repositories + mappers + other services
    ↓
Repository  →  Spring Data JPA — no business logic, pure data access
    ↓
Database    →  MySQL, accessed via Hibernate
```

DTOs cross every boundary between Controller and Client — entities never
leave the Service layer. MapStruct (or hand-written mapper beans, where
composition across multiple mappers was clearer) convert entity ⇄ DTO in
both directions.
