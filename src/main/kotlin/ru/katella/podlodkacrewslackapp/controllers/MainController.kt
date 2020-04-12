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
    fun greetings(@RequestParam params: Map<String,String>) {

        //println("DEBUG CHANNEL: $channel")
        params.forEach { (k, v) ->
            println("DEBUG PARAM: $k -> $v")
        }
        processingService.processLeaderboard("testing", "")
    }
}