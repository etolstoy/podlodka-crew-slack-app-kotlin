package ru.katella.podlodkacrewslackapp.controllers

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import ru.katella.podlodkacrewslackapp.services.EmailService


@RestController
@RequestMapping("/tilda-shop")
class TildaShopController {

    @Autowired
    lateinit var emailService: EmailService

    @PostMapping
    @ResponseBody
    fun handlePlaylistBuyEvent(@RequestParam formParams: Map<String, Any>,
                               @RequestParam(name = "Name") name: String,
                               @RequestParam(name = "Email") email: String
    ): String {
        val keys: Set<String> = formParams.keys
        val links = mapOf<String, String>(
            "Видео Teamlead Crew, сезон 1" to "xx",
            "Видео Teamlead Crew, сезон 2" to "xx",
            "Видео iOS Crew, сезон 1" to "xx"
        )

        val regex = """payment\[products\]\[[0-9]\]\[name\]""".toRegex()
        val result = keys.filter { regex.matches(it) }.map {
            formParams[it].toString()
        }.map {
            it + ". Ссылка: " + links[it]
        }.joinToString { it }

        emailService.sendEmail(email, "Плейлисты Podlodka Crew", result)

        return result
    }
}

