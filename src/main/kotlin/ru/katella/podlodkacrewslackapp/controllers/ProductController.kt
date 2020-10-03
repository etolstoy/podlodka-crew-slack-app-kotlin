package ru.katella.podlodkacrewslackapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.katella.podlodkacrewslackapp.data.repositories.Playlist
import ru.katella.podlodkacrewslackapp.services.ProductService
import ru.katella.podlodkacrewslackapp.services.ProductService.ProductType
import ru.katella.podlodkacrewslackapp.services.ProductService.Product

@RestController
@RequestMapping("/products")
class ProductController {

    @Autowired
    lateinit var productService: ProductService

    @GetMapping
    fun getProductList(type: String): List<Product> {
        return productService.obtainProducts(enumValueOf<ProductType>(type.toUpperCase()))
    }
}
