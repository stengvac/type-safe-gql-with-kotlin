package org.example.typesafe.gql.pizza.server

import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaBase
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaIngredient
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaIngredientPortionPrice
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaSize
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaSizePrice

object Pricing {
    val sizes = PizzaSize.entries
        .map { size ->
            PizzaSizePrice(
                size = size,
                price = size.price(),
            )
        }

    val ingredients = PizzaIngredient.entries
        .map { ingredient ->
            PizzaIngredientPortionPrice(
                ingredient = ingredient,
                pricePerPortion = ingredient.price(),
            )
        }

    fun PizzaSize.price() = when (this) {
        PizzaSize.STANDARD -> 120
        PizzaSize.XL -> 160
    }

    fun PizzaIngredient.price() = when (this) {
        PizzaIngredient.SALAMI -> 30
        PizzaIngredient.HAM -> 30
        PizzaIngredient.PINEAPPLE -> 25
        PizzaIngredient.MOZZARELLA -> 30
        PizzaIngredient.OLIVES -> 25
    }

    fun PizzaBase.price() = when (this) {
        PizzaBase.TOMATO -> 15
        PizzaBase.CREAMY -> 15
        PizzaBase.CHOCOLATE -> 20
    }
}
