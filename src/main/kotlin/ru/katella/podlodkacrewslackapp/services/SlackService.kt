package ru.katella.podlodkacrewslackapp.services

import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.reactions.ReactionsGetRequest
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.model.Reaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.repositories.User
import kotlin.math.absoluteValue

@Service
class SlackService {

    @Autowired
    lateinit var client: MethodsClient
    @Autowired
    lateinit var configService: ConfigService

    fun postUserReceivedPointsFrom(receivingUserId: String, donatingUserId: String, receivedPoints: Int, total: Int) {
        val channelName = configService.gameNotificationChannel

        val text = "${receivingUserId.userTag()} получает " +
                "$receivedPoints от " +
                "${donatingUserId.userTag()}. " +
                "Всего очков: $total"

        client.chatPostMessage {
            it.channel(channelName)
                .text(text)
        }
    }

    fun postUserReceivedPointsForReactions(receivingUserId: String, receivedPoints: Int, total: Int) {
        val channelName = configService.gameNotificationChannel

        val text = "${receivingUserId.userTag()} получает " +
                "$receivedPoints за десять :fire: " +
                "Всего очков: $total"

        client.chatPostMessage {
            it.channel(channelName)
                .text(text)
        }
    }

    fun postLeaderBoard(channel: String, leaderBoard: List<User>) {
        var table = "*Топ-10 участников конкурса:*"
        leaderBoard.forEachIndexed { index, user ->
            val row = ROW_SEPARATOR +
                    EMOJI_MAP.getOrDefault(index, DEFAULT_EMOJI) +
                    user.id.userTag() + " – " + user.points.pointsString()
            table += row
        }

        client.chatPostMessage {
            it.channel(channel)
                .mrkdwn(true)
                .text(table)
        }
    }

    fun postFavoriteHost(channel: String, hostId: String) {

        val texts = listOf(
            "Опросы релевантной аудитории показали, что лучше всех подкаст \"Подлодка\" ведет ${hostId.userTag()}",
            "Бесспорно, лучший ведущий подкаста – ${hostId.userTag()}",
            "Чей голос сладок, как мед, и ласкает уши слушателей? Конечно же ${hostId.userTag()}",
            "Моя мама говорит, что ${hostId.userTag()} – лучше всех",
            "Нет лучше музыки, чем голос ${hostId.userTag()}",
            "По статистике больше всего слушают выпуски, где присутствует ${hostId.userTag()}",
            "Лучший ведущий Подлодки в 2020 году – ${hostId.userTag()}"
        )
        val randomText = texts.random()
        client.chatPostMessage {
            it.channel(channel)
                .text(randomText)
        }
    }

    fun reactionInfo(channelId: String, messageTimestamp: String): Reaction? {
        val request = ReactionsGetRequest.builder()
            .channel(channelId)
            .timestamp(messageTimestamp)
            .build()
        val response = client.reactionsGet(request)
        return response.message.reactions.find { it.name == "thumbsup" }
    }

    fun slackUserInfo(userId: String): SlackUser {
        val request = UsersInfoRequest.builder()
            .user(userId)
            .build()
        val slackUser = client.usersInfo(request).user
        return SlackUser(slackUser.id, slackUser.realName, slackUser.isAdmin)
    }

    fun isUserAdmin(userId: String): Boolean {
        return try {
            val user = slackUserInfo(userId)
            user.isAdmin
        } catch (ex: Exception) {
            false
        }
    }

    private fun String.userTag(): String = "<@$this>"

    private fun Int.pointsString(): String {
        var result = "${this.toString()} "
        val abs = this.absoluteValue
        result += when (abs) {
            0, in 5..20 -> "очков"
            1 -> "очко"
            in 2..4 -> "очка"
            else -> {
                when (abs % 10) {
                    0, in 5..9 -> "очков"
                    1 -> "очко"
                    in 2..4 -> "очка"
                    else -> "очков"
                }
            }
        }
        return result
    }

    data class SlackUser(val userId: String, val userName: String, val isAdmin: Boolean)

    companion object {
        const val ROW_SEPARATOR = '\n'
        const val DEFAULT_EMOJI = ":point_right:"
        val EMOJI_MAP = mapOf<Int, String>(
            0 to ":zero:",
            1 to ":one:",
            2 to ":two:",
            3 to ":three:",
            4 to ":four:",
            5 to ":five:",
            6 to ":six:",
            7 to ":seven:",
            8 to ":eight:",
            9 to ":nine:"
        )
    }
}

