package ru.katella.podlodkacrewslackapp.services

import com.slack.api.bolt.App
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.model.event.MessageEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.slack.SlackApp

@Service
class MessageService {

    @Autowired
    lateinit var client: MethodsClient

    fun processMessage(message: MessageEvent) {

        val sender = message.user
        val text = message.text
        val clientMsgId = message.clientMsgId
        val channel = message.channel
        val blocks = message.blocks

        println(sender)
        println(text)
        println(clientMsgId)
        println(channel)
        blocks.forEach { println(it) }

        val userRequest = UsersInfoRequest.builder().user(sender).build()
        val response = client.usersInfo(userRequest)

        println("received user with name ${response.user.name}")
    }
}