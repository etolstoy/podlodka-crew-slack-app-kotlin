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
    fun leaderBoard(@RequestParam(name = "team_id") teamId: String,
                  @RequestParam(name = "channel_id") channelId: String,
                  @RequestParam(name = "user_id") userId: String) {
        processingService.processLeaderBoard(teamId, channelId, userId)
    }

    @PostMapping("/best_host")
    fun bestHost(@RequestParam(name = "team_id") teamId: String,
                 @RequestParam(name = "channel_id") channelId: String) {
        processingService.processBestHost(teamId, channelId)
    }

    @PostMapping("/reset_score")
    fun resetScore(@RequestParam(name = "team_id") teamId: String,
                   @RequestParam(name = "channel_id") channelId: String,
                   @RequestParam(name = "user_id") userId: String) {
        processingService.processReset(teamId, channelId, userId)
    }
}