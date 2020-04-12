package ru.katella.podlodkacrewslackapp.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.katella.podlodkacrewslackapp.services.ProcessingService

@RequestMapping("/slack")
@RestController
class MainController {

    @Autowired
    private lateinit var processingService: ProcessingService

    @PostMapping("/leaderboard")
    fun greetings(@RequestParam(name = "channel_name") channel: String,
                  @RequestParam(name = "user_id") userId: String) {

        processingService.processLeaderboard(channel, userId)
    }
}