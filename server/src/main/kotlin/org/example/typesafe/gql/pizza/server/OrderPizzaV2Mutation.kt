package org.example.typesafe.gql.pizza.server

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import org.example.typesafe.gql.pizza.schema.codegen.DgsConstants
import org.example.typesafe.gql.pizza.schema.codegen.types.IngredientsOutOfStockError
import org.example.typesafe.gql.pizza.schema.codegen.types.NoIngredientsOnPizzaError
import org.example.typesafe.gql.pizza.schema.codegen.types.OrderPizzaResponseV2
import org.example.typesafe.gql.pizza.schema.codegen.types.OrderedPizza
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaOrderInput
import org.example.typesafe.gql.pizza.schema.codegen.types.TooManyIngredientPortionsOnPizzaError

@DgsComponent
class OrderPizzaV2Mutation(
    private val orderPizzaService: OrderPizzaService,
) {

    @DgsMutation(field = DgsConstants.MUTATION.OrderPizza_v2)
    fun orderPizza(order: PizzaOrderInput): OrderPizzaResponseV2 {
        return when (val outcome = orderPizzaService.tryOrderPizza(order)) {
            is PizzaOrderConfirmation -> {
                orderResponseV2(order = outcome.toGql())
            }

            is OrderPizzaValidationError.IngredientsOutOfStock -> {
                orderResponseV2(ingredientsOutOfStock = mapIngredientsOutOfStock(outcome))
            }

            is OrderPizzaValidationError.NoIngredientsOnPizza -> {
                orderResponseV2(noIngredientsOnPizza = NoIngredientsOnPizzaError(outcome.availableIngredients))
            }

            is OrderPizzaValidationError.TooManyIngredientPortionsOnPizza -> {
                val pizzaOvercrowded = mapTooManyIngredientPortionsOnPizza(outcome)
                orderResponseV2(pizzaOvercrowded = pizzaOvercrowded)
            }
        }
    }
}

private fun orderResponseV2(
    order: OrderedPizza? = null,
    ingredientsOutOfStock: IngredientsOutOfStockError? = null,
    pizzaOvercrowded: TooManyIngredientPortionsOnPizzaError? = null,
    noIngredientsOnPizza: NoIngredientsOnPizzaError? = null,
) = OrderPizzaResponseV2(
    order = order,
    outOfStock = ingredientsOutOfStock,
    pizzaOvercrowded = pizzaOvercrowded,
    noIngredientsOnPizza = noIngredientsOnPizza,
)
