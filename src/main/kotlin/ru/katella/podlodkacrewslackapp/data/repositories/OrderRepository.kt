package ru.katella.podlodkacrewslackapp.data.repositories

import com.beust.klaxon.Json
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface OrderRepository: JpaRepository<Order, String> {
    fun findByOfferId(id: String): Order
}

@Entity
@Table(name = "offer_order")
data class Order(
    @Id val id: String,
    val confirmationId: String,
    var customerEmail: String,
    var firstName: String,
    var lastName: String,
    var email: String,
    var offerId: String,
    var initialPrice: String,
    var resultPrice: String
)