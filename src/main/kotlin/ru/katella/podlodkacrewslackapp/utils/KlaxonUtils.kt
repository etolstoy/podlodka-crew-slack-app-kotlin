package ru.katella.podlodkacrewslackapp.utils

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.KlaxonException

@Target(AnnotationTarget.FIELD)
annotation class KlaxonAmount

@Target(AnnotationTarget.FIELD)
annotation class KlaxonConfirmation

val amountConverter = object: Converter {
    override fun canConvert(cls: Class<*>)
            = cls == Map::class.java

    override fun fromJson(jv: JsonValue) =
            if (jv.obj != null) {
                println(jv)
                jv.obj?.get("value")
            } else {
                throw KlaxonException("Couldn't parse amount: ${jv.string}")
            }

    override fun toJson(value: Any): String = ""
}

val confirmationConverter = object: Converter {
    override fun canConvert(cls: Class<*>)
            = cls == Map::class.java

    override fun fromJson(jv: JsonValue) =
            if (jv.obj != null) {
                println(jv)
                jv.obj?.get("confirmation_url")
            } else {
                throw KlaxonException("Couldn't parse confirmation_url: ${jv.string}")
            }

    override fun toJson(value: Any): String = ""
}