# Type safe Kotlin client

Generate Java/Kotlin code from provided GraphQL schema and specified queries. Used library [apollographql/apollo-kotlin](https://github.com/apollographql/apollo-kotlin).

## Download schema
Schema has to be retrieved from endpoint. Introspection has to be enabled. 

* Start Order Pizza App
* Run `./gradlew downloadApolloSchema` see configuration in [build](build.gradle.kts) task `downloadApolloSchema`
* See updated [schema](src/main/resources/graphql/schema.graphqls)

## Generate Query/Mutation
Generator will use files matching [src/main/resources/graphql/**/*.graphql](src/main/resources/graphql)

* Add your own query/mutation. Name used in generator taken from `query <Name> { .. }`
* Run `./gradlew generateApolloSources`. Configuration is in [build](build.gradle.kts) under `apollo { service("pizza") { ... } }`.
