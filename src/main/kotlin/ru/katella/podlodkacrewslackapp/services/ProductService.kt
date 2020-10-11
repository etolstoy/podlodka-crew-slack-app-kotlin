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
class ProductService {
    @Autowired
    lateinit var airTableService: AirTableService

    enum class ProductType() {
        EVENT,
        PLAYLIST
    }

    data class AirTableResponse(
            val records: List<Record>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Record(
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

    fun obtainProducts(type: ProductType): List<Product> {
        val payload = mapOf(
                AirTableCommon.FILTER_BY to "{${AirTableProduct.TYPE}} = '${type.name.toLowerCase()}'"
        )
        val jsonString = airTableService.makeGetRequest(AirTableEndpoint.PRODUCT, payload)

        val mapper = ObjectMapper().registerKotlinModule()

        val result = mapper.readValue<AirTableResponse>(jsonString)
        return result.records.map { it.product }
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