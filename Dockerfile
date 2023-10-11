FROM navikt/java:17
USER root
USER apprunner
COPY build/libs/hm-grunndata-db-all.jar ./app.jar
