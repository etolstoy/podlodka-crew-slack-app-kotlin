package ru.katella.podlodkacrewslackapp.controllers

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import khttp.post
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
    fun createOrder(@RequestParam(name = "event_id") eventId: String): Map<String, String> {
        val kassaUrl = "https://payment.yandex.net/api/v3/payments"
        val shopId = "11"
        val secretKey = "test_"
        val authString: String = Base64.getEncoder().encodeToString("$shopId:$secretKey".toByteArray())

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
                kassaUrl,
                json = payload,
                headers = headers
        )
        if (r.statusCode == 200) {
            val parsed_response = Klaxon().parse<Confirmation>(r.jsonObject.toString()) ?: null
            val url = parsed_response?.payload?.get("confirmation_url") ?: "no_url"
            val result = mapOf(
                    "confirmation_url" to url
            )
            return result
        }

        return mapOf(
                "text" to "error"
        )
    }
}
