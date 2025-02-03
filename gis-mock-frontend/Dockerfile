# 1. Use an official Node.js runtime (LTS) to build and run the Next.js app
FROM node:22 AS frontend
# 2. Create and set the working directory
WORKDIR /app
# 3. Copy package.json and pnpm-lock.yaml (if present)
COPY package.json .
#COPY package-lock.json ./
#COPY pnpm-lock.yaml ./

# 4. Install pnpm globally, then install dependencies
RUN npm install -g pnpm
RUN pnpm install

# 5. Copy the rest of the application
COPY . .

# 6. Build the Next.js application for production
RUN pnpm run build

# 7. Expose port 3000
EXPOSE 3000

# 8. Start the Next.js server
CMD ["pnpm", "start"]