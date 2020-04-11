package ru.katella.podlodkacrewslackapp.services

import com.slack.api.methods.MethodsClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.User
import ru.katella.podlodkacrewslackapp.UserRepository

typealias UserId = String

@Service
class CommandProcessingService {

    @Autowired
    lateinit var client: MethodsClient

    @Autowired
    lateinit var userRepository: UserRepository

    fun processCommand(fromUser: UserId, toUser: UserId, command: Command, channelId: String) {
        println("user $fromUser send $command to $toUser")

        val text = "<@$fromUser> sent $command to <@$toUser>"

        when (command) {
            is Increment -> {
                userRepository.save(User(fromUser, 100, 0))
            }
        }

        client.chatPostMessage {
            it.channel(channelId)
                .text(text)
        }
    }

    fun parseCommand(text: String): Command {
        println(text)
        return when {
            text.contains("++") -> {
                Increment
            }
            text.contains("--") -> {
                Decrement
            }
            text.contains("+") -> {
                val delimiter = "+"
                val number = text.split(delimiter)[1].toIntOrNull()
                if (number != null) {
                    Increase(number)
                } else NoOp
            }
            text.contains("-") -> {
                val delimiter = "-"
                val number = text.split(delimiter)[1].toIntOrNull()
                if (number != null) {
                    Decrease(number)
                } else NoOp
            }
            else -> {
                NoOp
            }
        }
    }
}