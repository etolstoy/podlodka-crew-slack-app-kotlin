package ru.katella.podlodkacrewslackapp.controllers

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import khttp.post
import khttp.responses.Response
import ru.katella.podlodkacrewslackapp.data.ShopConfig
import ru.katella.podlodkacrewslackapp.data.repositories.Order
import ru.katella.podlodkacrewslackapp.data.repositories.OrderRepository
import java.util.Base64
import ru.katella.podlodkacrewslackapp.utils.KlaxonAmount
import ru.katella.podlodkacrewslackapp.utils.KlaxonConfirmation
import ru.katella.podlodkacrewslackapp.utils.amountConverter
import ru.katella.podlodkacrewslackapp.utils.confirmationConverter
import javax.persistence.Id

@RestController
@RequestMapping("/buy")
class PaymentFormController {

    @Autowired
    lateinit var orderRepository: OrderRepository

    data class Confirmation(
            val id: String,
            @KlaxonAmount
            val amount: String,
            @KlaxonConfirmation
            @Json(name = "confirmation")
            val confirmationUrl: String
    )

    data class OrderBag(
            @Json(name = "customer_email")
            val customerEmail: String,
            val promo: String,
            val orders: Array<SingleOrder>?
    )

    data class SingleOrder(
            @Json(name = "first_name")
            var firstName: String,
            @Json(name = "last_name")
            var lastName: String,
            var email: String,
            @Json(name = "product_id")
            var productId: String
    )

    @PostMapping
    @ResponseBody
    fun createOrder(@RequestBody payload: Map<Any, Any?>): Map<String, String?> {
        // Совершаем платеж в API Кассы
        val r = makeRequestToKassa()

        // Для успешного запроса – отдаем во фронт формочку оплаты и сохраняем статус заказа в кеш
        if (r.statusCode == 200) {
            val jsonString = r.jsonObject.toString()
            val confirmation = Klaxon()
                    .fieldConverter(KlaxonAmount::class, amountConverter)
                    .fieldConverter(KlaxonConfirmation::class, confirmationConverter)
                    .parse<Confirmation>(jsonString) ?: null

            // Сохраняем пейлоад в наш кеш
            if (confirmation?.id != null) {
                cacheOrder(confirmation.id, payload)

                // Отправляем на фронт урл странички оплаты
                val result = mapOf(
                        "confirmation_url" to confirmation?.confirmationUrl
                )
                return result
            }
        }

        return mapOf(
                "Error" to "Some error has happened with response ${r.jsonObject}"
        )
    }

    private fun cacheOrder(orderId: String, payload: Map<Any, Any?>) {
        // Разбираем тело реквеста в модельки
        val json = Gson().toJson(payload)
        val orderBag = Klaxon()
                .parse<OrderBag>(json) ?: null

        if (orderBag?.orders != null) {
            for (order in orderBag.orders) {
                orderRepository.saveAndFlush(Order(
                        orderId,
                        orderBag.customerEmail,
                        order.firstName,
                        order.lastName,
                        order.email,
                        order.productId,
                        "2000"
                ))
            }
        }
    }

    private fun makeRequestToKassa(): Response {
        val shopConfig = ShopConfig()
        val authString: String = Base64.getEncoder().encodeToString("${shopConfig.yandexShopId}:${shopConfig.yandexSecretKey}".toByteArray())
        val payload = mapOf(
            "amount" to mapOf(
                "value" to "3900",
                "currency" to "RUB"
            ),
            "capture" to true,
            "confirmation" to mapOf(
                "type" to "redirect",
                "return_url" to "https://podlodka.io/crew"
            )
        )
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Basic $authString",
            "Idempotence-Key" to java.util.UUID.randomUUID().toString()
        )
        val r = post(
            shopConfig.kassaUrl,
            json = payload,
            headers = headers
        )

        return r
    }

//    private fun cacheOrderToAirtable(orderBag: OrderBag, confirmation: Confirmation?) {
//        val shopConfig = ShopConfig()
//
//        val airtableHeaders = mapOf(
//            "Content-Type" to "application/json",
//            "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
//        )
//        val airtablePayload = mapOf(
//            "records" to arrayOf(
//                mapOf(
//                    "fields" to mapOf<String, String?>(
//                        "ProductId" to productId,
//                        "OrderId" to confirmation?.id,
//                        "Amount" to confirmation?.amount
//                    )
//                )
//            )
//        )
//
//        val r = post(
//            shopConfig.airtableUrl,
//            json = airtablePayload,
//            headers = airtableHeaders
//        )
//    }
}
