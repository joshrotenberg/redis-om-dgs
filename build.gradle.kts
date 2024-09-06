plugins {
    java
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    id("com.netflix.dgs.codegen") version "6.2.1"
    id("com.google.cloud.tools.jib") version "3.4.3"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spring.io/milestone")
}

extra["netflixDgsVersion"] = "9.0.0"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

    // DGS
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter")
    implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars")

    // Redis OM
    implementation("com.redis.om:redis-om-spring:0.9.3")
    annotationProcessor("com.redis.om:redis-om-spring:0.9.3")

    // Start up Redis automatically in development
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // CSV
    implementation("com.google.guava:guava:33.2.1-jre")
    implementation("org.apache.commons:commons-csv:1.11.0")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // testcontainers
    testImplementation("org.testcontainers:testcontainers:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("com.redis:testcontainers-redis:2.2.2")
}

dependencyManagement {
    imports {
        mavenBom("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:${property("netflixDgsVersion")}")
    }
}

tasks.generateJava {
    schemaPaths.add("${projectDir}/src/main/resources/graphql-client")
    packageName = "com.example.redisomdgs.codegen"
    generateClient = true
    generateCustomAnnotations = true
    typeMapping = mutableMapOf("Point" to "org.springframework.data.geo.Point")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
