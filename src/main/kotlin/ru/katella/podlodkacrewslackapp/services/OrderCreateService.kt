package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.repositories.Order

@Service
class OrderCreateService {
    @Autowired
    lateinit var airTableService: AirTableService

    fun createOrder(order: Order) {
//        // Сохраняем Recepient в Users
//        val recepientPayload = mapOf(
//            "records" to listOf<Map<String, Any>>(
//                mapOf(
//                    "fields" to mapOf<String, String>(
//                        "email" to order.email,
//                        "first_name" to order.firstName,
//                        "last_name" to order.lastName
//                    )
//                )
//            )
//        )
//        airTableService.makePostRequest(AirTableEndpoint.USER, recepientPayload)
//
//        // Сохраняем Customer в Users
//        val customerPayload = mapOf(
//            "records" to listOf<Map<String, Any>>(
//                mapOf(
//                    "fields" to mapOf<String, String>(
//                        "email" to order.customerEmail
//                    )
//                )
//            )
//        )
//        airTableService.makePostRequest(AirTableEndpoint.USER, customerPayload)

        // Сохраняем отдельные ордеры в Orders
        val orderPayload = mapOf(
            "records" to listOf<Map<String, Any>>(
                mapOf(
                    "fields" to mapOf<String, String>(
                        "OrderId" to order.confirmationId,
                        "Amount" to order.initialPrice,

                        "email" to order.customerEmail
                    )
                )
            )
        )

        // Сохраняем весь заказ в KassaOrder
    }
}