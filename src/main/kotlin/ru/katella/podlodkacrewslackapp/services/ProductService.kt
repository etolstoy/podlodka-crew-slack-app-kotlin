package ru.katella.podlodkacrewslackapp.services

import com.beust.klaxon.*
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import khttp.get
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.ShopConfig

@Service
class ProductService {
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
            @JsonAlias("fields")
            val product: Product
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Product(
            @JsonAlias("id")
            val productId: String,
            @JsonAlias("Name")
            val name: String?,
            @JsonAlias("product_type")
            val type: String,
            @JsonAlias("link_to_product")
            val link: String?,
            @JsonAlias("active_offers")
            val offerIds: List<String>,
            @JsonAlias("offer_price")
            val offerPrices: List<Number>
    )

    fun obtainProducts(type: ProductType): List<Product> {
        val shopConfig = ShopConfig()
        val headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
        )
        println(headers)

        val payload = mapOf(
                "filterByFormula" to "{product_type} = '${type.name.toLowerCase().capitalize()}'"
        )

        val r = get(
                shopConfig.airtableUrl + "Products",
                params = payload,
                headers = headers
        )
        val jsonString = r.jsonObject.toString()

        val mapper = ObjectMapper().registerKotlinModule()

        val result = mapper.readValue<AirTableResponse>(jsonString)
        return result.records.map { it.product }
    }
}