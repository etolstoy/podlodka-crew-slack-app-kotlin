package ru.katella.podlodkacrewslackapp.controllers

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import khttp.post
import ru.katella.podlodkacrewslackapp.data.ShopConfig
import java.io.StringReader
import java.util.Base64


@RestController
@RequestMapping("/buy")
class PaymentFormController {
    data class Confirmation(
            @Json(name = "confirmation")
            val payload: Map<String, String>
    )

    @PostMapping
    fun createOrder(@RequestParam(name = "product_id") productId: String): Map<String, String> {
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
        if (r.statusCode == 200) {
            val parsed_response = Klaxon().parse<Confirmation>(r.jsonObject.toString()) ?: null
            val url = parsed_response?.payload?.get("confirmation_url") ?: "no_url"
            val result = mapOf(
                    "confirmation_url" to url
            )
            // Сохраняем в промежуточный кеш AirTable инфу о платеже
            val airtableHeaders = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
            )
            val airtablePayload = mapOf(
                "records" to arrayOf(
                    mapOf(
                        "fields" to mapOf<String, String>(
                            "ProductId" to productId,
                            "OrderId" to "123",
                            "Amount" to "2000"
                        )
                    )
                )
            )

            val r = post(
                shopConfig.airtableUrl,
                json = airtablePayload,
                headers = airtableHeaders
            )
            println(r.jsonObject.toString())

            // Отправляем на фронт урл странички оплаты
            return result
        }

        return mapOf(
                "text" to "error"
        )
    }
}
