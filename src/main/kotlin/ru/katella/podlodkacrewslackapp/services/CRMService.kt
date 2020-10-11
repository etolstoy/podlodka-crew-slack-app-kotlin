package ru.katella.podlodkacrewslackapp.services

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.repositories.Order
import ru.katella.podlodkacrewslackapp.utils.*

@Service
class CRMService {
    @Autowired
    lateinit var airTableService: AirTableService

    data class AirTableParticipantResponse(
        val records: List<ParticipantRecord>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ParticipantRecord(
        val id: String,
        @JsonAlias(AirTableCommon.FIELDS)
        val participant: Participant
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Participant(
        @JsonAlias(AirTableParticipant.EMAIL)
        val email: String
    )

    fun createOrder(order: Order, status: String): Boolean {
        val mapper = ObjectMapper().registerKotlinModule()
        // Достаем или создаем Participant
        val participantPayload = mapOf(
            AirTableCommon.FILTER_BY to "{${AirTableParticipant.EMAIL}} = '${order.email}'"
        )
        val participantJsonString = airTableService.makeGetRequest(AirTableEndpoint.PARTICIPANT, participantPayload)
        val participantRecords = mapper.readValue<AirTableParticipantResponse>(participantJsonString).records
        var participantObjectId = ""
        if (participantRecords.count() == 0) {
            // Не нашли такого пользователя, создаем его с нуля
            createParticipant(order.email, order.firstName, order.lastName)
        } else {
            participantObjectId = participantRecords.first().id
        }

        // Достаем Offer
        val offerPayload = mapOf(
            AirTableCommon.FILTER_BY to "{${AirTableOffer.ID}} = '${order.offerId}'"
        )
        val offerJsonString = airTableService.makeGetRequest(AirTableEndpoint.OFFER, offerPayload)
        val offerObjectId = mapper.readValue<PriceService.AirTableOfferResponse>(offerJsonString).records.first().id

        // Достаем Promo
        val promoPayload = mapOf(
            AirTableCommon.FILTER_BY to "{${AirTablePromo.ID}} = '${order.usedPromo}'"
        )
        val promoJsonString = airTableService.makeGetRequest(AirTableEndpoint.PROMO, promoPayload)
        val promoObjectId = mapper.readValue<PriceService.AirTablePromoResponse>(promoJsonString).records.first().id

        // Создаем всю сущность
        val orderPayload = mapOf(
            AirTableCommon.RECORDS to listOf<Map<String, Any>>(
                mapOf(
                    AirTableCommon.FIELDS to mapOf<String, Any>(
                        AirTableOrder.KASSA_ID to order.confirmationId,
                        AirTableOrder.CUSTOMER_EMAIL to order.customerEmail,
                        AirTableOrder.FIRST_NAME to order.firstName,
                        AirTableOrder.LAST_NAME to order.lastName,
                        AirTableOrder.EMAIL to order.email,
                        AirTableOrder.INITIAL_PRICE to order.initialPrice,
                        AirTableOrder.FINAL_PRICE to order.finalPrice,
                        AirTableOrder.KASSA_ORDER_STATUS to status,
                        AirTableOrder.USED_PROMO to listOf(
                            promoObjectId
                        ),
                        AirTableOrder.OFFER to listOf(
                            offerObjectId
                        ),
                        AirTableOrder.PARTICIPANT to listOf(
                            participantObjectId
                        )
                    )
                )
            )
        )
        println(orderPayload)
        val r = airTableService.makePostRequest(AirTableEndpoint.ORDER, orderPayload)

        return r.statusCode == 200
    }

    fun createParticipant(email: String, firstName: String?, lastName: String?) {
        val map = mutableMapOf(
            AirTableParticipant.EMAIL to email
        )
        if (firstName != null) {
            map.put(AirTableParticipant.NAME, firstName)
        }
        if (lastName != null) {
            map.put(AirTableParticipant.SURNAME, lastName)
        }

        val participantPayload = mapOf(
            AirTableCommon.RECORDS to listOf<Map<String, Any>>(
                mapOf(
                    AirTableCommon.FIELDS to map
                )
            )
        )
        airTableService.makePostRequest(AirTableEndpoint.PARTICIPANT, participantPayload)
    }
}