package ru.katella.podlodkacrewslackapp.utils

import com.sun.mail.imap.protocol.ID

class AirTableEndpoint {
    companion object {
        const val PROMO = "Promocodes"
        const val PRODUCT = "Products"
        const val OFFER = "Offers"
        const val ORDER = "Orders"
        const val PARTICIPANT = "Participant"
    }
}

class AirTableCommon {
    companion object {
        const val FIELDS = "fields"
        const val FILTER_BY = "filterByFormula"
        const val RECORDS = "records"
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

class AirTableParticipant {
    companion object {
        const val EMAIL = "Email"
        const val NAME = "Name"
        const val SURNAME = "Surname"
    }
}

class AirTableOrder {
    companion object {
        const val KASSA_ID = "kassa_order_id"
        const val CUSTOMER_EMAIL = "customer_email"
        const val FIRST_NAME = "recepient_first_name"
        const val LAST_NAME = "recepient_last_name"
        const val EMAIL = "recepient_email"
        const val INITIAL_PRICE = "initial_price"
        const val FINAL_PRICE = "final_price"
        const val KASSA_ORDER_STATUS = "kassa_order_status"
        const val USED_PROMO = "used_promo"
        const val OFFER = "offer"
        const val PARTICIPANT = "participant"
    }
}