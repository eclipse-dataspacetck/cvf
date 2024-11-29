FROM eclipse-temurin:23.0.1_11-jre-alpine
ARG JAR
ARG ADDITIONAL_FILES

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

USER "$APP_USER"
WORKDIR /app

COPY ${JAR} tck-runtime.jar
COPY ${ADDITIONAL_FILES} ./

HEALTHCHECK NONE

ENTRYPOINT ["java", \
     "-Djava.security.egd=file:/dev/urandom", \
     "-jar", \
     "tck-runtime.jar"]
