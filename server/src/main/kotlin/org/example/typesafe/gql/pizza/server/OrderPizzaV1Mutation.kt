package org.example.typesafe.gql.pizza.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import org.example.typesafe.gql.pizza.schema.codegen.DgsConstants
import org.example.typesafe.gql.pizza.schema.codegen.types.NoIngredientsOnPizzaError
import org.example.typesafe.gql.pizza.schema.codegen.types.OrderedPizza
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaOrderInput

@DgsComponent
class OrderPizzaV1Mutation(
    private val orderPizzaService: OrderPizzaService,
    private val objectMapper: ObjectMapper,
) {

    @DgsMutation(field = DgsConstants.MUTATION.OrderPizza_v1)
    fun orderPizza(order: PizzaOrderInput): OrderedPizza {
        return when (val outcome = orderPizzaService.tryOrderPizza(order)) {
            is PizzaOrderConfirmation -> outcome.toGql()
            is OrderPizzaValidationError.IngredientsOutOfStock -> {
                // this is not entirely correct as it use error from version 2 - it should be another object
                val errorContext = mapIngredientsOutOfStock(outcome)

                throw GraphqlErrorWithContextException(
                    message = "Ingredients out of stock.",
                    errorCode = "INGREDIENTS_OUT_OF_STOCK",
                    errorContextJson = objectMapper.convertValue(errorContext, Map::class.java)
                )
            }

            is OrderPizzaValidationError.NoIngredientsOnPizza -> {
                // this is not entirely correct as it use error from version 2 - it should be another object
                val errorContext = NoIngredientsOnPizzaError(outcome.availableIngredients)

                throw GraphqlErrorWithContextException(
                    message = "Add some ingredients on Pizza Please!",
                    errorCode = "NO_INGREDIENTS_ADDED",
                    errorContextJson = objectMapper.convertValue(errorContext, Map::class.java)
                )
            }

            is OrderPizzaValidationError.TooManyIngredientPortionsOnPizza -> {
                // this is not entirely correct as it use error from version 2 - it should be another object
                val errorContext = mapTooManyIngredientPortionsOnPizza(outcome)

                throw GraphqlErrorWithContextException(
                    message = "Too many ingredients on Pizza.",
                    errorCode = "TOO_MANY_INGREDIENT_PORTIONS",
                    errorContextJson = objectMapper.convertValue(errorContext, Map::class.java)
                )
            }
        }
    }
}

class GraphqlErrorWithContextException(
    override val message: String?,
    val errorCode: String,
    val errorContextJson: Map<*, *>,
) : RuntimeException()
