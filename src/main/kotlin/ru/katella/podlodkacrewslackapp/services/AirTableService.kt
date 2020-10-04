package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import khttp.get
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.ShopConfig

@Service
class AirTableService {
    data class AirTableResponse<T>(
            val records: List<Record<T>>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Record<T>(
            val id: String,
            @JsonAlias("fields")
            val payload: T
    )

    fun getRecords(urlPath: String, payload: Map<String, String>): String {
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
}