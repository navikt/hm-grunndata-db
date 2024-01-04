FROM ghcr.io/navikt/baseimages/temurin:17
USER apprunner
COPY build/libs/hm-grunndata-db-all.jar ./app.jar
