import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


val jvmTarget = "17"
val micronautVersion = "4.10.1"
val logbackEncoderVersion = "7.3"
val postgresqlVersion = "42.7.2"
val tcVersion = "1.17.6"
val mockkVersion = "1.13.4"
val kotestVersion = "5.5.5"
val rapidsRiversVersion = "202410290928"
val grunndataDtoVersion = "202509171034"
val jupiterVersion ="5.9.2"
val flywayVersion="10.6.0"
val leaderElectionVersion = "202405151234"
val jakartaPersistenceVersion = "3.1.0"
val openSearchJavaClientVersion = "2.18.0"
val opensearchTestContainerVersion = "2.1.1"

group = "no.nav.hm"
version = properties["version"] ?: "local-build"

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    kotlin("plugin.allopen") version "1.9.25"
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
    id("io.micronaut.application") version "4.6.1"
}

configurations.all {
    resolutionStrategy {
       failOnChangingVersions()
    }
}

dependencies {

    api("ch.qos.logback:logback-classic")
    api("net.logstash.logback:logstash-logback-encoder:$logbackEncoderVersion")

    runtimeOnly("org.yaml:snakeyaml")
    implementation("io.micronaut:micronaut-jackson-databind")

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")

    // micronaut-data
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("jakarta.persistence:jakarta.persistence-api:$jakartaPersistenceVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("io.micronaut.sql:micronaut-jdbc-hikari")
    kapt("io.micronaut.data:micronaut-data-processor")
    implementation("org.postgresql:postgresql:${postgresqlVersion}")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.cache:micronaut-cache-caffeine")

    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut:micronaut-management")

    implementation("com.github.navikt:hm-rapids-and-rivers-v2-core:$rapidsRiversVersion")
    implementation("com.github.navikt:hm-rapids-and-rivers-v2-micronaut:$rapidsRiversVersion")
    implementation("com.github.navikt:hm-rapids-and-rivers-v2-micronaut-deadletter:$rapidsRiversVersion")

    implementation("no.nav.hm.grunndata:hm-grunndata-rapid-dto:$grunndataDtoVersion")

    // flyway postgresql
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    implementation("com.github.navikt:hm-micronaut-leaderelection:$leaderElectionVersion")

    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    implementation("org.opensearch.client:opensearch-java:${openSearchJavaClientVersion}")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
    testImplementation("org.testcontainers:postgresql:${tcVersion}")
    testImplementation("org.opensearch:opensearch-testcontainers:${opensearchTestContainerVersion}")
}

allOpen {
    annotation("javax.inject.Singleton")
    annotation("io.micronaut.context.annotation.Context")
}

micronaut {
    version.set(micronautVersion)
    testRuntime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
    }
}

application {
    mainClass.set("no.nav.hm.grunndata.db.Application")
}

java {
    sourceCompatibility = JavaVersion.toVersion(jvmTarget)
    targetCompatibility = JavaVersion.toVersion(jvmTarget)
    withSourcesJar()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = jvmTarget
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("skipped", "failed")
        showExceptions = true
        showStackTraces = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.11"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://packages.confluent.io/maven/")
}

