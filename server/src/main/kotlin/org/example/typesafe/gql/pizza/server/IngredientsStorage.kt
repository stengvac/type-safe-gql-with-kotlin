package org.example.typesafe.gql.pizza.server

import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaIngredient
import org.springframework.stereotype.Component

@Component
class IngredientsStorage {

    private var availableIngredientPortions: MutableMap<PizzaIngredient, Int> = mutableMapOf()

    init {
        restock()
    }

    fun availableIngredients(): List<PizzaIngredient> {
        return availableIngredientPortions
            .filter { ingredientPortions -> ingredientPortions.value > 0}
            .map { ingredientPortions -> ingredientPortions.key }
    }

    fun availablePortions(ingredient: PizzaIngredient) = availableIngredientPortions.getValue(ingredient)

    fun withdrawPortions(ingredient: PizzaIngredient, portions: Int) {
        val availablePortions = availablePortions(ingredient)
        if (availablePortions < portions) {
            error("Ingredient=$ingredient has available=$availablePortions, but was requested=$portions portions.")
        }

        availableIngredientPortions[ingredient] = availablePortions - portions
    }

    final fun restock() {
        // 10 portions per each ingredient
        availableIngredientPortions = PizzaIngredient.entries
            .associateWith { 10 }
            .toMutableMap()
    }
}
