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
<div class="bigger">Repo: <a>https://github.com/stengvac/type-safe-gql-with-kotlin</a></div> 
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
* Mainly GraphQL schema designing.
* GraphQL semantics for state representation. That's what will deliver type safety.
* Order Pizza API evolution Show case.

# API producer vs API consumer

* Statement: **usually main API focus is on success scenario**.
* It is easy to implement.
* Is it fine tho? What about errors, validation errors?
* Don't take me wrong I am not saying it is wrong approach. As usual it depends...

Today we will focus on case, where consumer has to react on validation errors:
* validation errors has to be well defined. 
* **Have you tried to consume validation errors from yours API?**

and has to react accordingly. Is proposed API friendly in such situation? 

# Motivation aka What will we learn?

* Improve GraphQL API behavior - state representation to consumer
* Reach Type safety on Client/Server
* Less painful integration

# Show case - Pizzeria

* Webpage where customer can customize and Order Pizza:
  * size: 32 cm or 45 cm
  * base: tomato, creamy
  * ingredients: ham, olives, ...
* Order Pizza response contain `order id` and `price` to pay once customer show up to pick up pizza.
* Web has to provide excellent user experience.
  * That means handle validation errors returned by GraphQL API. Not just **something went wrong** message.
  * Target is 5 stars rating

API has 3 versions. Each version with different approaches, but provide exactly same behavior.

<p class="between-slide-space">

# Version 1

Lets start with GraphQL schema [version 1](../server/src/main/resources/schema/schema.graphqls).

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
Response:
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
produce response
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

## Build in Error System

This version use for error propagation build in error system in GraphQL. Errors format according
to [Spec October 2021](https://spec.graphql.org/October2021/#sec-Errors) is defined as:

* The `errors` entry in the response is a non-empty list of errors, where each error is a map.
* If no errors were raised during the request, the `errors` entry should not be present in the result.
* If the data entry in the response is not present, the errors entry in the response must not be empty.

`error` structure is defined as:

* required key `message` with a description of the error intended for the developer as a guide to understand and
  correct the error.
* optional key `extensions` - must have a map as its value. additional information to errors however they see fit, and
  there are no additional restrictions on its contents.
* other optional keys ...

## Client
[README](../client/README.md). Client [facade](../client/src/main/kotlin/org/example/typesafe/gql/pizza/client/PizzeriaClient.kt), [mutation v1](../client/src/main/resources/graphql/mutation/orderPizza_v1.graphql) and [client v1](../client/src/main/kotlin/org/example/typesafe/gql/pizza/client/ClientV1.kt).

## Pros

* Easy to implement.
* Easy to understand. Apply to (validation) errors?

## Cons

* Errors are not type safe. Custom content has to be serialized as Map.
* Extract information from error is tricky. Integration via strings -_-.
* Ambiguity - which errors can be expected in response at once? Is it documented?
* Compiler won't help it is just bunch of strings.
* Errors can change without consumer knowledge. No schema backed.
* Testability on consumer side - which error combinations can be expected at same time? What has to be tested?

## API v1 Conclusion

* Success path is defined well.
* Consumer is able to react on errors. Has to be documented or found out by calling API.
* Error structure can change without notice. No schema backed.
* Integration regarding validation errors depends on strings..

<p class="between-slide-space"></p>

# Version 2

Lets improve some of mentioned problems with [schema_v2](../server/src/main/resources/schema/orderPizza_v2.graphqls).

## Validation Errors are part of schema

Response for validation error Too Many Ingredients
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
how [mutation v2](../client/src/main/resources/graphql/mutation/orderPizza_v2.graphql) and [client v2](../client/src/main/kotlin/org/example/typesafe/gql/pizza/client/ClientV2.kt) changed.

## Pros

* Validation errors are part of schema -> Codegen will help.
* Type safe to some extent. No need to deserialize `errors` and `extensions` fields any longer.
* Changes in errors are reflected is schema -> can be detected by consumer.

## Cons

* Response type mix data and validation errors -> reusability is lost.
* Ambiguity - which errors can be expected in response at once? Mixing possibilities...
* Testability on consumer side - which error combinations can be expected at same time?

## API v2 Conclusion

* Error types are now part of schema. Deserialization is solved.
* Consumer has to implement correctly error recognition. Not based on codes, but field presence.
* Type safety reached to some extent... Can be better tho.

<p class="between-slide-space"></p>

# Version 3

Lets improve API one last time. Now with help of GraphQL `union` semantics to get rid of ambiguous states.

## GraphQL Union

* Abstract GraphQL type, which enable to return one multiple object types.
    * Not **scalar** or **input type**
    * Types can have entirely different structure. 
* It go quite well with `sealed` semantics in Kotlin. Coincidence?

```graphql
union SearchResult = Book | Author

type Book {
    title: String!
}

type Author {
    name: String!
}
```

`SearchResult` is one of types `Boor` or `Author` not both or none of them.

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

`__typename` is technical GraphQL field, which can be queried for any type. It is used to match returned data to types listed in `union`.
Thus various framework can map returned data to correct types.

## Pros
* Clearly expressed API intention for various states.
  * Which error can occur at once.
* Schema clearly define, which responses are produced.
* Testability is Quite Good. On producer and consumer side as we both sides know, which states can occur.
* Codegen tools take advantage of `union` semantics and generate adequate code. Kotlin `sealed`, Typescript `union`.

## Cons
* API definition is complex.
* More Code, Complexity and Maintenance cost.
* Adding new type to union is breaking change for consumer.

# Summary
Proof APIs works as expected [ApiBehaviorTest](../client/src/test/kotlin/org/example/typesafe/gql/pizza/client/ApiBehaviorTest.kt).

We saw multiple approaches how to pass states to consumer. Difference lies in:

* API Complexity
* Consumer friendliness
* Type safety
* Dependents on use case...

# Thanks. Questions?
