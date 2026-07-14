# EliteCart — Backend (Phase 1–4: Foundation → Auth → Catalog → Orders → Analytics)

Professional e-commerce platform backend built with **Java 21 + Spring Boot 3**.

This covers **Phases 1–4 of 6** of the full EliteCart build: a complete, runnable
foundation (schema, security, auth), the full Product and Category modules,
the full shopping flow (Cart, Wishlist, Addresses, Orders, mock Payments), and
now Product Reviews plus the Admin Dashboard/Analytics APIs — all wired end to
end, not stubs.

## What's included in this phase

- **15 JPA entities** covering the full schema: `User`, `Role`, `Address`, `Category`,
  `Product`, `ProductImage`, `Cart`, `CartItem`, `Wishlist`, `WishlistItem`, `Order`,
  `OrderItem`, `Payment`, `Review`, `EmailLog` — with proper relationships, cascades,
  lazy loading, and indexes.
- **15 Spring Data JPA repositories**, one per entity, with the specific finder
  methods needed by later modules (e.g. `ProductRepository` already extends
  `JpaSpecificationExecutor` for dynamic filtering in Phase 2).
- **JWT authentication end-to-end**: `JwtUtil`, `JwtAuthenticationFilter`,
  `JwtAuthenticationEntryPoint`, `CustomUserDetailsService`, `UserPrincipal`,
  and a full `SecurityConfig` with stateless sessions, BCrypt (strength 12),
  CORS, and role-based route rules (`/admin/**` → `ROLE_ADMIN`).
- **Complete Auth module**: register, email verification, login (with
  remember-me), refresh token, logout, forgot password, reset password,
  change password — controller → service interface → service impl → DTOs
  → mapper, all wired together.
- **Email service** with responsive HTML templates (gradient header, styled
  CTA button) for verification and password reset emails, logged to the
  `email_logs` table on every send (success or failure).
- **Global exception handling** — one `@RestControllerAdvice` mapping every
  exception (not-found, bad request, duplicate, invalid token, bad
  credentials, validation errors, access denied, 500s) to a consistent JSON
  shape.
- **Swagger / OpenAPI 3** wired up with a bearer-token security scheme, so
  every protected endpoint can be tested directly from `/swagger-ui.html`.
- **Auto-seeded data**: `ROLE_ADMIN` / `ROLE_CUSTOMER` and a default admin
  account are created on first boot — no manual SQL needed.
- **Unit tests** (JUnit 5 + Mockito) for the registration flow as a working
  example of the testing pattern used throughout the project.
- **Docker**: multi-stage `Dockerfile` for the backend, plus a root
  `docker-compose.yml` wiring MySQL + backend + a frontend placeholder
  (the frontend service will build for real once Phase 5 lands).

## Phase 2 additions — Product & Category modules

- **Category module**: full CRUD (admin-only), nested/parent-child
  hierarchy, auto-generated + collision-safe slugs, flat listing and a
  recursive tree endpoint, product-count per category, and a guard that
  blocks deleting a category that still has products.
- **Product module**: full CRUD (admin-only) with multiple images per
  product (primary flag + display order), SKU uniqueness, discount pricing
  with an `effectivePrice` computed field, stock quantity, rating/review
  count, featured flag, and units-sold (for best-sellers).
- **Dynamic search/filter/sort/pagination** via a single `GET /products`
  endpoint backed by a JPA `Specification` — combine keyword search
  (name/description/SKU), category, min/max price, featured, in-stock, and
  minimum rating in any combination, plus standard Spring `Pageable` sorting
  (`?sort=price,asc`).
- **Curated listings**: `/products/featured`, `/products/latest`,
  `/products/best-sellers`.
- Public read endpoints (`/products/**`, `/categories/**`) vs. admin-only
  write endpoints (`/admin/products/**`, `/admin/categories/**`) are
  cleanly separated at the controller level, matching the existing
  `SecurityConfig` rule that locks `/admin/**` to `ROLE_ADMIN`.
- Unit tests for the SKU-duplicate and category-not-found guard rails.

## Phase 3 additions — Cart, Wishlist, Addresses, Orders & Payments

- **Cart module**: add/update/remove items with a live stock check, computed
  subtotal/tax (8%)/shipping (flat fee, free over $100)/grand total, clear
  cart. Adding an already-present product increments its quantity instead
  of duplicating the row.
- **Wishlist module**: add/remove products (duplicate-guarded), and a
  **move-to-cart** endpoint that adds the item to the cart and removes it
  from the wishlist in one call.
- **Address module**: full CRUD for a customer's own shipping/billing
  addresses, with ownership checks and "only one default per address type"
  enforcement.
- **Order module / checkout**: converts the cart into an order — validates
  stock for every line item up front, snapshots product name/price into
  `OrderItem` (so future price changes never rewrite history), deducts
  stock, tracks units sold (feeds the Phase 2 best-sellers endpoint),
  processes a mock payment, clears the cart, and sends an HTML order
  confirmation email — all in one transaction.
  - Order numbers are generated as `ORD-YYYYMMDD-XXXXXXXX`.
  - Customers can view paginated order history, order detail by id or
    order number, and cancel while the order is still `PENDING`/`CONFIRMED`
    (which restocks the items).
  - Admins can list all orders and transition status through
    `PENDING → CONFIRMED → PACKED → SHIPPED → DELIVERED` (or `CANCELLED`),
    automatically emailing the customer on `SHIPPED` and `DELIVERED`.
- **Mock Payment gateway**: generates a transaction id, "charges" the order
  total, and marks CARD/UPI/NET_BANKING/WALLET as `SUCCESS` immediately or
  COD as `PENDING`. A separate mock **refund** endpoint flips a successful
  payment to `REFUNDED`. Built behind a `PaymentService` interface so it can
  be swapped for a real Stripe/Razorpay integration later without touching
  `OrderService`.
- **Correctness fix carried through the whole codebase**: since
  `spring.jpa.open-in-view` is disabled, every read method that touches a
  lazy association (cart items, wishlist items, order items, category
  children, product images, etc.) is explicitly annotated
  `@Transactional(readOnly = true)` so it can't throw
  `LazyInitializationException` in production.
- Unit tests for the insufficient-stock guard on `POST /cart/items`.

## Phase 4 additions — Reviews & Admin Dashboard/Analytics

- **Review module**: one review per customer per product (duplicate-guarded),
  paginated public reads, owner-or-admin delete. Every add/delete
  recomputes the product's aggregate `rating` and `reviewCount` via a
  dedicated JPQL average/count query, so the Phase 2 product listing and
  filters (`minRating`) always reflect live data.
  - Kept at `/reviews` (not nested under `/products/**`) specifically so the
    write endpoints can't accidentally inherit the public-read rule — only
    `GET /reviews/product/**` is explicitly whitelisted in `SecurityConfig`.
- **Admin Dashboard/Analytics module** (`/admin/analytics/dashboard`):
  total revenue (excludes cancelled orders), total orders, total products,
  total customers, the 10 most recent orders, the top 5 products by units
  sold, a 12-month revenue trend (native `DATE_FORMAT` grouping — ready to
  feed straight into a Recharts line/bar chart), and revenue-by-category
  (ready for a Recharts pie chart).
- **CSV export** (`/admin/analytics/orders/export`): streams every order
  (number, date, customer email, status, total) as a downloadable
  `elitecart-orders.csv`.
- Unit test for the duplicate-review guard rail.

## Tech stack (this phase)

Java 21 · Spring Boot 3.3 · Spring Security 6 · JWT (jjwt 0.12) · Spring Data
JPA / Hibernate · MySQL 8 · Lombok · MapStruct · springdoc-openapi · Jakarta
Validation · Spring Mail · JUnit 5 · Mockito · Docker

## Project structure

```
elitecart-backend/
├── src/main/java/com/elitecart/backend/
│   ├── config/          # SecurityConfig, OpenApiConfig, JpaAuditingConfig, WebConfig, DataSeeder
│   ├── controller/       # AuthController, ProductController, CategoryController,
│   │                     # AdminProductController, AdminCategoryController, CartController,
│   │                     # WishlistController, AddressController, OrderController,
│   │                     # AdminOrderController, AdminPaymentController, ReviewController,
│   │                     # AdminAnalyticsController
│   ├── dto/
│   │   ├── auth/         # Register/Login/Auth/User/RefreshToken/ForgotPassword/ResetPassword/ChangePassword
│   │   ├── category/     # CategoryRequest, CategoryResponse
│   │   ├── product/      # ProductRequest, ProductResponse, ProductImageDto, ProductSearchCriteria
│   │   ├── cart/         # AddCartItemRequest, UpdateCartItemRequest, CartItemResponse, CartResponse
│   │   ├── wishlist/     # AddWishlistItemRequest, WishlistItemResponse, WishlistResponse
│   │   ├── address/      # AddressRequest, AddressResponse
│   │   ├── order/        # CheckoutRequest, CancelOrderRequest, UpdateOrderStatusRequest,
│   │   │                 # OrderItemResponse, OrderResponse
│   │   ├── payment/      # PaymentResponse
│   │   ├── review/       # ReviewRequest, ReviewResponse
│   │   ├── analytics/    # DashboardStatsResponse, MonthlySalesPoint, CategorySalesPoint
│   │   └── common/       # ApiResponse<T> envelope
│   ├── entity/           # 15 JPA entities + enums (RoleName, OrderStatus)
│   ├── exception/        # Custom exceptions + GlobalExceptionHandler + ErrorResponse
│   ├── mapper/           # UserMapper, ProductMapper, CategoryMapper, CartMapper, WishlistMapper,
│   │                     # AddressMapper, OrderMapper, PaymentMapper, ReviewMapper
│   ├── repository/       # 15 Spring Data JPA repositories (+ analytics queries on Order/User/Review)
│   ├── security/         # JwtUtil, JwtAuthenticationFilter, JwtAuthenticationEntryPoint,
│   │                     # UserPrincipal, CustomUserDetailsService
│   ├── specification/    # ProductSpecification (dynamic search/filter)
│   ├── service/          # Service interfaces (Auth, Email, Product, Category, Cart,
│   │                     # Wishlist, Address, Order, Payment, Review, Analytics)
│   │   └── impl/         # Implementations
│   ├── util/             # SlugUtil
│   └── EliteCartBackendApplication.java
├── src/test/java/...     # AuthServiceImplTest, ProductServiceImplTest, CartServiceImplTest,
│                         # ReviewServiceImplTest
├── pom.xml
├── Dockerfile
├── .dockerignore
├── .env.example
└── .gitignore
```

## Running locally

### Option A — Maven + local MySQL

```bash
# 1. Create a MySQL database (or let ddl-auto create it for you)
mysql -u root -p -e "CREATE DATABASE elitecart_db;"

# 2. Copy env template and fill in real values
cp .env.example .env

# 3. Run
mvn spring-boot:run
```

The API will be available at `http://localhost:8080/api`.
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### Option B — Docker

From the **repository root** (one level above `elitecart-backend/`):

```bash
docker-compose up --build
```

This starts MySQL, then the backend on port `8080`. (The `frontend` service in
`docker-compose.yml` will start working once the React app is generated in
Phase 5 — for now you can comment it out if you only want the backend.)

### Default admin login (auto-seeded)

```
email:    admin@elitecart.com
password: Admin@123
```

⚠️ Change `ADMIN_EMAIL` / `ADMIN_PASSWORD` in your `.env` before deploying anywhere real.

## Auth API quick reference

| Method | Endpoint                     | Auth required | Description                          |
|--------|-------------------------------|:--:|---------------------------------------|
| POST   | `/auth/register`              | No | Create a customer account             |
| POST   | `/auth/verify-email?token=`   | No | Verify email from the link sent       |
| POST   | `/auth/login`                 | No | Get access + refresh tokens           |
| POST   | `/auth/refresh-token`         | No | Exchange refresh token for new pair   |
| POST   | `/auth/logout`                | Yes| Invalidate the stored refresh token   |
| POST   | `/auth/forgot-password`       | No | Send password reset email             |
| POST   | `/auth/reset-password`        | No | Set new password using reset token    |
| POST   | `/auth/change-password`       | Yes| Change password while logged in       |

## Product & Category API quick reference

| Method | Endpoint                          | Auth | Description                              |
|--------|-------------------------------------|:--:|-------------------------------------------|
| GET    | `/products`                        | No | Search/filter/sort/paginate               |
| GET    | `/products/{id}`                   | No | Get by id                                 |
| GET    | `/products/slug/{slug}`            | No | Get by slug                               |
| GET    | `/products/featured`               | No | Up to 10 featured products                |
| GET    | `/products/latest`                 | No | Up to 10 newest products                  |
| GET    | `/products/best-sellers`           | No | Up to 10 best sellers by units sold       |
| POST   | `/admin/products`                  | Admin | Create product                          |
| PUT    | `/admin/products/{id}`             | Admin | Update product                          |
| DELETE | `/admin/products/{id}`             | Admin | Delete product                          |
| GET    | `/categories`                      | No | Flat list of all categories               |
| GET    | `/categories/tree`                 | No | Nested parent → children tree             |
| GET    | `/categories/{id}`                 | No | Get by id                                 |
| GET    | `/categories/slug/{slug}`          | No | Get by slug                               |
| POST   | `/admin/categories`                | Admin | Create category (optionally nested)     |
| PUT    | `/admin/categories/{id}`           | Admin | Update category                         |
| DELETE | `/admin/categories/{id}`           | Admin | Delete category (blocked if has products)|

## Cart, Wishlist, Address & Order API quick reference

| Method | Endpoint                              | Auth  | Description                                  |
|--------|-----------------------------------------|:---:|-------------------------------------------------|
| GET    | `/cart`                                | Customer | View cart with computed totals            |
| POST   | `/cart/items`                          | Customer | Add item (or bump quantity)                |
| PUT    | `/cart/items/{itemId}`                 | Customer | Set exact quantity                         |
| DELETE | `/cart/items/{itemId}`                 | Customer | Remove one item                            |
| DELETE | `/cart`                                | Customer | Clear cart                                 |
| GET    | `/wishlist`                            | Customer | View wishlist                              |
| POST   | `/wishlist/items`                      | Customer | Add product                                |
| DELETE | `/wishlist/items/{itemId}`             | Customer | Remove product                             |
| POST   | `/wishlist/items/{itemId}/move-to-cart`| Customer | Move item to cart                          |
| GET    | `/addresses`                           | Customer | List own addresses                         |
| POST   | `/addresses`                           | Customer | Add address                                |
| PUT    | `/addresses/{id}`                      | Customer | Update address                             |
| DELETE | `/addresses/{id}`                      | Customer | Delete address                             |
| POST   | `/orders/checkout`                     | Customer | Place order from cart (mock payment)       |
| GET    | `/orders`                              | Customer | Paginated order history                    |
| GET    | `/orders/{id}`                         | Customer | Order detail by id                         |
| GET    | `/orders/number/{orderNumber}`         | Customer | Order detail by order number               |
| POST   | `/orders/{id}/cancel`                  | Customer | Cancel (PENDING/CONFIRMED only)            |
| GET    | `/admin/orders`                        | Admin | List all orders (paginated)                   |
| PUT    | `/admin/orders/{id}/status`            | Admin | Update order status (emails on ship/deliver)  |
| POST   | `/admin/payments/orders/{orderId}/refund` | Admin | Mock refund a successful payment          |

## Review & Admin Analytics API quick reference

| Method | Endpoint                                | Auth  | Description                                    |
|--------|--------------------------------------------|:---:|---------------------------------------------------|
| GET    | `/reviews/product/{productId}`             | No  | Paginated reviews for a product                    |
| POST   | `/reviews/product/{productId}`             | Customer | Add a review (one per product)                |
| DELETE | `/reviews/{id}`                            | Customer/Admin | Delete own review (or any, if admin)     |
| GET    | `/admin/analytics/dashboard`               | Admin | Revenue, orders, products, customers, latest orders, top products, monthly trend, category sales |
| GET    | `/admin/analytics/orders/export`           | Admin | Download all orders as CSV                        |
| GET    | `/admin/customers`                         | Admin | Paginated list of customers (added in Phase 6 for the admin console's Customers page) |

## What's next

- **Phase 5** — React 19 + Vite + Tailwind frontend (auth + shop pages)
- **Phase 6** — Admin frontend, GitHub Actions CI/CD, Railway/Render deploy
  guides, ER diagram, Postman collection

Say the word and I'll build Phase 5 next — the React storefront.
