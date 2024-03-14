pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.21"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "type-safe-gql-with-kotlin"
include("server")
include("client")
