FROM node:lts-alpine as build

WORKDIR /build
COPY . .

RUN npm install && \
    npm run build

FROM nginx:stable-alpine as prod

RUN apk add curl

COPY service-wait /bin/service-wait
COPY nginx.conf /etc/nginx.conf
COPY --from=build /build/dist /var/www/html/static

EXPOSE 80

CMD [ "nginx", "-c", "/etc/nginx.conf" ]
