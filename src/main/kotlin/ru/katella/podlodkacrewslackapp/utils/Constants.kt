package ru.katella.podlodkacrewslackapp.utils

import com.sun.mail.imap.protocol.ID

class AirTableEndpoint {
    companion object {
        const val PROMO = "Promocodes"
        const val PRODUCT = "Products"
        const val OFFER = "Offers"
        const val ORDER = "Orders"
        const val PARTICIPANT = "Participants"
    }
}

class AirTableCommon {
    companion object {
        const val FIELDS_PAYLOAD = "fields"
        const val FILTER_KEYWORD = "filterByFormula"
        const val RECORDS_KEYWORD = "records"
    }
}

class AirTableProduct {
    companion object {
        const val ID = "id"
        const val NAME = "name"
        const val TYPE = "product_type"
        const val LINK = "link"
        const val ACTIVE_OFFERS = "active_offers"
    }
}

class AirTablePromo {
    companion object {
        const val ID = "id"
        const val PRICE_TYPE = "price_type"
        const val IS_ACTIVE = "is_active"
        const val USAGE_LEFT = "usage_left"
        const val PRICE_TYPE_DECREASE = "price_decrease"
        const val PRICE_TYPE_FIXED = "fixed_price"
        const val PROMO_TYPE_LIMITED = "limited"
        const val PROMO_TYPE_UNLIMITED = "unlimited"
    }
}

class AirTableOffer {
    companion object {
        const val ID = "id"
        const val ACTIVE_PROMO = "active_promo_ids"
    }
}