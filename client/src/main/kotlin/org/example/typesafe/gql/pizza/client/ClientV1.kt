package org.example.typesafe.gql.pizza.client

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Error
import com.fasterxml.jackson.databind.ObjectMapper
import org.example.typesafe.gql.pizza.client.fragment.IngredientsOutOfStockError
import org.example.typesafe.gql.pizza.client.fragment.NoIngredientsOnPizzaError
import org.example.typesafe.gql.pizza.client.fragment.TooManyIngredientPortionsOnPizzaError
import org.example.typesafe.gql.pizza.client.type.PizzaIngredient
import org.example.typesafe.gql.pizza.client.type.PizzaOrderInput

class ClientV1(
    private val client: ApolloClient,
    private val objectMapper: ObjectMapper,
) : PizzeriaClient {

    override suspend fun orderPizza(order: PizzaOrderInput): OrderPizzaOutcome {
        val response = client.mutation(OrderPizzaV1Mutation(order)).execute()

        return when {
            response.data != null -> mapConfirmation(response.data!!.orderPizza_v1)
            response.hasErrors() -> mapValidationErrors(response.errors!!.first())
            // should not happen
            else -> error("Nor error not data received in response")
        }
    }

    private fun mapValidationErrors(error: Error): OrderPizzaOutcome.ValidationError {
        val errorCode = error.extensions?.get("errorCode")?.toString()
        val errorContext = error.extensions?.get("context") as? Map<*, *>

        return when (errorCode) {
            "NO_INGREDIENTS_ADDED" -> {
                val mappedError = objectMapper.convertValue(errorContext, NoIngredientsOnPizza::class.java)
                mapNoIngredients(mappedError)
            }

            "TOO_MANY_INGREDIENT_PORTIONS" -> {
                val mappedError = objectMapper.convertValue(errorContext, TooManyIngredientPortionsOnPizza::class.java)
                mapTooManyIngredientPortions(mappedError)
            }

            "INGREDIENTS_OUT_OF_STOCK" -> {
                val mappedError = objectMapper.convertValue(errorContext, IngredientsOutOfStock::class.java)
                mapIngredientsOutOfStock(mappedError)
            }
            // There is no way to enforce only those possibilities.
            else -> error("did not receive expected error code")
        }
    }

    // somehow derived object for out of stock error. for simplicity it use error defined in API v2
    private class IngredientsOutOfStock(
        override val ingredients: List<Ingredient>,
    ) : IngredientsOutOfStockError {

        class Ingredient(
            override val ingredient: PizzaIngredient,
            override val availablePortions: Int,
            override val orderedPortions: Int,
        ) : IngredientsOutOfStockError.Ingredient
    }

    // somehow derived object for out of stock error. for simplicity it use error defined in API v2
    private class TooManyIngredientPortionsOnPizza(
        override val maxPortions: Int,
    ) : TooManyIngredientPortionsOnPizzaError

    // somehow derived object for out of stock error. for simplicity it use error defined in API v2
    private class NoIngredientsOnPizza(
        override val availableIngredients: List<PizzaIngredient>,
    ) : NoIngredientsOnPizzaError
}



