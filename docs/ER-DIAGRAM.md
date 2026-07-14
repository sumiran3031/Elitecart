# EliteCart — Entity-Relationship Diagram

This reflects the 15 JPA entities in `elitecart-backend`. Rendered
automatically by GitHub/GitLab (Mermaid support) — paste into
[mermaid.live](https://mermaid.live) if your viewer doesn't render it.

```mermaid
erDiagram
    USERS ||--o{ ADDRESSES : has
    USERS ||--o| CART : owns
    USERS ||--o| WISHLIST : owns
    USERS ||--o{ ORDERS : places
    USERS ||--o{ REVIEWS : writes
    USERS }o--o{ ROLES : "has (user_roles)"

    CATEGORIES ||--o{ CATEGORIES : "parent of"
    CATEGORIES ||--o{ PRODUCTS : contains

    PRODUCTS ||--o{ PRODUCT_IMAGES : has
    PRODUCTS ||--o{ CART_ITEMS : "referenced by"
    PRODUCTS ||--o{ WISHLIST_ITEMS : "referenced by"
    PRODUCTS ||--o{ ORDER_ITEMS : "referenced by"
    PRODUCTS ||--o{ REVIEWS : receives

    CART ||--o{ CART_ITEMS : contains
    WISHLIST ||--o{ WISHLIST_ITEMS : contains

    ORDERS ||--o{ ORDER_ITEMS : contains
    ORDERS ||--o| PAYMENTS : "paid via"
    ORDERS }o--|| ADDRESSES : "ships to"
    ORDERS }o--|| ADDRESSES : "bills to"

    USERS {
        bigint id PK
        varchar first_name
        varchar last_name
        varchar email UK
        varchar password
        varchar phone_number
        boolean enabled
        boolean account_non_locked
        varchar email_verification_token
        varchar reset_password_token
        varchar refresh_token
        datetime last_login
    }

    ROLES {
        bigint id PK
        varchar name "ROLE_ADMIN | ROLE_CUSTOMER"
    }

    ADDRESSES {
        bigint id PK
        bigint user_id FK
        varchar full_name
        varchar phone_number
        varchar address_line1
        varchar address_line2
        varchar city
        varchar state
        varchar postal_code
        varchar country
        varchar address_type "SHIPPING | BILLING"
        boolean is_default
    }

    CATEGORIES {
        bigint id PK
        varchar name
        varchar slug UK
        varchar description
        varchar image_url
        boolean active
        bigint parent_id FK
    }

    PRODUCTS {
        bigint id PK
        varchar name
        varchar slug UK
        varchar sku UK
        text description
        decimal price
        decimal discount_price
        int stock_quantity
        double rating
        int review_count
        boolean featured
        boolean active
        bigint units_sold
        bigint category_id FK
    }

    PRODUCT_IMAGES {
        bigint id PK
        bigint product_id FK
        varchar image_url
        boolean is_primary
        int display_order
    }

    CART {
        bigint id PK
        bigint user_id FK UK
    }

    CART_ITEMS {
        bigint id PK
        bigint cart_id FK
        bigint product_id FK
        int quantity
    }

    WISHLIST {
        bigint id PK
        bigint user_id FK UK
    }

    WISHLIST_ITEMS {
        bigint id PK
        bigint wishlist_id FK
        bigint product_id FK
    }

    ORDERS {
        bigint id PK
        varchar order_number UK
        bigint user_id FK
        bigint shipping_address_id FK
        bigint billing_address_id FK
        decimal subtotal
        decimal tax
        decimal discount
        decimal shipping_fee
        decimal grand_total
        varchar status "PENDING|CONFIRMED|PACKED|SHIPPED|DELIVERED|CANCELLED"
        varchar cancelled_reason
    }

    ORDER_ITEMS {
        bigint id PK
        bigint order_id FK
        bigint product_id FK
        varchar product_name
        decimal unit_price
        int quantity
        decimal line_total
    }

    PAYMENTS {
        bigint id PK
        bigint order_id FK UK
        varchar transaction_id UK
        decimal amount
        varchar method "CARD|UPI|NET_BANKING|COD|WALLET"
        varchar status "PENDING|SUCCESS|FAILED|REFUNDED"
        datetime paid_at
        datetime refunded_at
    }

    REVIEWS {
        bigint id PK
        bigint product_id FK
        bigint user_id FK
        int rating
        text comment
    }

    EMAIL_LOGS {
        bigint id PK
        varchar recipient
        varchar subject
        varchar email_type
        varchar status "SENT|FAILED"
        varchar error_message
    }
```

## Notes on design decisions

- **`order_items` snapshots `product_name` and `unit_price`** at the time of
  purchase, so a later product rename or price change never rewrites order
  history.
- **`cart` and `wishlist` are 1:1 with `users`** (unique `user_id`), created
  automatically at registration (see `AuthServiceImpl.register`).
- **`categories` is self-referencing** (`parent_id`) to support the nested
  category requirement — a category tree of arbitrary depth.
- **`payments` is 1:1 with `orders`** — one mock payment attempt per order.
  A real gateway integration would likely need a `payment_attempts` table
  instead, to support retries.
- **`email_logs` has no foreign keys on purpose** — it's an audit trail that
  should survive even if the referenced user/order is later deleted.
