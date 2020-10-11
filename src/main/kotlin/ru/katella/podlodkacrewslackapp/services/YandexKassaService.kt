package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import khttp.get
import khttp.post
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.ShopConfig
import java.util.*

@Service
class YandexKassaService {
    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KassaPaymentStatusResponse(
        val id: String,
        val status: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KassaOrderResponse(
            val id: String,
            val amount: KassaOrderResponseAmount,
            val confirmation: KassaOrderResponseConfirmation
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KassaOrderResponseAmount(
            val value: Number
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    private data class KassaOrderResponseConfirmation(
            @JsonAlias("confirmation_url")
            val url: String
    )

    data class Confirmation(
            val id: String,
            val amount: String,
            val confirmationUrl: String
    )

    val shopConfig = ShopConfig()

    fun requestPaymentConfirmation(price: Number): Confirmation? {
        val payload = mapOf(
                "amount" to mapOf(
                        "value" to price.toString(),
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
                "Authorization" to "Basic ${kassaAuthString()}",
                "Idempotence-Key" to java.util.UUID.randomUUID().toString()
        )
        val r = post(
                shopConfig.kassaUrl,
                json = payload,
                headers = headers
        )
        if (r.statusCode == 200) {
            val jsonString = r.jsonObject.toString()
            val mapper = ObjectMapper().registerKotlinModule()
            val result = mapper.readValue<KassaOrderResponse>(jsonString)

            return Confirmation(
                    id = result.id,
                    confirmationUrl = result.confirmation.url,
                    amount = result.amount.value.toString()
            )
        }
        return null
    }

    fun getPaymentStatus(id: String): String? {
        // https://kassa.yandex.ru/developers/api#get_payment
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Basic ${kassaAuthString()}",
            "Idempotence-Key" to java.util.UUID.randomUUID().toString()
        )
        val r = get(
            shopConfig.kassaUrl + id,
            headers = headers)
        if (r.statusCode == 200) {
            val jsonString = r.jsonObject.toString()
            val mapper = ObjectMapper().registerKotlinModule()
            val result = mapper.readValue<KassaPaymentStatusResponse>(jsonString)
            return result.status
        }
        return null
    }

    private fun kassaAuthString() = Base64.getEncoder().encodeToString("${shopConfig.yandexShopId}:${shopConfig.yandexSecretKey}".toByteArray())
}