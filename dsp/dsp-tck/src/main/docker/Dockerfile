FROM eclipse-temurin:21.0.2_13-jre-alpine

ARG APP_USER=docker
ARG APP_UID=10100

RUN addgroup --system "$APP_USER"

RUN adduser \
     --shell /sbin/nologin \
     --disabled-password \
     --gecos "" \
     --ingroup "$APP_USER" \
     --no-create-home \
     --uid "$APP_UID" \
     "$APP_USER"

# run "apk add" first, because user "docker" is not allowed to do that

RUN apk --no-cache add curl

USER "$APP_USER"

WORKDIR /tck

COPY .. ./

EXPOSE 8080

