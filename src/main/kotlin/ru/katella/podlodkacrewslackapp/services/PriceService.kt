package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PriceService {
    @Autowired
    lateinit var airTableService: AirTableService

    enum class PromoPriceType(val typeName: String) {
        DECREASE("price_decrease"),
        FIXED(typeName = "fixed_price")
    }

    enum class PromoType(val typeName: String) {
        SINGLE("single"),
        UNLIMITED("unlimited")
    }

    data class AirTablePromoResponse(
            val records: List<PromoRecord>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PromoRecord(
            val id: String,
            @JsonAlias("fields")
            val promo: Promo
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Promo(
            val id: String,
            val price: Number,
            @JsonAlias("price_type")
            val priceType: String,
            val type: String,
            @JsonAlias("is_active")
            val isActive: Boolean
    )

    data class AirTableOfferResponse(
            val records: List<OfferRecord>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OfferRecord(
            val id: String,
            @JsonAlias("fields")
            val offer: Offer
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Offer(
            val id: String,
            val price: Number,
            @JsonAlias("active_promo_ids")
            val activePromoIds: List<String>?
    )

    // Здесь мы рассчитываем цену, которую должен заплатить покупатель с учетом своих промокодов
    // Для этого мы должны получить список офферов, каждый запроцессить с промокодом, потом посчитать сумму
    fun getPrice(offerIds: List<String>, promo: String?): Number {
        val offerIdString = "OR(" +
                offerIds.joinToString(separator = ", ") { "{id} = '$it'" } +
                ")"
        val payload = mapOf(
                "filterByFormula" to offerIdString
        )
        val jsonString = airTableService.getRecords("Offers", payload)
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<AirTableOfferResponse>(jsonString)
        val offers = result.records.map { it.offer }

        val promoObject = getPromo(promo)

        val priceCalculator = PromoCalculatorService()
        val price = priceCalculator.countPrice(offers, promoObject)
        return price
    }

    private fun getPromo(id: String?): Promo? {
        if (id == null) {
            return null
        }
        val payload = mapOf(
                "filterByFormula" to "{id} = '$id'"
        )
        val jsonString = airTableService.getRecords("Promocodes", payload)
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<AirTablePromoResponse>(jsonString)
        return result.records.firstOrNull()?.promo
    }
}