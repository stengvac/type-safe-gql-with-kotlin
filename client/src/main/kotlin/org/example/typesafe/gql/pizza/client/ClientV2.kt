package org.example.typesafe.gql.pizza.client

import com.apollographql.apollo3.ApolloClient
import org.example.typesafe.gql.pizza.client.type.PizzaOrderInput

class ClientV2(
    private val client: ApolloClient,
) : PizzeriaClient {

    override suspend fun orderPizza(order: PizzaOrderInput): OrderPizzaOutcome {
        val response = client.mutation(OrderPizzaV2Mutation(order)).execute()

        return when {
            response.data?.orderPizza_v2 != null -> {
                val orderResult = response.data!!.orderPizza_v2
                when {
                    orderResult.order != null -> mapConfirmation(orderResult.order)
                    else -> mapValidationErrors(orderResult)
                }
            }
            response.hasErrors() -> error("received errors which are not expected")
            // should not happen
            else -> error("Nor error not data received in response")
        }
    }

    private fun mapValidationErrors(orderResult: OrderPizzaV2Mutation.OrderPizza_v2): OrderPizzaOutcome.ValidationError {
        // hmm, what if two fields are non null at same time...
        return when {
            orderResult.noIngredientsOnPizza != null -> mapNoIngredients(orderResult.noIngredientsOnPizza)
            orderResult.pizzaOvercrowded != null -> mapTooManyIngredientPortions(orderResult.pizzaOvercrowded)
            orderResult.outOfStock != null -> mapIngredientsOutOfStock(orderResult.outOfStock)
            else -> error("order result did not match any expected errors...")
        }
    }
}
