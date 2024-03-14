package org.example.typesafe.gql.pizza.client

import org.example.typesafe.gql.pizza.client.fragment.IngredientsOutOfStockError
import org.example.typesafe.gql.pizza.client.fragment.NoIngredientsOnPizzaError
import org.example.typesafe.gql.pizza.client.fragment.OrderedPizza
import org.example.typesafe.gql.pizza.client.fragment.TooManyIngredientPortionsOnPizzaError

fun mapConfirmation(confirmation: OrderedPizza): OrderPizzaOutcome.Confirmation {
    return OrderPizzaOutcome.Confirmation(
        orderId = confirmation.orderId,
        totalPrice = confirmation.price,
    )
}

fun mapIngredientsOutOfStock(
    error: IngredientsOutOfStockError
): OrderPizzaOutcome.ValidationError.IngredientsOutOfStock {
    return OrderPizzaOutcome.ValidationError.IngredientsOutOfStock(
        ingredients = error.ingredients.map { ingredientsOutOfStock ->
            IngredientOutOfStock(
                ingredient = ingredientsOutOfStock.ingredient,
                orderedPortions = ingredientsOutOfStock.orderedPortions,
                availablePortions = ingredientsOutOfStock.availablePortions,
            )
        }
    )
}

fun mapTooManyIngredientPortions(
    error: TooManyIngredientPortionsOnPizzaError
): OrderPizzaOutcome.ValidationError.TooManyIngredientPortions {
    return OrderPizzaOutcome.ValidationError.TooManyIngredientPortions(
        maxAllowedPortions = error.maxPortions,
    )
}

fun mapNoIngredients(
    error: NoIngredientsOnPizzaError
): OrderPizzaOutcome.ValidationError.NoIngredientsOnPizza {
    return OrderPizzaOutcome.ValidationError.NoIngredientsOnPizza(error.availableIngredients)
}
