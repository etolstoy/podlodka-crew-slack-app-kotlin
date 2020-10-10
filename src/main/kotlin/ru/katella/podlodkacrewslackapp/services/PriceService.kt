package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.utils.AirTableCommon
import ru.katella.podlodkacrewslackapp.utils.AirTableEndpoint
import ru.katella.podlodkacrewslackapp.utils.AirTableOffer
import ru.katella.podlodkacrewslackapp.utils.AirTablePromo

@Service
class PriceService {
    @Autowired
    lateinit var airTableService: AirTableService

    enum class PromoPriceType(val typeName: String) {
        DECREASE(AirTablePromo.PRICE_TYPE_DECREASE),
        FIXED(typeName = AirTablePromo.PRICE_TYPE_FIXED)
    }

    enum class PromoType(val typeName: String) {
        SINGLE(AirTablePromo.PROMO_TYPE_SINGLE),
        UNLIMITED(AirTablePromo.PROMO_TYPE_UNLIMITED)
    }

    data class AirTablePromoResponse(
            val records: List<PromoRecord>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class PromoRecord(
            val id: String,
            @JsonAlias(AirTableCommon.RECORD_PAYLOAD)
            val promo: Promo
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Promo(
            val id: String,
            val price: Number,
            @JsonAlias(AirTablePromo.PRICE_TYPE)
            val priceType: String,
            val type: String,
            @JsonAlias(AirTablePromo.IS_ACTIVE)
            val isActive: Boolean
    )

    data class AirTableOfferResponse(
            val records: List<OfferRecord>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OfferRecord(
            val id: String,
            @JsonAlias(AirTableCommon.RECORD_PAYLOAD)
            val offer: Offer
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Offer(
            val id: String,
            val price: Number,
            @JsonAlias(AirTableOffer.ACTIVE_PROMO)
            val activePromoIds: List<String>?
    )

    // Здесь мы рассчитываем цену, которую должен заплатить покупатель с учетом своих промокодов
    // Для этого мы должны получить список офферов, каждый запроцессить с промокодом, потом посчитать сумму
    fun getPrice(offerIds: List<String>, promo: String?): Number {
        val offerIdString = "OR(" +
                offerIds.joinToString(separator = ", ") { "{${AirTableOffer.ID}} = '$it'" } +
                ")"
        val payload = mapOf(
                AirTableCommon.FILTER_KEYWORD to offerIdString
        )

        val jsonString = airTableService.getRecords(AirTableEndpoint.OFFER, payload)
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
                AirTableCommon.FILTER_KEYWORD to "{${AirTablePromo.ID}} = '$id'"
        )
        val jsonString = airTableService.getRecords(AirTableEndpoint.PROMO, payload)
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<AirTablePromoResponse>(jsonString)
        return result.records.firstOrNull()?.promo
    }
}