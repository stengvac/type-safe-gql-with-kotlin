package org.example.typesafe.gql.pizza.server

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import org.example.typesafe.gql.pizza.schema.codegen.DgsConstants

@DgsComponent
class RestockMutation(
    private val storage: IngredientsStorage,
) {

    @DgsMutation(field = DgsConstants.MUTATION.Restock)
    fun restock(): Boolean {
        storage.restock()

        return true
    }
}
