package org.example.typesafe.gql.pizza.server

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import org.example.typesafe.gql.pizza.schema.codegen.DgsConstants
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaBase
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaBasePrice
import org.example.typesafe.gql.pizza.schema.codegen.types.PizzaPricing
import org.example.typesafe.gql.pizza.server.Pricing.price

@DgsComponent
class PizzaPricingQuery {

    @DgsQuery(field = DgsConstants.QUERY.Pricing)
    fun ingredientPricing(): PizzaPricing {
        return PizzaPricing(
            ingredients = Pricing.ingredients,
            sizes = Pricing.sizes,
            bases = PizzaBase.entries.map { base ->
                PizzaBasePrice(
                    base = base,
                    price = base.price(),
                )
            }
        )
    }
}
