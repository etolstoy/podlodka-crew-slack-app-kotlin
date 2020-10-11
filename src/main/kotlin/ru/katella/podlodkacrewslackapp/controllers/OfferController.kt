package ru.katella.podlodkacrewslackapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.katella.podlodkacrewslackapp.services.PriceService
import ru.katella.podlodkacrewslackapp.services.OfferService
import ru.katella.podlodkacrewslackapp.services.OfferService.ProductType

@RestController
@RequestMapping("/offers")
class OfferController {

    @Autowired
    lateinit var offerService: OfferService

    @GetMapping
    fun getProductList(@RequestParam(name = "product_type") type: String?, @RequestParam(name = "product_id") productId: String?): List<Map<String, Any>>? {
        if (type != null) {
            return offerService.obtainOffers(enumValueOf<ProductType>(type.toUpperCase())).map {
                mapOffer(it)
            }
        }
        if (productId != null) {
            return offerService.obtainOffers(productId = productId).map {
                mapOffer(it)
            }
        }
        if (type == null && productId == null) {
            return offerService.obtainOffers().map {
                mapOffer(it)
            }
        }
        return null
    }

    private fun mapOffer(offer: PriceService.Offer): Map<String, Any> = mapOf(
        "offer_id" to offer.id,
        "product_name" to offer.productName.first(),
        "product_id" to offer.productId.first(),
        "price" to offer.price
    )
}
