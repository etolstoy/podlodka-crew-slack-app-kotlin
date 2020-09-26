package ru.katella.podlodkacrewslackapp.controllers

import com.beust.klaxon.*
import com.google.gson.Gson
import com.sun.org.apache.xpath.internal.operations.Bool
import khttp.post
import khttp.get
import org.json.JSONObject
import org.springframework.web.bind.annotation.*
import ru.katella.podlodkacrewslackapp.data.ShopConfig
import javax.servlet.http.HttpServletRequest

import ru.katella.podlodkacrewslackapp.utils.KlaxonAmount
import ru.katella.podlodkacrewslackapp.utils.amountConverter

@RestController
@RequestMapping("/notification")
class KassaNotificationController {
    data class KassaNotificationDetails(
        val id: String,
        val paid: Boolean,
        @KlaxonAmount
        val amount: String,
        val description: String,
        val created_at: String,
        val expires_at: String
    )

    data class KassaNotification(
        val type: String,
        val event: String,
        @Json(name = "object")
        val details: KassaNotificationDetails
    )

    data class Order(
        val orderId: String,
        val productId: String,
        val amount: String,
        val customerEmail: String
    )

    @PostMapping
    @ResponseBody
    fun handleNotification(@RequestBody payload: Map<Any, Any>, request: HttpServletRequest): String {
        val json = Gson().toJson(payload)
        // Валидируем, что запрос пришел с корректного айпишника
        // println(request.getRemoteAddr()) – тут надо будет встроить проверку https://kassa.yandex.ru/developers/using-api/webhooks#ip

        // Разбираем уведомление от Яндекс.Кассы: https://kassa.yandex.ru/developers/using-api/webhooks
        val notification = Klaxon()
            .fieldConverter(KlaxonAmount::class, amountConverter)
            .parse<KassaNotification>(json) ?: null

        if (notification?.event != "payment.succeeded") {
            return "Payment isn't succeded still"
        }

        // Вытаскиваем из базы информацию о заказе пользователя по ID заказа
        val shopConfig = ShopConfig()

        val airtableHeaders = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
        )
        val orderId = notification?.details?.id
        val airtablePayload = mapOf(
                "maxRecords" to "1",
                "fields" to "ProductId",
                "fields" to "OrderId",
                "filterByFormula" to "{OrderId} = '$orderId'"
        )

        val r = get(
                shopConfig.airtableUrl,
                params = airtablePayload,
                headers = airtableHeaders
        )
        val jsonString = r.jsonObject.toString()


        return r.jsonObject.toString()
        // Сериализовать ответ AirTable в свой объект
        // Сохранить в базу новый статус
        // Отправить на почту покупателю плейлист
    }
}
