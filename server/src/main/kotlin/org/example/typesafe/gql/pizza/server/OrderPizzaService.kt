package org.example.typesafe.gql.pizza.server

import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaIngredient
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaOrderInput
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaSize
import org.example.typesafe.gql.pizza.server.Pricing.price
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class OrderPizzaService(
    private val storage: IngredientsStorage,
) {

    private val orderId = AtomicInteger(0)

    fun tryOrderPizza(order: PizzaOrderInput): OrderPizzaOutcome {
        val orderedIngredients = mutableMapOf<PizzaIngredient, Int>()
        order.ingredients.forEach { ingredient ->
            val portions = orderedIngredients.computeIfAbsent(ingredient.ingredient) { 0 }
            orderedIngredients[ingredient.ingredient] = (portions + ingredient.portions)
        }

        return when (val validationError = validateOrder(orderedIngredients, order.size.maxIngredientPortions())) {
            null -> orderPizza(order, orderedIngredients)
            else -> validationError
        }
    }

    private fun validateOrder(
        orderedIngredients: Map<PizzaIngredient, Int>,
        maxPortions: Int,
    ): OrderPizzaValidationError? {
        if (orderedIngredients.isEmpty()) {
            return OrderPizzaValidationError.NoIngredientsOnPizza(storage.availableIngredients())
        }

        val orderedPortions = orderedIngredients.values.sum()
        if (orderedPortions > maxPortions) {
            return OrderPizzaValidationError.TooManyIngredientPortionsOnPizza(
                maxPortions = maxPortions,
            )
        }

        val outOfStockIngredients = orderedIngredients.mapNotNull { (ingredient, orderedPortions) ->
            val availablePortions = storage.availablePortions(ingredient)
            if (availablePortions < orderedPortions) {
                OrderPizzaValidationError.IngredientOutOfStock(
                    ingredient = ingredient,
                    orderedPortions = orderedPortions,
                    availablePortions = availablePortions,
                )
            } else null
        }

        if (outOfStockIngredients.isNotEmpty()) {
            return OrderPizzaValidationError.IngredientsOutOfStock(outOfStockIngredients)
        }

        return null
    }

    private fun PizzaSize.maxIngredientPortions(): Int {
        return when (this) {
            PizzaSize.STANDARD -> 10
            PizzaSize.XL -> 15
        }
    }

    private fun orderPizza(order: PizzaOrderInput, orderedIngredients: MutableMap<PizzaIngredient, Int>): PizzaOrderConfirmation {
        var portionsPrice = 0
        orderedIngredients.forEach { (ingredient, portions) ->
            storage.withdrawPortions(ingredient, portions)
            portionsPrice += ingredient.price() * portions
        }

        return PizzaOrderConfirmation(
            id = orderId.incrementAndGet(),
            totalPrice = order.size.price() + order.base.price() + portionsPrice
        )
    }


}
