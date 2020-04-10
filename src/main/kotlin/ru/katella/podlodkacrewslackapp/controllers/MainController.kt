package ru.katella.podlodkacrewslackapp.controllers

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/")
@RestController
class MainController {

    @RequestMapping("greetings")
    fun greetings(): String = "hello"
}