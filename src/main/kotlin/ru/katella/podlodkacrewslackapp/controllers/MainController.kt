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
    fun greetings(@RequestParam(name = "channel_id") channelId: String,
                  @RequestParam(name = "user_id") userId: String) {
        processingService.processLeaderboard(channelId, userId)
    }

    @PostMapping("/best_host")
    fun bestHost(@RequestParam(name = "channel_id") channelId: String) {
        processingService.processBestHost(channelId)
    }

    @PostMapping("/reset_score")
    fun resetScore(@RequestParam(name = "channel_id") channelId: String,
                   @RequestParam(name = "user_id") userId: String) {
        processingService.processReset(channelId, userId)
    }
}