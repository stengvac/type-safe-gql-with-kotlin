package org.example.typesafe.gql.pizza.client

import org.example.typesafe.gql.pizza.client.type.PizzaIngredient
import org.example.typesafe.gql.pizza.client.type.PizzaOrderInput

/**
 * Facade for various client implementations.
 */
interface PizzeriaClient {

    /**
     * Submit pizza [order].
     *
     * @throws IllegalStateException in case of unexpected states.
     * @return either order with data or validation error.
     */
    suspend fun orderPizza(order: PizzaOrderInput): OrderPizzaOutcome
}

sealed interface OrderPizzaOutcome {

    data class Confirmation(
        val orderId: Int,
        val totalPrice: Int,
    ) : OrderPizzaOutcome

    sealed interface ValidationError : OrderPizzaOutcome {
        // customer has to add at least one ingredient with at least one portion on pizza
        data class NoIngredientsOnPizza(
            val availableIngredients: List<PizzaIngredient>,
        ) : ValidationError

        // too many ingredient portions on pizza
        data class TooManyIngredientPortions(
            val maxAllowedPortions: Int,
        ) : ValidationError

        // pizzeria run out of stock of listed ingredients
        data class IngredientsOutOfStock(
            val ingredients: List<IngredientOutOfStock>,
        ) : ValidationError
    }
}

data class IngredientOutOfStock(
    val ingredient: PizzaIngredient,
    val orderedPortions: Int,
    val availablePortions: Int,
)
