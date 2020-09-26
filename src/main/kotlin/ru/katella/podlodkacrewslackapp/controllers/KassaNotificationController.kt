package ru.katella.podlodkacrewslackapp.controllers

import com.beust.klaxon.*
import com.google.gson.Gson
import com.sun.org.apache.xpath.internal.operations.Bool
import org.json.JSONObject
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/notification")
class KassaNotificationController {
    @Target(AnnotationTarget.FIELD)
    annotation class KlaxonAmount

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

    val amountConverter = object: Converter {
        override fun canConvert(cls: Class<*>)
                = cls == Map::class.java

        override fun fromJson(jv: JsonValue) =
            if (jv.obj != null) {
                println(jv)
                jv.obj?.get("value")
            } else {
                throw KlaxonException("Couldn't parse amount: ${jv.string}")
            }

        override fun toJson(value: Any): String = ""
    }

    @PostMapping
    @ResponseBody
    fun handleNotification(@RequestBody payload: Map<Any, Any>, request: HttpServletRequest): String {
        val json = Gson().toJson(payload)
        // Валидируем, что запрос пришел с корректного айпишника
        // println(request.getRemoteAddr()) – тут надо будет встроить проверку https://kassa.yandex.ru/developers/using-api/webhooks#ip

        // Разбираем уведомление от Яндекс.Кассы: https://kassa.yandex.ru/developers/using-api/webhooks
        val parsed_response = Klaxon()
            .fieldConverter(KlaxonAmount::class, amountConverter)
            .parse<KassaNotification>(json) ?: null

        // Вытаскиваем из базы информацию о заказе пользователя по ID заказа

        return parsed_response.toString()
    }
}
