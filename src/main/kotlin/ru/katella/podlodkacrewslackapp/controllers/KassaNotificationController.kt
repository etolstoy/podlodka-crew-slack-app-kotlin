package ru.katella.podlodkacrewslackapp.controllers

import com.beust.klaxon.*
import com.google.gson.Gson
import com.sun.org.apache.xpath.internal.operations.Bool
import org.json.JSONObject
import org.springframework.web.bind.annotation.*
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
