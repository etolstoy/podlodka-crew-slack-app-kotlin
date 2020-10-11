package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.repositories.Order
import ru.katella.podlodkacrewslackapp.utils.AirTableCommon
import ru.katella.podlodkacrewslackapp.utils.AirTableEndpoint
import ru.katella.podlodkacrewslackapp.utils.AirTableOrder

@Service
class CRMService {
    @Autowired
    lateinit var airTableService: AirTableService

    fun createOrder(order: Order, status: String): Boolean {
        // Достаем или создаем Participant

        // Достаем Offer

        // Достаем Promo

        // Создаем всю сущность
        val orderPayload = mapOf(
            AirTableCommon.RECORDS to listOf<Map<String, Any>>(
                mapOf(
                    AirTableCommon.FIELDS to mapOf<String, Any>(
                        AirTableOrder.KASSA_ID to order.confirmationId,
                        AirTableOrder.CUSTOMER_EMAIL to order.customerEmail,
                        AirTableOrder.FIRST_NAME to order.firstName,
                        AirTableOrder.LAST_NAME to order.lastName,
                        AirTableOrder.EMAIL to order.email,
                        AirTableOrder.INITIAL_PRICE to order.initialPrice,
                        AirTableOrder.FINAL_PRICE to order.finalPrice,
                        AirTableOrder.KASSA_ORDER_STATUS to status
                    )
                )
            )
        )
        println(orderPayload)
        val r = airTableService.makePostRequest(AirTableEndpoint.ORDER, orderPayload)

        return r.statusCode == 200
    }
}