package ru.katella.podlodkacrewslackapp.controllers

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.ui.ModelMap
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import ru.katella.podlodkacrewslackapp.data.repositories.Playlist
import ru.katella.podlodkacrewslackapp.data.repositories.PlaylistRepository
import ru.katella.podlodkacrewslackapp.services.EmailService


@RestController
@RequestMapping("/tilda-shop")
class TildaShopController {

    @Autowired
    lateinit var emailService: EmailService

    @Autowired
    lateinit var playlistRepository: PlaylistRepository

    @PostMapping
    @ResponseBody
    fun handlePlaylistBuyEvent(@RequestParam formParams: Map<String, Any>,
                               @RequestParam(name = "Name") name: String,
                               @RequestParam(name = "Email") email: String
    ): String {
        val keys: Set<String> = formParams.keys
        val regex = """payment\[products\]\[[0-9]\]\[name\]""".toRegex()
        val result = keys.filter { regex.matches(it) }.map {
            formParams[it].toString()
        }.map {
            val playlist = getPlaylistByName(it)
            it + ". Ссылка: " + playlist.url + "\n"
        }.joinToString("") { it }

        val messageText = "Привет, Спасибо за заказ!\n\n" + result

        emailService.sendEmail(email, "Ссылки на плейлисты Podlodka Crew", messageText)

        return result
    }

    fun getPlaylistByName(name: String): Playlist {
        playlistRepository.findAll().forEach {
            if (it.name == name) {
                return it
            }
        }
        return Playlist("", "")
    }
}
