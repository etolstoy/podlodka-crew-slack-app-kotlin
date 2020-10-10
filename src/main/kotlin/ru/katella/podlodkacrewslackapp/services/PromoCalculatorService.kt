package ru.katella.podlodkacrewslackapp.services

import ru.katella.podlodkacrewslackapp.services.PriceService.Offer
import ru.katella.podlodkacrewslackapp.services.PriceService.Promo

class PromoCalculatorService {
    fun countPrice(offers: List<Offer>, promo: Promo?): Number {
        var allOffersPrice = offers.fold(0.0) { sum, offer ->
            sum + offer.price.toDouble()
        }

        // Проверяем промокод. Ищем его в таблице, затем проверяем на is_active.
        // Затем идем по всем событиям и проверяем, что у оффера есть промокод с таким айди
        if (promo != null) {
            if (promo.isActive == false) {
                println("Промокод не активен")
                return allOffersPrice
            }

            var priceWithPromo = 0.0
            var availablePromoUses = promo.usageLeft
            offers.forEach {
                // Этот промокод подходит к этому офферу, значит надо чекнуть его тип и поменять цену
                if (it.activePromoIds?.contains(promo.id) == true && (availablePromoUses > 0 || promo.type == PriceService.PromoType.UNLIMITED.typeName)) {
                    // Если это промокод с фикспрайсом, то просто плюсуем его стоимость к сумме
                    if (promo.priceType == PriceService.PromoPriceType.FIXED.typeName) {
                        priceWithPromo += promo.price.toDouble()
                    }

                    // Если тип – Decrease, то уменьшаем стоимость предмета на эту сумму, но чекаем на то, что не меньше нуля
                    if (promo.priceType == PriceService.PromoPriceType.DECREASE.typeName) {
                        var resultPrice = it.price.toDouble() - promo.price.toDouble()
                        println(resultPrice)
                        if (resultPrice < 0) {
                            resultPrice = 0.0
                        }
                        priceWithPromo += resultPrice
                    }

                    // Уменьшаем количество использований промокода
                    if (promo.type != PriceService.PromoType.UNLIMITED.typeName) {
                        availablePromoUses -= 1
                    }
                } else {
                    priceWithPromo += it.price.toDouble()
                }
            }

            allOffersPrice = priceWithPromo
        }

        return allOffersPrice
    }
}