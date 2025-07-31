# hm-grunndata-db

Kjører på localhost:

```
docker-compose up -d
export DB_DRIVER=org.postgresql.Driver
export DB_JDBC_URL=jdbc:postgresql://localhost:5432/gdb
export RAPIDSANDRIVERS_ENABLED=true
./gradlew build run

```

```
index grunndata to opensearch
Run on localhost:
curl -X post http://localhost:8082/internal/index/all?alias=true
```
