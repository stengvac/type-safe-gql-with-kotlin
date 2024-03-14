package org.example.typesafe.gql.pizza.server

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import org.example.typesafe.gql.pizza.schema.codegen.DgsConstants
import org.example.typesafe.gql.pizza.schema.codegen.types.NoIngredientsOnPizzaError
import org.example.typesafe.gql.pizza.schema.codegen.types.OrderPizzaResponseV3
import org.example.typesafe.gql.pizza.schema.codegen.types.OrderPizzaValidationErrorResponse
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaOrderInput

@DgsComponent
class OrderPizzaV3Mutation(
    private val orderPizzaService: OrderPizzaService,
) {

    @DgsMutation(field = DgsConstants.MUTATION.OrderPizza_v3)
    fun orderPizza(order: PizzaOrderInput): OrderPizzaResponseV3 {
        return when (val outcome = orderPizzaService.tryOrderPizza(order)) {
            is PizzaOrderConfirmation -> outcome.toGql()
            is OrderPizzaValidationError.IngredientsOutOfStock -> {
                val error = mapIngredientsOutOfStock(outcome)
                OrderPizzaValidationErrorResponse(error)
            }

            is OrderPizzaValidationError.NoIngredientsOnPizza -> {
                val error = NoIngredientsOnPizzaError(
                    availableIngredients = outcome.availableIngredients
                )

                OrderPizzaValidationErrorResponse(error)
            }

            is OrderPizzaValidationError.TooManyIngredientPortionsOnPizza -> {
                val error = mapTooManyIngredientPortionsOnPizza(outcome)

                OrderPizzaValidationErrorResponse(error)
            }
        }
    }
}
