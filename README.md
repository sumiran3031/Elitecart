# EliteCart

A complete, portfolio-ready, full-stack e-commerce platform — Spring Boot 3
backend, React 19 storefront + admin console, Docker, CI/CD, and full API
docs.

Built across 6 phases as a real working system at every step (no
placeholders, no partial modules) — see each phase's own README for the
detailed breakdown.

---

## Features

- **Auth**: JWT access/refresh tokens, email verification, forgot/reset/change
  password, remember-me, role-based access (ADMIN / CUSTOMER)
- **Catalog**: nested categories, product search/filter/sort/pagination,
  multiple images, discount pricing, featured/latest/best-seller listings
- **Shopping**: cart, wishlist (with move-to-cart), saved addresses
- **Orders**: full checkout flow, mock payment gateway (card/UPI/net
  banking/COD/wallet), order status lifecycle with automatic emails,
  customer cancellation, admin refunds
- **Reviews**: one per customer per product, live rating aggregation
- **Admin console**: Recharts dashboard (revenue trend, category breakdown,
  top products), product/category/order/customer management, CSV export
- **Ops**: Dockerized, GitHub Actions CI (build + test both apps), deployment
  guides for Railway and Render

## Tech stack

**Backend** — Java 21, Spring Boot 3.3, Spring Security 6, JWT (jjwt),
Spring Data JPA / Hibernate, MySQL 8, Lombok, MapStruct, springdoc-openapi,
Jakarta Validation, Spring Mail, JUnit 5 + Mockito, Docker

**Frontend** — React 19, Vite 6, React Router 6, Redux Toolkit, Axios, React
Hook Form, Framer Motion, Recharts, React Icons, Tailwind CSS

**DevOps** — Docker Compose, GitHub Actions, Railway / Render

## Folder structure

```
elitecart/
├── elitecart-backend/       # Spring Boot API (see its own README for full detail)
├── elitecart-frontend/      # React storefront + admin console (see its own README)
├── docs/
│   ├── ER-DIAGRAM.md         # Full entity-relationship diagram (Mermaid)
│   ├── ARCHITECTURE.md       # System architecture + request-flow sequence diagrams
│   └── DEPLOYMENT.md         # Step-by-step Railway & Render deployment guides
├── postman/
│   └── EliteCart.postman_collection.json   # Every endpoint, ready to import
├── .github/workflows/ci-cd.yml              # Build + test both apps on every push
├── docker-compose.yml                        # MySQL + backend + frontend, one command
├── LICENSE
└── README.md                                 # You are here
```


## Diagrams

- [`docs/ER-DIAGRAM.md`](docs/ER-DIAGRAM.md) — full database schema (15 tables) with relationship notes
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — system architecture, plus sequence diagrams for the auth and checkout flows

## Author

Built as a portfolio project demonstrating a production-shaped full-stack
build: clean layered architecture, real transaction boundaries, JWT security
done properly, and a frontend that isn't just static mockups — every screen
is wired to a real, working API.

## License

MIT — see [LICENSE](LICENSE).

## Future enhancements

- Real payment gateway integration (Stripe/Razorpay) behind the existing `PaymentService` interface
- Coupon/discount codes at cart level
- Product variants (size/color) instead of flat SKUs
- Elasticsearch-backed product search for large catalogs
- WebSocket-based real-time order status updates
- Multi-currency / i18n support
- Automated E2E tests (Playwright/Cypress) in CI
