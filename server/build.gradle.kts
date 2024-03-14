group = "org.example.typesafe.gql.pizza.server"

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.21"
    id("com.netflix.dgs.codegen") version "6.1.4"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.3"))
    implementation("org.springframework.boot:spring-boot-starter")

    implementation(platform("com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:8.3.1"))
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
    implementation("com.netflix.graphql.dgs:graphql-dgs-extended-scalars")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.apache.commons:commons-lang3:3.14.0")
}

tasks {
    // DGS code gen task - yep confusing name
    generateJava {
        schemaPaths = mutableListOf("${projectDir}/src/main/resources/schema")
        packageName = "org.example.typesafe.gql.pizza.schema.codegen"
        // Enable generating the type safe query API - testing
        generateClient = false
        generateDataTypes = true
        generateInterfaces = false
        generateInterfaceSetters = false
        snakeCaseConstantNames = true

        typeMapping = mutableMapOf(
            "PositiveInt" to "kotlin.Int",
        )
    }
}
