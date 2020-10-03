package ru.katella.podlodkacrewslackapp.utils

import ru.katella.podlodkacrewslackapp.data.ShopConfig

fun defaultAirTableHeaders(): Map<String, String> {
    val shopConfig = ShopConfig()
    val headers = mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${shopConfig.airtableSecretKey}"
    )
    return headers
}