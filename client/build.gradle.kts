import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.nio.file.Path

plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.21"
    // generate Query/Mutation from prepared graphql files
    id("com.apollographql.apollo3") version "3.8.2"
}

repositories {
    mavenCentral()
}

dependencies {
    val apolloClientVersion = "3.8.2"
    implementation("com.apollographql.apollo3:apollo-runtime-jvm:$apolloClientVersion")
    implementation("com.apollographql.apollo3:apollo-adapters-jvm:$apolloClientVersion")

    val koTestVersion = "5.8.0"
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$koTestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$koTestVersion")

    val jackson = "2.15.4"
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson")
}

group = "org.example"

apollo {
    service("pizza") {
        val schemaPath = Path.of(projectDir.path, "src", "main", "resources", "graphql")

        schemaFile.set(schemaPath.resolve( "schema.graphqls").toFile())
        includes.set(listOf("**/*.graphql"))
        srcDir(schemaPath.toFile())
        packageName.set("org.example.typesafe.gql.pizza.client")

        generateOptionalOperationVariables.set(false)

        codegenModels.set("responseBased")
        flattenModels.set(true)
        // those scalar adapters has to be added to client via builder
        // see https://github.com/apollographql/apollo-kotlin/blob/main/apollo-gradle-plugin-external/src/main/kotlin/com/apollographql/apollo3/gradle/api/Service.kt
        mapScalar("PositiveInt", "kotlin.Int")
    }
}

tasks {
    downloadApolloSchema {
        endpoint.set("http://localhost:8080/graphql")
        schema.set("${projectDir}/src/main/resources/graphql/schema.graphqls")
    }

    test {
        useJUnitPlatform {
            testLogging.exceptionFormat = TestExceptionFormat.FULL
            testLogging.showStandardStreams = true
        }
    }
}
