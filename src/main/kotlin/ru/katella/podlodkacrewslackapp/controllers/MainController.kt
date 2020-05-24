package ru.katella.podlodkacrewslackapp.controllers

import com.slack.api.methods.MethodsClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import ru.katella.podlodkacrewslackapp.services.ProcessingService

@RequestMapping("/slack")
@RestController
class MainController {

    @Autowired
    private lateinit var processingService: ProcessingService

    @Autowired
    private lateinit var methodClient: MethodsClient

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

    @PostMapping("/start_game")
    fun startGame(@RequestParam(name = "team_id") teamId: String,
                  @RequestParam(name = "channel_id") channelId: String,
                  @RequestParam(name = "user_id") userId: String) {
        processingService.processStartStopGame(teamId, channelId, userId, true)
    }

    @PostMapping("/stop_game")
    fun stopGame(@RequestParam(name = "team_id") teamId: String,
                  @RequestParam(name = "channel_id") channelId: String,
                  @RequestParam(name = "user_id") userId: String) {
        processingService.processStartStopGame(teamId, channelId, userId, false)
    }

    @GetMapping("/install")
    fun install(@RequestParam(name = "code") code: String): String {

        val response = methodClient.oauthAccess {
            it.code(code)
        }
        return response.accessToken
    }
}