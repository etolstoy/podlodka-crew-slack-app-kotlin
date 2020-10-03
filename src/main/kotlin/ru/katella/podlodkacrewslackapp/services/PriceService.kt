package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import khttp.get
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.ShopConfig
import ru.katella.podlodkacrewslackapp.utils.defaultAirTableHeaders

@Service
class PriceService {
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
        val shopConfig = ShopConfig()
        val headers = defaultAirTableHeaders()

        val offerIdString = "OR(" +
                offerIds.joinToString(separator = ", ") { "{id} = '$it'" } +
                ")"
        val payload = mapOf(
                "filterByFormula" to offerIdString
        )

        val r = get(
                shopConfig.airtableUrl + "Offers",
                params = payload,
                headers = headers
        )
        val jsonString = r.jsonObject.toString()
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<AirTableOfferResponse>(jsonString)
        val offers = result.records.map { it.offer }

        var allOffersPrice = offers.fold(0.0) { sum, offer ->
            sum + offer.price.toDouble()
        }

        // Проверяем промокод. Ищем его в таблице, затем проверяем на is_active.
        // Затем идем по всем событиям и проверяем, что у оффера есть промокод с таким айди
        if (promo != null) {
            val promo = getPromo(promo)
            if (promo == null) {
                println("Промокод не найден")
                return allOffersPrice
            }
            if (promo.isActive == false) {
                println("Промокод не активен")
                return allOffersPrice
            }

            var priceWithPromo = 0.0
            var availablePromoUses = 1
            offers.forEach {
                // Этот промокод подходит к этому офферу, значит надо чекнуть его тип и поменять цену
                if (it.activePromoIds?.contains(promo.id) == true && availablePromoUses > 0) {
                    // Если это промокод с фикспрайсом, то просто плюсуем его стоимость к сумме
                    if (promo.priceType == PromoPriceType.FIXED.typeName) {
                        priceWithPromo += promo.price.toDouble()
                    }

                    // Если тип – Decrease, то уменьшаем стоимость предмета на эту сумму, но чекаем на то, что не меньше нуля
                    if (promo.priceType == PromoPriceType.DECREASE.typeName) {
                        var resultPrice = it.price.toDouble() - promo.price.toDouble()
                        println(resultPrice)
                        if (resultPrice < 0) {
                            resultPrice = 0.0
                        }
                        priceWithPromo += resultPrice
                    }

                    // Уменьшаем количество использований промокода
                    if (promo.type != PromoType.UNLIMITED.typeName) {
                        availablePromoUses -= 1
                    }
                } else {
                    priceWithPromo += it.price.toDouble()
                }
            }

            allOffersPrice = priceWithPromo
        }

        return allOffersPrice
    }

    private fun getPromo(id: String): Promo? {
        val shopConfig = ShopConfig()
        val headers = defaultAirTableHeaders()

        val payload = mapOf(
                "filterByFormula" to "{id} = '$id'"
        )

        val r = get(
                shopConfig.airtableUrl + "Promocodes",
                params = payload,
                headers = headers
        )
        val jsonString = r.jsonObject.toString()
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<AirTablePromoResponse>(jsonString)
        return result.records.firstOrNull()?.promo
    }
}