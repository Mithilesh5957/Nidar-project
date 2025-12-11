# Build Stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
# Force development environment to ensure devDependencies (vite) are installed
ENV NODE_ENV=development
RUN npm install
COPY . .
RUN npm run build

# Run Stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
