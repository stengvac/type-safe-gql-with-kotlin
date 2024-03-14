package org.example.typesafe.gql.pizza.server

import com.netflix.graphql.dgs.DgsComponent
import graphql.GraphqlErrorBuilder
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture

@DgsComponent
class GraphqlErrorHandling : DataFetcherExceptionHandler {

    override fun handleException(
        params: DataFetcherExceptionHandlerParameters
    ): CompletableFuture<DataFetcherExceptionHandlerResult> {

        val error = when (val ex = ExceptionUtils.getRootCause(params.exception)) {
            is GraphqlErrorWithContextException -> {
                val extensions = mapOf(
                    "errorCode" to ex.errorCode,
                    "context" to ex.errorContextJson,
                )

                GraphqlErrorBuilder
                    .newError(params.dataFetchingEnvironment)
                    .extensions(extensions)
                    .message(ex.message ?: "Internal Server Error")
                    .build()
            }

            else -> {
                LoggerFactory.getLogger(GraphqlErrorHandling::class.java).error("", params.exception)
                GraphqlErrorBuilder
                    .newError(params.dataFetchingEnvironment)
                    .message("Internal Server Error")
                    .build()
            }
        }

        val result = DataFetcherExceptionHandlerResult
            .newResult()
            .error(error)
            .build()

        return CompletableFuture.completedFuture(result)
    }
}
