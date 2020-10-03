package ru.katella.podlodkacrewslackapp.data.repositories

import com.beust.klaxon.Json
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface OrderRepository: JpaRepository<Order, String> {
    fun findByOrderId(id: String): Order
}

@Entity
@Table(name = "product_order")
data class Order(
        @Id val orderId: String,
        var customerEmail: String,
        var firstName: String,
        var lastName: String,
        var email: String,
        var productId: String,
        var amount: String
)