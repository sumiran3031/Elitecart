# EliteCart — Deployment Guide

Two deployment paths are documented here: **Railway** (simplest — one
platform for everything) and **Render** (separate services, generous free
tier). Both assume you've already pushed this repo to GitHub.

---

## Option A — Railway (recommended for a quick portfolio deploy)

Railway can host the backend, frontend, and MySQL database all in one
project, each auto-deploying from your GitHub repo on push.

### 1. Create the project
1. Go to [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub repo** → select this repo.
2. Railway will detect multiple services; point it at `elitecart-backend` first.

### 2. Add MySQL
- In the project, click **+ New** → **Database** → **MySQL**.
- Railway auto-generates `MYSQL_URL`, `MYSQLHOST`, `MYSQLPASSWORD`, etc. — note these for step 3.

### 3. Configure the backend service
- Root directory: `elitecart-backend`
- Railway auto-detects the `Dockerfile` and builds from it.
- Set environment variables (Settings → Variables):

  | Variable | Value |
  |---|---|
  | `DB_URL` | `jdbc:mysql://${{MYSQLHOST}}:${{MYSQLPORT}}/${{MYSQLDATABASE}}?useSSL=false&serverTimezone=UTC` |
  | `DB_USERNAME` | `${{MYSQLUSER}}` |
  | `DB_PASSWORD` | `${{MYSQLPASSWORD}}` |
  | `JWT_SECRET` | *generate a real 256-bit base64 secret — don't reuse the sample one* |
  | `MAIL_USERNAME` / `MAIL_PASSWORD` | your SMTP credentials |
  | `FRONTEND_BASE_URL` | your frontend's Railway URL (set after step 4) |
  | `CORS_ALLOWED_ORIGINS` | same as above |
  | `ADMIN_EMAIL` / `ADMIN_PASSWORD` | your own admin credentials, not the sample defaults |

- Railway assigns a public URL automatically (Settings → Networking → Generate Domain).

### 4. Configure the frontend service
- **+ New** → **GitHub repo** (same repo) → root directory: `elitecart-frontend`.
- Build arg / env var: `VITE_API_BASE_URL=https://<your-backend-domain>/api`
- Generate a public domain here too.
- Go back to the backend service and set `FRONTEND_BASE_URL` /
  `CORS_ALLOWED_ORIGINS` to this frontend URL, then redeploy the backend.

### 5. Done
Every `git push` to `main` now redeploys both services automatically.

---

## Option B — Render

Render separates cleanly into a **Web Service** (backend), a **Static Site**
(frontend), and a managed **PostgreSQL/MySQL** — Render's native managed DB
is PostgreSQL only, so for MySQL you'd either use Render's "Private
Service" running the official `mysql` Docker image, or an external managed
MySQL (PlanetScale, AWS RDS, etc.). The steps below assume an external
MySQL host.

### 1. Backend — Web Service
1. **New** → **Web Service** → connect this repo.
2. Root directory: `elitecart-backend`. Environment: **Docker**.
3. Set the same environment variables as the Railway table above, pointing
   `DB_URL`/`DB_USERNAME`/`DB_PASSWORD` at your external MySQL host.
4. Render builds the `Dockerfile` and exposes it on a `*.onrender.com` URL.

### 2. Frontend — Static Site
1. **New** → **Static Site** → same repo, root directory `elitecart-frontend`.
2. Build command: `npm install && npm run build`
3. Publish directory: `dist`
4. Environment variable: `VITE_API_BASE_URL=https://<backend>.onrender.com/api`
5. Add a rewrite rule so React Router works on refresh: `/*` → `/index.html` (200).

### 3. Wire CORS
Back on the backend service, set `FRONTEND_BASE_URL` and
`CORS_ALLOWED_ORIGINS` to the static site's URL, then redeploy.

---

## Environment variables reference

See `elitecart-backend/.env.example` and `elitecart-frontend/.env.example`
for the full list with defaults. The ones that **must** change before any
real deployment:

- `JWT_SECRET` — generate with `openssl rand -base64 32`
- `ADMIN_EMAIL` / `ADMIN_PASSWORD` — the seeded default admin account
- `DB_PASSWORD` — don't ship with `root`/`root`
- `MAIL_USERNAME` / `MAIL_PASSWORD` — real SMTP credentials (a Gmail **App
  Password**, not your account password, if using Gmail)

## Post-deploy checklist

- [ ] Visit `<backend-url>/api/swagger-ui.html` to confirm the API is live
- [ ] Log in with the admin account and change its password immediately
- [ ] Place a full test order end-to-end (register → verify → shop → checkout)
- [ ] Confirm transactional emails are arriving (check spam folder first)
- [ ] Point `docker-compose.yml`'s local dev values away from production secrets
