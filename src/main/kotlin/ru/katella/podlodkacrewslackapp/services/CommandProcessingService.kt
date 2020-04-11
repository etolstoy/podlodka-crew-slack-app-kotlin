package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.repositories.User
import ru.katella.podlodkacrewslackapp.repositories.UserRepository

@Service
class CommandProcessingService {

    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var slackService: SlackService

    fun processCommand(fromUser: String, toUser: String, command: Command, channelId: String) {


        val donator = userRepository.findById(fromUser).orElseGet {
            userRepository.saveAndFlush(User(fromUser))
        }
        val recipient = userRepository.findById(toUser).orElseGet {
            userRepository.saveAndFlush(User(toUser))
        }

        val isCommandAllowed = slackService.isUserAdmin(fromUser)
        if (!isCommandAllowed) {
            return
        }

        var received = 0
        val total = when (command) {
            is Increment -> {
                received = 1
                recipient.likesReceived + 1
            }
            is Decrement -> {
                received = -1
                recipient.likesReceived - 1
            }
            is Increase -> {
                received = command.by
                recipient.likesReceived + command.by
            }
            is Decrease -> {
                received = -command.by
                recipient.likesReceived - command.by
            }
            is NoOp -> {
                null
            }
        }?.apply {
            userRepository.saveAndFlush(recipient.copy(likesReceived = this))
            slackService.postUserReceivedPointsFrom(fromUser, toUser, received, this)
        }
    }

    fun parseCommand(text: String): Command {
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