package ru.katella.podlodkacrewslackapp.controllers

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import khttp.post
import khttp.responses.Response
import ru.katella.podlodkacrewslackapp.data.ShopConfig
import ru.katella.podlodkacrewslackapp.data.repositories.Order
import ru.katella.podlodkacrewslackapp.data.repositories.OrderRepository
import ru.katella.podlodkacrewslackapp.services.PriceService
import ru.katella.podlodkacrewslackapp.services.ProductService
import ru.katella.podlodkacrewslackapp.services.YandexKassaService
import ru.katella.podlodkacrewslackapp.utils.*
import java.util.Base64

@RestController
@RequestMapping("/buy")
class PaymentFormController {
    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var priceService: PriceService

    @Autowired
    lateinit var kassaService: YandexKassaService

    data class Confirmation(
            val id: String,
            @KlaxonAmount
            val amount: String,
            @KlaxonConfirmation
            @Json(name = "confirmation")
            val confirmationUrl: String
    )

    data class OrderBag(
            @JsonAlias("customer_email")
            val customerEmail: String,
            val promo: String,
            val orders: Array<SingleOrder>?
    )

    data class SingleOrder(
            @JsonAlias("first_name")
            var firstName: String,
            @JsonAlias("last_name")
            var lastName: String,
            var email: String,
            @JsonAlias("offer_id")
            var offerId: String
    )

    @PostMapping
    @ResponseBody
    fun createOrder(@RequestBody payload: Map<Any, Any?>): Map<String, String?> {
        // Валидация – переданные offerId и promoId должны существовать
        val mapper = ObjectMapper().registerKotlinModule()
        val orderBag = mapper.convertValue<OrderBag>(payload)
        val offerIds = orderBag.orders?.map { it.offerId }

        // Проверяем, что все офферы существуют
        if (offerIds == null || offerIds.count() == 0 || productService.validateOffers(offerIds) == false) {
            return mapOf(
                    "Error" to "You passed invalid offerIds"
            )
        }

        // Проверяем, что промо существует и активен
        if (priceService.validatePromo(orderBag.promo) == false) {
            return mapOf(
                    "Error" to "You passed invalid promo"
            )
        }

        // Считаем стоимость покупки с учетом списка офферов и промокода
        val orderPrice = priceService.getPrice(offerIds, orderBag.promo)
        if (orderPrice.price == 0) {
            // TODO: Тут надо сразу переходить к оформлению успешного заказа
        }

        // Совершаем запрос в API Кассы, если валидация успешна
        val confirmation = kassaService.requestPaymentConfirmation(orderPrice.price)
        if (confirmation == null) {
            return mapOf(
                    "Error" to "Yandex Kassa didn't approve payment"
            )
        }

        // Обновляем usageCount промокода
        priceService.updatePromoUsage(orderBag.promo, orderPrice.promoUsageLeft)

        // Сохраняем статус заказа в локальный кеш


        // Возвращаем фронту ссылку на форму оплаты, а статус заказа сохраняем в кэше


        return mapOf()
//        val r = makeRequestToKassa()

//        productService.obtainProducts(type = ProductService.ProductType.PLAYLIST)
//        // Для успешного запроса – отдаем во фронт формочку оплаты и сохраняем статус заказа в кеш
//        if (r.statusCode == 200) {
//            val jsonString = r.jsonObject.toString()
//            val confirmation = Klaxon()
//                    .fieldConverter(KlaxonAmount::class, amountConverter)
//                    .fieldConverter(KlaxonConfirmation::class, confirmationConverter)
//                    .parse<Confirmation>(jsonString) ?: null
//
//            // Сохраняем пейлоад в наш кеш
//            if (confirmation?.id != null) {
//                cacheOrder(confirmation.id, payload)
//
//                // Отправляем на фронт урл странички оплаты
//                val result = mapOf(
//                        "confirmation_url" to confirmation?.confirmationUrl
//                )
//                return result
//            }
//        }
//
//        return mapOf(
//                "Error" to "Some error has happened with response ${r.jsonObject}"
//        )
    }

    private fun cacheOrder(orderId: String, payload: Map<Any, Any?>) {
        // Разбираем тело реквеста в модельки
        val json = Gson().toJson(payload)
        val orderBag = Klaxon()
                .parse<OrderBag>(json) ?: null

        if (orderBag?.orders != null) {
            for (order in orderBag.orders) {
                orderRepository.saveAndFlush(Order(
                        orderId,
                        orderBag.customerEmail,
                        order.firstName,
                        order.lastName,
                        order.email,
                        order.offerId,
                        "2000"
                ))
            }
        }
    }
}
