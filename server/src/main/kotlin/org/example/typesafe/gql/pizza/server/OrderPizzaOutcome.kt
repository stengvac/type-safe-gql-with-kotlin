package org.example.typesafe.gql.pizza.server

import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaIngredient

sealed interface OrderPizzaOutcome

data class PizzaOrderConfirmation(
    val id: Int,
    val totalPrice: Int,
): OrderPizzaOutcome

sealed interface OrderPizzaValidationError : OrderPizzaOutcome {
    // customer has to add at least one ingredient on pizza
    data class NoIngredientsOnPizza(
        val availableIngredients: List<PizzaIngredient>,
    ) : OrderPizzaValidationError

    // too many ingredients portions on pizza - it just won't fit
    data class TooManyIngredientPortionsOnPizza(
        val maxPortions: Int,
    ) : OrderPizzaValidationError

    // pizzeria run out of stock of ingredients
    data class IngredientsOutOfStock(
        val ingredients: List<IngredientOutOfStock>,
    ) : OrderPizzaValidationError

    data class IngredientOutOfStock(
        val ingredient: PizzaIngredient,
        val orderedPortions: Int,
        val availablePortions: Int,
    )
}
