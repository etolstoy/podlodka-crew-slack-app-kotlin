package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import khttp.get
import khttp.patch
import khttp.post
import org.json.JSONObject
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.ShopConfig
import ru.katella.podlodkacrewslackapp.utils.AirTableCommon

@Service
class AirTableService {
    data class AirTableResponse<T>(
            val records: List<Record<T>>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Record<T>(
            val id: String,
            @JsonAlias(AirTableCommon.RECORD_PAYLOAD)
            val payload: T
    )

    fun makeGetRequest(urlPath: String, payload: Map<String, String>): String {
        val shopConfig = ShopConfig()
        val headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
        )
        val r = get(
                shopConfig.airtableUrl + urlPath,
                params = payload,
                headers = headers
        )
        val jsonString = r.jsonObject.toString()
        return jsonString
    }

    fun makePatchRequest(urlPath: String, payload: Map<String, Any>): String {
        val shopConfig = ShopConfig()
        val headers = mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
        )
        val r = patch(
                shopConfig.airtableUrl + urlPath,
                data = JSONObject(payload),
                headers = headers
        )
        val jsonString = r.jsonObject.toString()
        return jsonString
    }

    fun makePostRequest(urlPath: String, payload: Map<String, Any>): String {
        val shopConfig = ShopConfig()
        val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
        )
        val r = post(
            shopConfig.airtableUrl + urlPath,
            data = JSONObject(payload),
            headers = headers
        )
        val jsonString = r.jsonObject.toString()
        return jsonString
    }
}