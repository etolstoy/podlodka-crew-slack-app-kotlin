package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.repositories.Message
import ru.katella.podlodkacrewslackapp.repositories.ReactionsRepository
import ru.katella.podlodkacrewslackapp.repositories.User
import ru.katella.podlodkacrewslackapp.repositories.UserRepository

@Service
class ProcessingService {

    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var reactionsRepository: ReactionsRepository
    @Autowired
    lateinit var slackService: SlackService

    fun processPoints(donatingUser: String, receivingUser: String, operation: Operation, channelId: String) {

        val donator = getOrCreateUser(donatingUser)
        val recipient = getOrCreateUser(receivingUser)

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
        getOrCreateUser(userId)
        val searchResult = userRepository.findAll(Sort.by(Sort.Direction.DESC, "points"))
        if (searchResult.isEmpty()) {
            return
        }
        slackService.postLeaderBoard(userId, channelId, searchResult)
    }

    fun processBestHost(channelId: String) {
        val hostIds = listOf("U011BT88CDR", "U011A80PMQT", "U011EESQ0G6", "U011F6VEEUV")
        val favoriteHostId = hostIds.random()
        slackService.postFavoriteHost(channelId, favoriteHostId)
    }

    fun processNewReaction(channelId: String, reaction: String, receivingUser: String, reactingUser: String, messageTimestamp: String) {
        if (reaction in TRIGGERING_REACTIONS) {
            val message = slackService.messageInfo(channelId, messageTimestamp)

            val messageReactions = message.reactions.filter { it.name in TRIGGERING_REACTIONS }
            if (messageReactions.all { it.count < REACTIONS_FOR_PRIZE }) return

            val dbMessage = reactionsRepository.findByTimestampAndChannel(messageTimestamp, channelId)
            if (dbMessage.isNotEmpty()) return

            val user = getOrCreateUser(receivingUser)
            val newPoints = user.points + POINTS_FOR_REACTIONS
            userRepository.saveAndFlush(user.copy(points = newPoints))
            reactionsRepository.saveAndFlush(Message(messageTimestamp, channelId))
            slackService.postUserReceivedPointsForReactions(message.permalink, receivingUser, POINTS_FOR_REACTIONS, newPoints)
        }
    }

    fun processReset(channelId: String, userId: String) {
        val user = slackService.slackUserInfo(userId)
        if (user.isAdmin) {
            reactionsRepository.deleteAll()
            userRepository.deleteAll()
        } else {
            slackService.postEphemeralMessage(channelId,
                userId,
                "Эх, вот бы сейчас все *обнулить* да начать сначала? Но нет, эта опция только для админов ;)")
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

    fun getOrCreateUser(userId: String): User {
        return userRepository.findById(userId).orElseGet {
            val slackUser = slackService.slackUserInfo(userId)
            userRepository.saveAndFlush(User(userId, slackUser.userName, slackUser.isAdmin))
        }
    }

    companion object {
        private const val REACTIONS_FOR_PRIZE = 10
        private const val POINTS_FOR_REACTIONS = 10
        private val TRIGGERING_REACTIONS = listOf("thumbsup", "+1")
    }
}