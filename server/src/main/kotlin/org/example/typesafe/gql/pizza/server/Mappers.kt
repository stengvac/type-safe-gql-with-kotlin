package org.example.typesafe.gql.pizza.server

import org.example.typesafe.gql.pizza.schema.codegen.types.IngredientOutOfStock
import org.example.typesafe.gql.pizza.schema.codegen.types.IngredientsOutOfStockError
import org.example.typesafe.gql.pizza.schema.codegen.types.OrderedPizza
import org.example.typesafe.gql.pizza.schema.codegen.types.TooManyIngredientPortionsOnPizzaError

fun PizzaOrderConfirmation.toGql() = OrderedPizza(
    orderId = id,
    price = totalPrice,
)


fun mapIngredientsOutOfStock(error: OrderPizzaValidationError.IngredientsOutOfStock): IngredientsOutOfStockError {
    val mapped = error.ingredients.map { ingredientOutOfStock ->
        IngredientOutOfStock(
            ingredient = ingredientOutOfStock.ingredient,
            orderedPortions = ingredientOutOfStock.orderedPortions,
            availablePortions = ingredientOutOfStock.availablePortions,
        )
    }

    return IngredientsOutOfStockError(mapped)
}

fun mapTooManyIngredientPortionsOnPizza(outcome: OrderPizzaValidationError.TooManyIngredientPortionsOnPizza) : TooManyIngredientPortionsOnPizzaError {
    return TooManyIngredientPortionsOnPizzaError(
        maxPortions = outcome.maxPortions,
    )
}

