package no.nav.hm.grunndata.db.testcontainer

import org.testcontainers.containers.PostgreSQLContainer
import java.sql.DriverManager

object TestDbContainer {
    val postgres = PostgreSQLContainer("postgres:16").apply {
        withDatabaseName("postgres")
        withUsername("postgres")
        withPassword("postgres")
        withReuse(true)
        start()
    }

    init {
        println("TestContainer initialized with URL: ${postgres.jdbcUrl}")
        createRole("gdb")
        createDatabase("gdb", "gdb")
     }

    private fun createRole(owner: String) {
        val url = "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/postgres"
        DriverManager.getConnection(url, "postgres", "postgres").use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("CREATE ROLE $owner WITH LOGIN PASSWORD '$owner'")
            }
        }
    }

    private fun createDatabase(dbName: String, owner: String) {
        val url = "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/postgres"
        DriverManager.getConnection(url, "postgres", "postgres").use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute("CREATE DATABASE $dbName OWNER $owner")
            }
        }
    }

}