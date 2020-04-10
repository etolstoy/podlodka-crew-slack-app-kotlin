package ru.katella.podlodkacrewslackapp.services

import com.slack.api.methods.MethodsClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

typealias UserId = String

@Service
class LikeService {

    @Autowired
    lateinit var client: MethodsClient

    fun processLike(fromUser: UserId, toUser: UserId, likeString: String, channelId: String) {
        println("user $fromUser send $likeString to $toUser")
        client.chatPostMessage {
            it.text("User ")
                .channel()
                .username(fromUser)
                .text(" sent $likeString to ")
                .username(toUser)
        }
    }
}