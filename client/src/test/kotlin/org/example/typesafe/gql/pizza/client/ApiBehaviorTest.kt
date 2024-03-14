package org.example.typesafe.gql.pizza.client

import com.apollographql.apollo3.ApolloClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.example.typesafe.gql.pizza.client.type.PizzaBase
import org.example.typesafe.gql.pizza.client.type.PizzaIngredient
import org.example.typesafe.gql.pizza.client.type.PizzaIngredientPortionInput
import org.example.typesafe.gql.pizza.client.type.PizzaOrderInput
import org.example.typesafe.gql.pizza.client.type.PizzaSize


class ApiBehaviorTest : FunSpec() {
    private val client = ApolloClient.Builder()
        .httpServerUrl("http://localhost:8080/graphql")
        .build()

    init {
        test("client v1") {
            val clientV1 = ClientV1(client, ObjectMapper().registerKotlinModule())
            testClient(clientV1, client)
        }

        test("client v2") {
            val clientV2 = ClientV2(client)
            testClient(clientV2, client)
        }

        test("client v3") {
            val clientV3 = ClientV3(client)
            testClient(clientV3, client)
        }
    }

    private suspend fun testClient(pizzeriaClient: PizzeriaClient, client: ApolloClient) {
        client.mutation(RestockMutation()).execute()

        orderWithNoIngredientsOnPizza(pizzeriaClient)
        orderTooManyIngredientPortions(pizzeriaClient)

        orderPizzaSuccessfully(pizzeriaClient, client)

        orderWithIngredientsOutOfStock(pizzeriaClient)
    }

    private suspend fun orderWithIngredientsOutOfStock(pizzeriaClient: PizzeriaClient) {
        val order = PizzaOrderInput(
            size = PizzaSize.STANDARD,
            base = PizzaBase.TOMATO,
            ingredients = listOf(
                PizzaIngredientPortionInput(
                    ingredient = PizzaIngredient.HAM,
                    portions = 6
                ),
            )
        )

        pizzeriaClient.orderPizza(order)
            .shouldBeTypeOf<OrderPizzaOutcome.ValidationError.IngredientsOutOfStock>()
            .ingredients
            .shouldBeSingleton { ingredient ->
                ingredient.ingredient shouldBe PizzaIngredient.HAM
                ingredient.orderedPortions shouldBe 6
                ingredient.availablePortions shouldBe 5
            }
    }

    private suspend fun orderTooManyIngredientPortions(pizzeriaClient: PizzeriaClient) {
        val order = PizzaOrderInput(
            size = PizzaSize.STANDARD,
            base = PizzaBase.TOMATO,
            ingredients = listOf(
                PizzaIngredientPortionInput(
                    ingredient = PizzaIngredient.HAM,
                    portions = 5
                ),
                PizzaIngredientPortionInput(
                    ingredient = PizzaIngredient.PINEAPPLE,
                    portions = 5,
                ),
                PizzaIngredientPortionInput(
                    ingredient = PizzaIngredient.MOZZARELLA,
                    portions = 5,
                )
            )
        )

        val error = pizzeriaClient.orderPizza(order)
            .shouldBeTypeOf<OrderPizzaOutcome.ValidationError.TooManyIngredientPortions>()
        error.maxAllowedPortions shouldBe 10
    }

    private suspend fun orderPizzaSuccessfully(pizzeriaClient: PizzeriaClient, client: ApolloClient) {
        val validOrder = PizzaOrderInput(
            size = PizzaSize.STANDARD,
            base = PizzaBase.TOMATO,
            ingredients = listOf(
                PizzaIngredientPortionInput(
                    ingredient = PizzaIngredient.PINEAPPLE,
                    portions = 3,
                ),
                PizzaIngredientPortionInput(
                    ingredient = PizzaIngredient.HAM,
                    portions = 5,
                ),
                PizzaIngredientPortionInput(
                    ingredient = PizzaIngredient.SALAMI,
                    portions = 2,
                )
            )
        )

        val confirmation = pizzeriaClient.orderPizza(validOrder).shouldBeTypeOf<OrderPizzaOutcome.Confirmation>()
        confirmation.orderId shouldBeGreaterThan 0
        val expectedPrice = computeExpectedTotalPrice(validOrder, client)
        confirmation.totalPrice shouldBe expectedPrice
    }

    private suspend fun orderWithNoIngredientsOnPizza(pizzeriaClient: PizzeriaClient) {
        val order = PizzaOrderInput(
            size = PizzaSize.STANDARD,
            base = PizzaBase.TOMATO,
            ingredients = listOf()
        )
        pizzeriaClient.orderPizza(order)
            .shouldBeTypeOf<OrderPizzaOutcome.ValidationError.NoIngredientsOnPizza>()
            .availableIngredients shouldContainExactlyInAnyOrder PizzaIngredient.entries.minus(PizzaIngredient.UNKNOWN__)
    }

    private suspend fun computeExpectedTotalPrice(order: PizzaOrderInput, client: ApolloClient): Int {
        val pricing = client.query(PricingQuery()).execute().data!!.pricing
        val sizePrice = pricing.sizes.first { sizePrice -> sizePrice.size == order.size }.price
        val basePrice = pricing.bases.first { baseSize -> baseSize.base == order.base }.price
        val ingredientPortionPrice = pricing.ingredients.associate { it.ingredient to it.pricePerPortion }
        val portionsPrice = order.ingredients.sumOf { input ->
            ingredientPortionPrice.getValue(input.ingredient) * input.portions
        }

        return sizePrice + basePrice + portionsPrice
    }
}
