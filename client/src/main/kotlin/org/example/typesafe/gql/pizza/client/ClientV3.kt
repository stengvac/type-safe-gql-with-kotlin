package org.example.typesafe.gql.pizza.client

import com.apollographql.apollo3.ApolloClient
import org.example.typesafe.gql.pizza.client.OrderPizzaV3Mutation.OrderPizzaValidationErrorResponseOrderPizza_v3
import org.example.typesafe.gql.pizza.client.OrderPizzaV3Mutation.OrderedPizzaOrderPizza_v3
import org.example.typesafe.gql.pizza.client.OrderPizzaV3Mutation.OtherOrderPizza_v3
import org.example.typesafe.gql.pizza.client.type.PizzaOrderInput

class ClientV3(
    private val client: ApolloClient,
) : PizzeriaClient {

    override suspend fun orderPizza(order: PizzaOrderInput): OrderPizzaOutcome {
        val response = client.mutation(OrderPizzaV3Mutation(order)).execute()

        return when {
            response.data?.orderPizza_v3 != null -> {
                when (val data = response.data!!.orderPizza_v3) {
                    is OrderedPizzaOrderPizza_v3 -> mapConfirmation(data)
                    is OrderPizzaValidationErrorResponseOrderPizza_v3 -> mapValidationErrors(data.error)
                    is OtherOrderPizza_v3 -> throwOnOther(data.__typename)
                }
            }
            response.hasErrors() -> error("received errors which are not expected")
            // should not happen
            else -> error("Nor error not data received in response")
        }
    }

    private fun mapValidationErrors(error: OrderPizzaV3Mutation.Error): OrderPizzaOutcome.ValidationError {
        return when (error) {
            is OrderPizzaV3Mutation.IngredientsOutOfStockErrorError -> mapIngredientsOutOfStock(error)
            is OrderPizzaV3Mutation.NoIngredientsOnPizzaErrorError -> mapNoIngredients(error)
            is OrderPizzaV3Mutation.TooManyIngredientPortionsOnPizzaErrorError -> mapTooManyIngredientPortions(error)
            is OrderPizzaV3Mutation.OtherError -> throwOnOther(error.__typename)
        }
    }

    private fun throwOnOther(typename: String): Nothing {
        error("Server added to union type new types. Client is not ready for them. typeName=$typename.")
    }
}
