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
import ru.katella.podlodkacrewslackapp.utils.AirTableProduct

@Service
class OfferService {
    @Autowired
    lateinit var airTableService: AirTableService

    enum class ProductType() {
        CONFERENCE,
        PLAYLIST
    }

    data class AirTableProductResponse(
            val records: List<ProductRecord>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ProductRecord(
            val id: String,
            @JsonAlias(AirTableCommon.FIELDS)
            val product: Product
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Product(
            @JsonAlias(AirTableProduct.ID)
            val productId: String,
            @JsonAlias(AirTableProduct.NAME)
            val name: String?,
            @JsonAlias(AirTableProduct.TYPE)
            val type: String,
            @JsonAlias(AirTableProduct.LINK)
            val link: String?,
            @JsonAlias(AirTableProduct.ACTIVE_OFFERS)
            val offerIds: List<String>
    )

    fun obtainOffers(): List<PriceService.Offer> {
        val jsonString = airTableService.makeGetRequest(AirTableEndpoint.OFFER, null)
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<PriceService.AirTableOfferResponse>(jsonString)
        return result.records.map { it.offer }
    }

    fun obtainOffers(type: ProductType): List<PriceService.Offer> {
        val payload = mapOf(
                AirTableCommon.FILTER_BY to "{${AirTableOffer.PRODUCT_TYPE}} = '${type.name.toLowerCase()}'"
        )
        val jsonString = airTableService.makeGetRequest(AirTableEndpoint.OFFER, payload)
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<PriceService.AirTableOfferResponse>(jsonString)
        return result.records.map { it.offer }
    }

    fun obtainOffers(productId: String): List<PriceService.Offer> {
        val payload = mapOf(
            AirTableCommon.FILTER_BY to "{${AirTableOffer.PRODUCT_ID}} = '${productId}'"
        )
        val jsonString = airTableService.makeGetRequest(AirTableEndpoint.OFFER, payload)
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<PriceService.AirTableOfferResponse>(jsonString)
        return result.records.map { it.offer }
    }

    fun validateOffers(offerIds: List<String>): Boolean {
        // Валидация простая – запрашиваем у AirTable офферы по их Id. Количество вернувшихся офферов должно быть равно количеству Id
        val offerIdString = "OR(" +
                offerIds.joinToString(separator = ", ") { "{${AirTableOffer.ID}} = '$it'" } +
                ")"
        val payload = mapOf(
                AirTableCommon.FILTER_BY to offerIdString
        )
        val jsonString = airTableService.makeGetRequest(AirTableEndpoint.OFFER, payload)
        val mapper = ObjectMapper().registerKotlinModule()
        val result = mapper.readValue<PriceService.AirTableOfferResponse>(jsonString)
        val resultCount = result.records.count()

        return resultCount == offerIds.distinct().count()
    }
}