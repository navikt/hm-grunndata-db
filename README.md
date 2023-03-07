# hm-grunndata-db

Kjører på localhost:

```
docker-compose up -d
export DB_DRIVER=org.postgresql.Driver
export DB_JDBC_URL=jdbc:postgresql://localhost:5432/gdb
./gradlew build run

```

Sync av data fra HMDB:
````
k port-forward hm-grunndata-hmdb2gdb-7dffc4f4d9-b4h2w 8081:8080

curl http://localhost:8888/internal/sync/agreements
curl http://localhost:8888/internal/sync/suppliers
curl http://localhost:8888/internal/sync/products

````


