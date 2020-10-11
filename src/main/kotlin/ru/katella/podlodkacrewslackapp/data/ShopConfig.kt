package ru.katella.podlodkacrewslackapp.data

class ShopConfig() {
    // Keys
    val yandexShopId: String? = System.getenv("YANDEX_SHOP_ID")
    val yandexSecretKey: String? = System.getenv("YANDEX_SECRET_KEY")
    val airtableSecretKey: String? = System.getenv("AIRTABLE_SECRET_KEY")

    // Urls
    val kassaUrl: String = "https://payment.yandex.net/api/v3/payments/"
    val airtableUrl: String = "https://api.airtable.com/v0/appAec9VCWrRQ1KOW/"
}