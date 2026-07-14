# EliteCart — Frontend (Phases 5–6: Storefront + Admin Console)

The full frontend, built with **React 19 + Vite + Tailwind CSS** — the
customer storefront (Phase 5) plus the admin console (Phase 6).

This is a complete, working frontend that talks to the Phase 1–4 backend —
not a mockup. Every page below is wired to a real API call.

## Design system

- **Palette**: warm off-white canvas (`#FAFAF9`), near-black ink (`#15151A`),
  an indigo→violet brand gradient (`#6366F1 → #8B5CF6` — the same gradient
  used in the backend's transactional emails, for brand continuity), amber
  for discounts, emerald/rose for success/danger states.
- **Type**: Space Grotesk for display/headings, Inter for body copy,
  JetBrains Mono for prices and SKUs.
- **Signature elements**: a glassmorphic sticky navbar (`backdrop-blur-xl`),
  an animated gradient-mesh hero, and rounded-2xl product cards with a
  soft hover-lift (`card-hover`) — all defined as reusable Tailwind
  `@layer components` classes in `src/index.css` (`.btn-primary`, `.card`,
  `.input-field`, etc.) so every page stays visually consistent.

## Tech stack

React 19 · Vite 6 · React Router 6 · Redux Toolkit · Axios · React Hook Form ·
Framer Motion · Recharts (wired up in Phase 6 for the admin dashboard) ·
React Icons · react-hot-toast · Tailwind CSS

## What's included in this phase

- **Auth**: Login (with remember-me), Register, Forgot Password, Reset
  Password, Verify Email — all on a shared glass `AuthLayout` with the
  animated mesh background.
- **Axios client with automatic token refresh**: a response interceptor
  catches `401`s, silently calls `/auth/refresh-token` exactly once, queues
  any requests that arrived mid-refresh, and retries them — falling back to
  a redirect to `/login` only if the refresh itself fails.
- **Redux Toolkit slices**: `auth`, `cart`, `wishlist` — each backed by
  `createAsyncThunk`s that call the real API and persist the session
  (`accessToken`, `refreshToken`, `user`) to `localStorage`.
- **Home**: animated gradient hero, featured / new-arrivals / best-sellers
  rows pulled live from `/products/featured`, `/products/latest`,
  `/products/best-sellers`.
- **Products**: full search/filter/sort/pagination UI wired to the Phase 2
  `GET /products` endpoint — keyword search, category sidebar (with live
  product counts), price range, featured toggle, sort dropdown, URL-synced
  filters (shareable/bookmarkable search results), and a paginator.
- **Product details**: image gallery, quantity selector, add-to-cart /
  add-to-wishlist, and a full review section (submit a review if logged in,
  paginated review list) — rating/review count update live after submitting.
- **Cart**: quantity +/-, remove item, live totals (subtotal/tax/shipping/
  grand total) computed by the backend.
- **Wishlist**: remove item, one-click move-to-cart.
- **Checkout**: address selection (with an inline "add new address" form),
  separate billing address toggle, mock payment method picker, and order
  placement that redirects straight into the new order's detail page.
- **My Orders / Order Details**: paginated order history, a visual status
  timeline (Pending → Confirmed → Packed → Shipped → Delivered), cancel
  button (only while cancellable), and payment/shipping summary.
- **Profile**: account info, change password, saved addresses (add/delete).
- **Route protection**: `ProtectedRoute` guards `/checkout`, `/orders`,
  `/profile` and redirects unauthenticated visitors to `/login`, returning
  them to where they were headed after signing in.
- **404 page**.

The project **builds clean** (`npm run build` verified in this environment —
497 modules transformed, no errors).

## Phase 6 additions — Admin Console

- **Admin layout**: dedicated sidebar navigation (Dashboard, Products,
  Categories, Orders, Customers), gated behind `ProtectedRoute
  requireAdmin` — non-admins are redirected away even if they guess the URL.
- **Dashboard**: Recharts-powered — a 12-month revenue line chart, a
  revenue-by-category pie chart, a top-products-by-units-sold bar chart,
  headline stat cards (revenue/orders/products/customers), a latest-orders
  feed, and a one-click CSV export (streamed through the authenticated Axios
  client as a blob, then triggered as a browser download — not a bare link,
  since the export endpoint requires a bearer token).
- **Products**: full CRUD table — inline create/edit form, delete with
  confirmation, category dropdown sourced live from the API.
- **Categories**: full CRUD table with a parent-category dropdown (excludes
  the category being edited, to prevent self-parenting) and a product count
  per row.
- **Orders**: paginated table with an inline status-change dropdown
  (triggers the backend's automatic shipped/delivered emails) and a refund
  action for successfully paid orders.
- **Customers**: paginated read-only table (backed by a small backend
  addition — `GET /admin/customers` — added in this phase alongside the
  frontend, since Phase 1–4 never needed a customer-listing endpoint until
  the admin console called for one).

The project still **builds clean** with the admin console included
(verified: 1122 modules transformed, no errors — Recharts adds real size,
noted as a follow-up: the build now warns about a >500kB chunk, a natural
candidate for route-based code-splitting via `React.lazy` if this were
pushed further toward production).

## Project structure

```
elitecart-frontend/
├── src/
│   ├── api/              # axiosClient (with refresh interceptor) + one file per resource
│   ├── app/store.js       # Redux Toolkit store
│   ├── features/          # auth / cart / wishlist slices (createAsyncThunk-based)
│   ├── components/
│   │   ├── layout/        # Navbar, Footer, MainLayout
│   │   ├── common/        # ProtectedRoute, StarRating, Pagination, LoadingSkeleton, EmptyState
│   │   └── product/       # ProductCard
│   ├── pages/
│   │   ├── auth/          # Login, Register, ForgotPassword, ResetPassword, VerifyEmail, AuthLayout
│   │   ├── customer/       # Home, Products, ProductDetails, Cart, Wishlist, Checkout,
│   │   │                   # MyOrders, OrderDetails, Profile
│   │   └── admin/          # AdminLayout, AdminDashboard, AdminProducts, AdminCategories,
│   │                       # AdminOrders, AdminCustomers
│   ├── App.jsx             # Route definitions
│   ├── main.jsx            # Entry point (Redux Provider, Router, Toaster)
│   └── index.css           # Tailwind directives + design-system component classes
├── index.html
├── tailwind.config.js       # Design tokens (colors, fonts, gradients, shadows, keyframes)
├── vite.config.js
├── Dockerfile                # Multi-stage build → served via nginx
├── nginx.conf
└── .env.example
```

## Running locally

```bash
cp .env.example .env   # set VITE_API_BASE_URL if your backend isn't on localhost:8080
npm install
npm run dev
```

The app runs at `http://localhost:5173` and expects the Phase 1–4 backend at
`http://localhost:8080/api` (see the backend's own README for how to start it).

### Docker

This is already wired into the root `docker-compose.yml` from Phase 1 — running
`docker-compose up --build` from the repo root now builds and serves this
frontend on port `5173` via nginx, alongside the backend and MySQL.

## Related docs

- `../docs/ARCHITECTURE.md` — system architecture + request-flow diagrams
- `../docs/ER-DIAGRAM.md` — full database schema
- `../docs/DEPLOYMENT.md` — Railway & Render deployment steps
- `../postman/EliteCart.postman_collection.json` — every API endpoint, ready to import
