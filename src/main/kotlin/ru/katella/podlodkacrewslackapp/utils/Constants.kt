package ru.katella.podlodkacrewslackapp.utils

import com.sun.mail.imap.protocol.ID

class AirTableEndpoint {
    companion object {
        const val PROMO = "Promocodes"
        const val PRODUCT = "Products"
        const val OFFER = "Offers"
    }
}

class AirTableCommon {
    companion object {
        const val RECORD_PAYLOAD = "fields"
        const val FILTER_KEYWORD = "filterByFormula"
    }
}

class AirTableProduct {
    companion object {
        const val ID = "id"
        const val NAME = "Name"
        const val TYPE = "product_type"
        const val LINK = "link_to_product"
        const val ACTIVE_OFFERS = "active_offers"
        const val OFFER_PRICE = "offer_price"
    }
}

class AirTablePromo {
    companion object {
        const val ID = "id"
        const val PRICE_TYPE = "price_type"
        const val IS_ACTIVE = "is_active"
        const val PRICE_TYPE_DECREASE = "price_decrease"
        const val PRICE_TYPE_FIXED = "fixed_price"
        const val PROMO_TYPE_SINGLE = "single"
        const val PROMO_TYPE_UNLIMITED = "unlimited"
    }
}

class AirTableOffer {
    companion object {
        const val ID = "id"
        const val ACTIVE_PROMO = "active_promo_ids"
    }
}