package org.example.typesafe.gql.pizza.server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PizzaApplication

fun main(args: Array<String>) {
    runApplication<PizzaApplication>(*args) {}
}
