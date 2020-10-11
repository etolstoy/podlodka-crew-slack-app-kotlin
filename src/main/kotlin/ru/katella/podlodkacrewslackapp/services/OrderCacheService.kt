package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.repositories.Order
import ru.katella.podlodkacrewslackapp.data.repositories.OrderRepository

@Service
class OrderCacheService {
    @Autowired
    lateinit var orderRepository: OrderRepository

    data class BulkOrder(
            val customerEmail: String,
            val promo: PriceService.Promo?,
            val orders: Array<SingleOrder>?
    )

    data class SingleOrder(
            var firstName: String,
            var lastName: String,
            var email: String,
            var offerId: String,
            var initialPrice: String? = "0",
            var resultPrice: String? = "0"
    )

    fun obtainCachedOrders(): List<Order> = orderRepository.findAll()

    fun cacheOrders(orders: List<Order>?) {
        if (orders != null) {
            orders.forEach {
                orderRepository.saveAndFlush(it)
            }
        }
    }
}