package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.repositories.User
import ru.katella.podlodkacrewslackapp.repositories.UserRepository

@Service
class ProcessingService {

    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var slackService: SlackService

    fun processPoints(donatingUser: String, receivingUser: String, operation: Operation, channelId: String) {

        val donator = userRepository.findById(donatingUser).orElseGet {
            val slackUser = slackService.slackUserInfo(donatingUser)
            userRepository.saveAndFlush(User(donatingUser, slackUser.userName, slackUser.isAdmin))
        }
        val recipient = userRepository.findById(receivingUser).orElseGet {
            val slackUser = slackService.slackUserInfo(receivingUser)
            userRepository.saveAndFlush(User(receivingUser,slackUser.userName, slackUser.isAdmin))
        }

        val isCommandAllowed = slackService.isUserAdmin(donatingUser)
        if (!isCommandAllowed) {
            return
        }

        var received = 0
        val total = when (operation) {
            is Increment -> {
                received = 1
                recipient.points + 1
            }
            is Decrement -> {
                received = -1
                recipient.points - 1
            }
            is Increase -> {
                received = operation.by
                recipient.points + operation.by
            }
            is Decrease -> {
                received = -operation.by
                recipient.points - operation.by
            }
            is NoOp -> {
                null
            }
        }?.apply {
            userRepository.saveAndFlush(recipient.copy(points = this))
            slackService.postUserReceivedPointsFrom(receivingUser, donatingUser, received, this)
        }
    }

    fun processLeaderboard(channelId: String, userId: String) {
        val searchResult = userRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "points")))
        if (searchResult.isEmpty) {
            return
        }
        slackService.postLeaderBoard(channelId, searchResult.content)
    }

    fun processNewReaction(channelId: String, reaction: String, receivingUser: String, reactingUser: String, messageTimestamp: String) {
        if (reaction == "fire") {
            slackService.messageInfo(channelId, messageTimestamp)
        }
    }

    fun parseCommand(text: String): Operation {
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