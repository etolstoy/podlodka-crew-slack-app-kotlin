package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.repositories.Message
import ru.katella.podlodkacrewslackapp.data.repositories.ReactionsRepository
import ru.katella.podlodkacrewslackapp.data.repositories.User
import ru.katella.podlodkacrewslackapp.data.repositories.UserRepository
import ru.katella.podlodkacrewslackapp.data.slack.SessionStatsBuilder
import ru.katella.podlodkacrewslackapp.data.slack.isUseful

@Service
class ProcessingService {

    @Autowired
    lateinit var userRepository: UserRepository
    @Autowired
    lateinit var reactionsRepository: ReactionsRepository
    @Autowired
    lateinit var slackService: SlackService
    @Autowired
    lateinit var configService: ConfigService

    fun processPoints(callingUser: String, mentionedUser: String, operation: PointsOperation, teamId: String, channelId: String) {

        val donator = getOrCreateUser(teamId, callingUser)
        val recipient = getOrCreateUser(teamId, mentionedUser)

        val isCommandAllowed = slackService.isUserAdmin(teamId, callingUser)
        if (!isCommandAllowed) {
            return
        }

        if (!checkGameIsStarted(teamId, channelId, callingUser)) return

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
            slackService.postUserReceivedPointsFrom(teamId, mentionedUser, callingUser, received, this)
        }
    }

    fun processLottery(callingUser: String,
                       mentionedUser: String,
                       op: Lottery,
                       teamId: String,
                       channelId: String,
                       currentThread: String) {
        val caller = getOrCreateUser(teamId, callingUser)
        if (!caller.isAdmin) return
        if (!checkGameIsStarted(teamId, channelId, callingUser)) return

        val botUser = slackService.slackUserInfo(teamId, mentionedUser)
        if (!botUser.isBot && botUser.userName != BOT_NAME) return

        val admins = slackService.slackAdminsList(teamId).map { it.userId }

        val messageInfo = slackService.messageInfo(teamId, channelId, currentThread)
        val users = messageInfo.reactions
            .flatMap { it.users }
            .distinct()
            .filter { !admins.contains(it) && it != botUser.userId}
            .shuffled()

        if (op.participants > users.size) return
        val participants = users.take(op.participants)

        val updatedUsers = participants.map {
            val user = getOrCreateUser(teamId, it)
            userRepository.saveAndFlush(user.copy(points = user.points + op.pointsPerParticipant))
        }
        slackService.postLotteryWinnersToThread(teamId, participants, op.pointsPerParticipant, channelId, currentThread)
        updatedUsers.forEach {
            slackService.postUserReceivedPointsForLottery(teamId, it.id, op.pointsPerParticipant, it.points)
        }
    }

    fun processLeaderBoard(teamId: String, channelId: String, userId: String) {
        getOrCreateUser(teamId, userId)
        val searchResult = userRepository.findByTeamId(teamId).filter { !it.isAdmin }.sortedByDescending { it.points }
        if (searchResult.isEmpty()) {
            return
        }
        slackService.postLeaderBoard(teamId, userId, channelId, searchResult)
    }

    fun processBestHost(teamId: String, channelId: String) {
        val favoriteHostId = HOST_IDS[teamId]?.random() ?: return
        slackService.postFavoriteHost(teamId, channelId, favoriteHostId)
    }

    fun processNewReaction(teamId: String,
                           channelId: String,
                           reaction: String,
                           receivingUser: String,
                           reactingUser: String,
                           messageTimestamp: String) {
        if (!checkGameIsStarted(teamId, channelId)) return
        if (reaction !in TRIGGERING_REACTIONS) return

        val message = slackService.messageInfo(teamId, channelId, messageTimestamp)
        if (!message.isUseful(TRIGGERING_REACTIONS)) return

        val messageReactions = message.reactions.filter { it.name in TRIGGERING_REACTIONS }
        if (messageReactions.all { it.count < REACTIONS_FOR_PRIZE }) return

        val dbMessage = reactionsRepository.findByTimestampAndTeamIdAndChannel(messageTimestamp, teamId, channelId)
        if (dbMessage.isNotEmpty()) return

        val user = getOrCreateUser(teamId, receivingUser)
        val newPoints = user.points + POINTS_FOR_REACTIONS
        userRepository.saveAndFlush(user.copy(points = newPoints))
        reactionsRepository.saveAndFlush(Message(messageTimestamp, teamId, channelId))
        slackService.postUserReceivedPointsForReactions(teamId, message.permalink, receivingUser, POINTS_FOR_REACTIONS, newPoints)
        }

    fun processReset(teamId: String, channelId: String, userId: String) {
        val user = getOrCreateUser(teamId, userId)
        if (user.isAdmin) {
            reactionsRepository.deleteByTeamId(teamId)
            userRepository.deleteAll()
        } else {
            slackService.postEphemeralMessage(teamId,
                channelId,
                userId,
                "Эх, вот бы сейчас все *обнулить* да начать сначала? Но нет, эта опция только для админов ;)")
        }
    }

    fun processSessionsStats(teamId: String, channelId: String, userId: String) {
        val messages = slackService.sessionsRateMessages(teamId, channelId)
        val file = SessionStatsBuilder().build(messages)
        slackService.uploadFile(teamId, channelId, file.content, file.name)
    }

    fun processStartStopGame(teamId: String, channelId: String, userId: String, gameStarted: Boolean) {
        val user = getOrCreateUser(teamId, userId)
        if (!user.isAdmin) {
            slackService.postEphemeralMessage(teamId,
                channelId,
                userId,
                "Нельзя так просто взять и порулить игрой, когда ты не админ!")
        } else {
            configService.setGameActive(teamId, gameStarted)

            val gameStatus = if (gameStarted) "начата" else "остановлена"
            slackService.postEphemeralMessage(teamId,
                channelId,
                userId,
                "Игра $gameStatus!")
        }
    }

    private fun checkGameIsStarted(teamId: String, channelId: String, notifiedUser: String? = null): Boolean {
        return if (!configService.isGameActive(teamId)) {
            if (notifiedUser != null) {
                slackService.postEphemeralMessage(teamId, channelId, notifiedUser, "Игра еще не начата!")
            }
            false
        } else {
            true
        }
    }

    private fun getOrCreateUser(teamId: String, userId: String): User {
        return userRepository.findById(userId).orElseGet {
            val slackUser = slackService.slackUserInfo(teamId, userId)
            userRepository.saveAndFlush(User(userId, slackUser.userName, slackUser.teamId, slackUser.isAdmin))
        }
    }

    companion object {
        private const val REACTIONS_FOR_PRIZE = 10
        private const val POINTS_FOR_REACTIONS = 10
        //TODO remove hardcoded bot name
        private val BOT_NAME = System.getenv("BOT_NAME")
        internal val TRIGGERING_REACTIONS = listOf("thumbsup", "+1")
        //TODO remove hardcoded IDs
        private val HOST_IDS by lazy { getHosts() }

        private fun getHosts(): Map<String, List<String>> {
            val hostsString = System.getenv("PODLODKA_HOSTS")
            val idsMap = mutableMapOf<String, List<String>>()
            hostsString.split(",")
                .map { it.split("_") }
                .map { it[0] to it[1].split(":") }
                .forEach { (team, ids) ->
                    idsMap[team] = ids
                }
            return idsMap
        }
    }
}