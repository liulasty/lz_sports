# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Run stage
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
ARG FRONTEND_PORT=5173
RUN sed -i "s/listen 80;/listen ${FRONTEND_PORT};/" /etc/nginx/conf.d/default.conf
EXPOSE ${FRONTEND_PORT}
CMD ["nginx", "-g", "daemon off;"]
