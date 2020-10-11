package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import khttp.get
import khttp.patch
import khttp.post
import khttp.responses.Response
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
            @JsonAlias(AirTableCommon.FIELDS)
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

    fun makePostRequest(urlPath: String, payload: Map<String, Any>): Response {
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
        return r
    }
}