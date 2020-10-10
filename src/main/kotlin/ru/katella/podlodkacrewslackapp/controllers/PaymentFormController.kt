package ru.katella.podlodkacrewslackapp.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.katella.podlodkacrewslackapp.services.OrderCacheService
import ru.katella.podlodkacrewslackapp.services.PriceService
import ru.katella.podlodkacrewslackapp.services.ProductService
import ru.katella.podlodkacrewslackapp.services.YandexKassaService

@RestController
@RequestMapping("/buy")
class PaymentFormController {
    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var priceService: PriceService

    @Autowired
    lateinit var kassaService: YandexKassaService

    @Autowired
    lateinit var orderCacheService: OrderCacheService

    @PostMapping
    @ResponseBody
    fun createOrder(@RequestBody payload: Map<Any, Any?>): Map<String, String?> {
        // Валидация – переданные offerId и promoId должны существовать
        val mapper = ObjectMapper().registerKotlinModule()
        val orderBag = mapper.convertValue<OrderCacheService.OrderBag>(payload)
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
        orderCacheService.cacheOrder(orderBag, confirmation.id)

        // Возвращаем фронту ссылку на форму оплаты, а статус заказа сохраняем в кэше
        return mapOf(
                "confirmation_url" to confirmation.confirmationUrl
        )
    }
}
