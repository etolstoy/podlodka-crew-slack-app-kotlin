package ru.katella.podlodkacrewslackapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.katella.podlodkacrewslackapp.services.PriceService

@RestController
@RequestMapping("/price")
class PriceController {

    @Autowired
    lateinit var priceService: PriceService

    @GetMapping
    fun countPrice(@RequestParam offers: List<String>, promo: String?): Number {
        return priceService.getPrice(offers, promo).price
    }

}
