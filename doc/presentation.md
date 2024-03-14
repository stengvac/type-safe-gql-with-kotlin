<style>
    .between-slide-space {
        margin-top: 350px;
    }

    .wrapper {
        overflow: hidden; /* add this to contain floated children */
    }
    .author-image {
        padding-left: 20px;
        width: 190px;
        float:left;
    }
    .author-label {
        float: left;
        width: 150px;
        text-align: right;
        padding-right: 15px;

    }
    .author-text {}
    .bold {
      font-weight: bold;
    }
    .bigger {
      font-size: large;
    }
    body {
      font-size: 120% !important;
    }

</style>

# Typesafe GraphQL API with Kotlin
<div class="bigger">Repo: <a href="https://github.com/stengvac/type-safe-gql-with-kotlin">https://github.com/stengvac/type-safe-gql-with-kotlin</a></div> 
<div class="bigger">Disclaimer: Speaker can have Strong Opinions.</div>

## Speaker

<div class="wrapper">
  <div class="author-image">
    <img src="w3_peasant.jpg" />
  </div>
  <div class="author-label">
    <div>Name:</div>
    <div>Job Title:</div>
    <div>Motto:</div>
  </div>
  <div class="author-text">
    <div class="bold">Václav Štengl</div>
    <div class="bold">Señor Software Engineer, Ataccama</div>
    <div class="bold">"More work? Off I go, then!" (Warcraft 3, Peasant)</div>
</div>
</div>

## About

* Kotlin just a little bit :(.
* Focus primarily on designing GraphQL operations.
* Emphasize GraphQL semantics for state representation, ensuring type safety.
* Showcase the evolution of the Order Pizza API.

# API producer vs API consumer

* Statement: **Usually, the primary focus of the API is on the success scenario**.
* This approach is straightforward to implement.
* However, is it sufficient? What about errors and validation errors?
* While not implying that it's the wrong approach, it's important to recognize its limitations.

Today, we will concentrate on cases where the consumer must react to validation errors:
* Validation errors need to be well-defined.
* **Have you attempted to handle validation errors from your API?**

# What will we learn?

* Enhancing GraphQL API behavior for consumer state representation.
* Achieving type safety on both client and server sides.
* Less painful integration.

# Showcase - Pizzeria

* A webpage where customer can customize and order Pizza:
  * Size: 32 cm or 45 cm
  * Base: tomato, creamy
  * Ingredients: ham, olives, ...
* The Order Pizza response contain `order id` and `price` to be paid when customer arrives to pick up pizza.
* The website aims to provide an excellent user experience
  * including handling validation errors returned by the GraphQL API, rather than simply displaying a generic error message.

The API has 3 versions, each with different approach, but all providing the same behavior.

<p class="between-slide-space">

# Version 1

Let's begin with the GraphQL schema for [version 1](../server/src/main/resources/schema/schema.graphqls).

## Valid Order

```graphql
mutation OrderPizzaV1 {
  orderPizza_v1(
    order: {
      size: STANDARD,
      base: TOMATO,
      ingredients: [
        {
          ingredient: HAM,
          portions: 2
        }
        {
          ingredient: MOZZARELLA,
          portions: 2
        }
      ]
    }
  ) {
    price
    orderId
  }
}
```
Produces response:
```json
{
  "data": {
    "orderPizza_v1": {
      "price": 255,
      "orderId": 1
    }
  }
}
```

## Invalid Order
```graphql
mutation OrderPizzaV1 {
  orderPizza_v1(
    order: {
      size: STANDARD,
      base: TOMATO,
      ingredients: [
        {
          ingredient: HAM,
          portions: 6
        },
        {
          ingredient: MOZZARELLA,
          portions: 5
        }
      ]
    }
  ) {
    price
    orderId
  }
}
```
Produces response
```json
{
  "errors": [
    {
      "message": "Too many ingredients on Pizza.",
      "locations": [
        {
          "line": 2,
          "column": 5
        }
      ],
      "path": [
        "orderPizza_v1"
      ],
      "extensions": {
        "errorCode": "TOO_MANY_INGREDIENT_PORTIONS",
        "context": {
          "maxPortions": 10
        },
        "classification": "DataFetchingException"
      }
    }
  ],
  "data": null
}
```

## Built-in Error System

This version utilizes the built-in error system in GraphQL for error propagation. Errors formatted according
to the [Spec October 2021](https://spec.graphql.org/October2021/#sec-Errors) as follows:

* The `errors` entry in the response is a non-empty list of errors, where each error is a map.
* If no errors were raised during the request, the `errors` entry should not be present in the result.
* If the data entry in the response is not present, the errors entry in the response must not be empty.

The structure of an `error` is defined as follows:

* A Required key `message` with a description of the error intended for the developer as a guide to understand and
  correct the error.
* An optional key `extensions` - must have a map as its value. Additional information to errors however they see fit, and
  there are no additional restrictions on its contents.
* Other optional keys ...

## Client
[README](../client/README.md). Client [facade](../client/src/main/kotlin/org/example/typesafe/gql/pizza/client/PizzeriaClient.kt), [mutation v1](../client/src/main/resources/graphql/mutation/orderPizza_v1.graphql) and [client v1](../client/src/main/kotlin/org/example/typesafe/gql/pizza/client/ClientV1.kt).

## Pros

* Easy to implement and understand.

## Cons

* Errors lack type safety.
* Extracting information from errors can be tricky.
* Ambiguity regarding which errors can be expected in response.
* Compiler can't help with string based integration.
* Errors can change without consumer notification.
* Limited testability on the consumer side.

## API v1 Conclusion

* Success path is well-defined.
* Consumers can react to errors but require documentation or direct API calls.
* Error structure lacks schema backing.
* Integration for validation errors relies on strings.

<p class="between-slide-space"></p>

# Version 2

Let's address some of the mentioned issues with [schema_v2](../server/src/main/resources/schema/orderPizza_v2.graphqls).

## Validation Errors are part of schema

Response for the validation error "Too Many Ingredients"
```json
{
  "data": {
    "orderPizza_v2": {
      "order": null,
      "outOfStock": null,
      "noIngredientsOnPizza": null,
      "pizzaOvercrowded": {
        "maxPortions": 10
      }
    }
  },
  "errors": null
}
```
See how [mutation v2](../client/src/main/resources/graphql/mutation/orderPizza_v2.graphql) and [client v2](../client/src/main/kotlin/org/example/typesafe/gql/pizza/client/ClientV2.kt) have changed.

## Pros

* Validation errors are part of the schema, enabling code generation.
* Type safety for validation errors.
* Changes in errors are reflected is schema.

## Cons

* Response type mixes data and validation errors, reducing reusability.
* Ambiguity regarding which errors can be expected in response at once.
* Limited testability on the consumer side.

## API v2 Conclusion

* Error types are now part of the schema, achieving type safety.
* Validation error deserialization is handled by lib/framework.
* Consumers need to correctly implement error recognition based on field presence.

<p class="between-slide-space"></p>

# Version 3

Let's further enhance the API. This time, with the help of GraphQL `union` semantics to eliminate ambiguous states.

## GraphQL Union

* An abstract GraphQL type that allows returning one of multiple object types.
    * Not a **scalar** or **input type**
    * Types can have entirely different structure. 
* It pairs well with `sealed` semantics in Kotlin.

```graphql
union SearchResult = Book | Author

type Book {
    title: String!
}

type Author {
    name: String!
}
```

`SearchResult` can be one of the types `Book` or `Author`, but not both or neither.

Final API [schema_v3](../server/src/main/resources/schema/orderPizza_v3.graphqls), [mutation v3](../client/src/main/resources/graphql/mutation/orderPizza_v3.graphql) and [client v3](../client/src/main/kotlin/org/example/typesafe/gql/pizza/client/ClientV3.kt).

```json
{
  "data": {
    "orderPizza_v3": {
      "__typename": "OrderPizzaValidationErrorResponse",
      "error": {
        "__typename": "TooManyIngredientPortionsOnPizzaError",
        "maxPortions": 10
      }
    }
  }
}
```

The `__typename` field is a technical GraphQL field used to match returned data to types listed in the union. Various frameworks can use it to map returned data to correct types.

## Pros
* Clearly expresses API intentions for various states, indicating which errors can occur simultaneously.
* Schema clearly defines which responses are produced.
* Testability is quite good, as both producer and consumer sides know which states can occur.
* Code generation tools leverage `union` semantics to generate appropriate code (e.g., Kotlin, Java `sealed`, TypeScript `union`).

## Cons
* API definition becomes complex.
* Increased code complexity and maintenance costs.
* Adding a new type to the union is a breaking change for consumers.

# Summary
Proof that APIs work as expected with [ApiBehaviorTest](../client/src/test/kotlin/org/example/typesafe/gql/pizza/client/ApiBehaviorTest.kt).

We've explored multiple approaches to communicate states to consumers, differing in:

* API complexity
* Consumer-friendliness
* Type safety

Which one use? Dependents on use case.

# Thanks. Questions?
