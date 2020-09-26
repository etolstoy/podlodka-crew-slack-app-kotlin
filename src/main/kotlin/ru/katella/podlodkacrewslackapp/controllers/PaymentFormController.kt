package ru.katella.podlodkacrewslackapp.controllers

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import khttp.post
import khttp.responses.Response
import ru.katella.podlodkacrewslackapp.data.ShopConfig
import java.io.StringReader
import java.util.Base64
import ru.katella.podlodkacrewslackapp.utils.KlaxonAmount
import ru.katella.podlodkacrewslackapp.utils.KlaxonConfirmation
import ru.katella.podlodkacrewslackapp.utils.amountConverter
import ru.katella.podlodkacrewslackapp.utils.confirmationConverter


@RestController
@RequestMapping("/buy")
class PaymentFormController {
    data class Confirmation(
            val id: String,
            @KlaxonAmount
            val amount: String,
            @KlaxonConfirmation
            @Json(name = "confirmation")
            val confirmationUrl: String
    )

    @PostMapping
    fun createOrder(@RequestParam(name = "product_id") productId: String): Map<String, String?> {
        // Совершаем платеж в API Кассы
        val r = makeRequestToKassa()

        // Для успешного запроса – отдаем во фронт формочку оплаты и сохраняем статус заказа в кеш
        if (r.statusCode == 200) {
            val jsonString = r.jsonObject.toString()
            val confirmation = Klaxon()
                    .fieldConverter(KlaxonAmount::class, amountConverter)
                    .fieldConverter(KlaxonConfirmation::class, confirmationConverter)
                    .parse<Confirmation>(jsonString) ?: null

            // Сохраняем в промежуточный кеш AirTable инфу о платеже
            cacheOrderToAirtable(productId, confirmation)

            // Отправляем на фронт урл странички оплаты
            val result = mapOf(
                    "confirmation_url" to confirmation?.confirmationUrl
            )
            return result
        }

        return mapOf(
                "Error" to "Some error has happened with response ${r.jsonObject}"
        )
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
        println(r.jsonObject.toString())

        return r
    }

    private fun cacheOrderToAirtable(productId: String, confirmation: Confirmation?) {
        val shopConfig = ShopConfig()

        val airtableHeaders = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
        )
        val airtablePayload = mapOf(
            "records" to arrayOf(
                mapOf(
                    "fields" to mapOf<String, String?>(
                        "ProductId" to productId,
                        "OrderId" to confirmation?.id,
                        "Amount" to confirmation?.amount
                    )
                )
            )
        )

        val r = post(
            shopConfig.airtableUrl,
            json = airtablePayload,
            headers = airtableHeaders
        )
    }
}
