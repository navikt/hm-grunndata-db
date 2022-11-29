# hm-grunndata-db

Kjører på localhost:
```

docker-compose up -d

export DB_DRIVER=org.postgresql.Driver
export DB_JDBC_URL=jdbc:postgresql://localhost:5432/gdb
export OPEN_SEARCH_URI=https://localhost:9200
export OPEN_SEARCH_PASSWORD=admin
export OPEN_SEARCH_USERNAME=admin

```
Gunndata for hjelpemidler.

Proof of concept:

Vi starter med en proof of concept med denne tjenesten som base. POC'en blir en monolith og etterhvert splittet 
i mindre microservices.



