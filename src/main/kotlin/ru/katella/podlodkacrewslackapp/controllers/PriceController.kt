package ru.katella.podlodkacrewslackapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.katella.podlodkacrewslackapp.services.PriceService

@RestController
@RequestMapping("/offers_promo")
class PriceController {

    @Autowired
    lateinit var priceService: PriceService

    @GetMapping
    fun countPrice(@RequestParam(name = "offer_ids") offers: List<String>, promo: String?): Map<String, Number> {
        val price = priceService.getPrice(offers, promo).bulkPrice
        return mapOf(
            "price" to price
        )
    }

}
