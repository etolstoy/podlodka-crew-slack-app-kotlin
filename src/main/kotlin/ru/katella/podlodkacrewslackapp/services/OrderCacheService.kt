package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.controllers.PaymentFormController
import ru.katella.podlodkacrewslackapp.data.repositories.Order
import ru.katella.podlodkacrewslackapp.data.repositories.OrderRepository

@Service
class OrderCacheService {
    @Autowired
    lateinit var orderRepository: OrderRepository

    data class OrderBag(
            @JsonAlias("customer_email")
            val customerEmail: String,
            val promo: String,
            val orders: Array<SingleOrder>?
    )

    data class SingleOrder(
            @JsonAlias("first_name")
            var firstName: String,
            @JsonAlias("last_name")
            var lastName: String,
            var email: String,
            @JsonAlias("offer_id")
            var offerId: String
    )

    fun obtainCachedOrders(): List<Order> = orderRepository.findAll()

    fun cacheOrder(orderBag: OrderBag, confirmationId: String) {
        if (orderBag.orders != null) {
            orderBag.orders.forEach {
                orderRepository.saveAndFlush(Order(
                        id = java.util.UUID.randomUUID().toString(),
                        confirmationId = confirmationId,
                        customerEmail = orderBag.customerEmail,
                        firstName = it.firstName,
                        lastName = it.lastName,
                        email = it.email,
                        offerId = it.offerId
                ))
            }
        }
    }
}