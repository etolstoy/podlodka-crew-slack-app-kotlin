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

    fun processPoints(callingUser: String, mentionedUser: String, operation: PointsOperation, channelId: String) {

        val donator = getOrCreateUser(callingUser)
        val recipient = getOrCreateUser(mentionedUser)

        val isCommandAllowed = slackService.isUserAdmin(callingUser)
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
        }.apply {
            userRepository.saveAndFlush(recipient.copy(points = this))
            slackService.postUserReceivedPointsFrom(mentionedUser, callingUser, received, this)
        }
    }

    fun processLottery(callingUser: String,
                       mentionedUser: String,
                       op: Lottery,
                       channelId: String,
                       currentThread: String) {
        val caller = getOrCreateUser(callingUser)
        if (!caller.isAdmin) return

        val botUser = slackService.slackUserInfo(mentionedUser)
        if (!botUser.isBot && botUser.userName != BOT_NAME) return

        val messageInfo = slackService.messageInfo(channelId, currentThread)
        val users = messageInfo.reactions
            .flatMap { it.users }
            .distinct()
            .filter { /*TODO раскомментить!!! !HOST_IDS.contains(it) && */it != botUser.userId }
            .shuffled()
        if (op.participants > users.size) return
        val participants = users.take(op.participants)

        val updatedUsers = participants.map {
            val user = getOrCreateUser(it)
            userRepository.saveAndFlush(user.copy(points = user.points + op.pointsPerParticipant))
        }
        slackService.postLotteryWinnersToThread(participants, op.pointsPerParticipant, channelId, currentThread)
        updatedUsers.forEach {
            slackService.postUserReceivedPointsForLottery(it.id, op.pointsPerParticipant, it.points)
        }
    }

    fun processLeaderboard(channelId: String, userId: String) {
        getOrCreateUser(userId)
        val searchResult = userRepository.findAll(Sort.by(Sort.Direction.DESC, "points")).filter { !it.isAdmin }
        if (searchResult.isEmpty()) {
            return
        }
        slackService.postLeaderBoard(userId, channelId, searchResult)
    }

    fun processBestHost(channelId: String) {
        val favoriteHostId = HOST_IDS.random()
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



    fun getOrCreateUser(userId: String): User {
        return userRepository.findById(userId).orElseGet {
            val slackUser = slackService.slackUserInfo(userId)
            userRepository.saveAndFlush(User(userId, slackUser.userName, slackUser.isAdmin))
        }
    }

    companion object {
        private const val REACTIONS_FOR_PRIZE = 10
        private const val POINTS_FOR_REACTIONS = 10
        //TODO remove hardcoded bot name
        private const val BOT_NAME = "Skipper Bot"
        private val TRIGGERING_REACTIONS = listOf("thumbsup", "+1")
        //TODO remove hardcoded IDs
        private val HOST_IDS = listOf("U011BT88CDR", "U011A80PMQT", "U011EESQ0G6", "U011F6VEEUV")
    }
}