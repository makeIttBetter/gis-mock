
# Financial Data Filtering App (Frontend)

This is the **Next.js + React** frontend where users can view and filter Apple's annual income statements.

## Prerequisites

- **Node.js 18+** or **Node.js 22+**
- **pnpm** package manager (instead of npm or yarn)

## How to Run Locally

1. **Install Dependencies**:
   ```bash
   pnpm install
   ```
2. **Development Server**:
   ```bash
   pnpm dev
   ```
   - Opens on [http://localhost:3000](http://localhost:3000) by default.

## How to Build for Production Locally

```bash
pnpm build
pnpm start
```
- This compiles the Next.js application and serves it on `localhost:3000`.

## How to Run via Docker (Alone)

1. **Build Image**:
   ```bash
   docker build -t financial-data-frontend .
   ```
2. **Run Container**:
   ```bash
   docker run -p 3000:3000 financial-data-frontend
   ```
3. Open [http://localhost:3000](http://localhost:3000) in your browser to see the React app.

## Project Files

- **src/** contains the Next.js pages and components.
- **public/** hosts static assets.
- **Dockerfile** (production Dockerfile with multi-stage build).
- **nginx.conf** sets up the server to handle `/api` requests to the backend and serve the React build at `/`.
