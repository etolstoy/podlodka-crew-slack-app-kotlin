package ru.katella.podlodkacrewslackapp.services

import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest
import com.slack.api.methods.request.reactions.ReactionsGetRequest
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.methods.response.reactions.ReactionsGetResponse
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
                "${receivedPoints.pointsString()} от " +
                "${donatingUserId.userTag()}. " +
                "Всего очков: $total"

        client.chatPostMessage {
            it.channel(channelName)
                .text(text)
        }
    }

    fun postUserReceivedPointsForLottery(receivingUserId: String, receivedPoints: Int, total: Int) {
        val channelName = configService.gameNotificationChannel

        val text = "${receivingUserId.userTag()} получает " +
                "${receivedPoints.pointsString()} за оценку сессии и капельку везения! " +
                "Всего очков: $total"

        client.chatPostMessage {
            it.channel(channelName)
                .text(text)
        }
    }

    fun postUserReceivedPointsForReactions(originalMessage: String, receivingUserId: String, receivedPoints: Int, total: Int) {
        val channelName = configService.gameNotificationChannel

        val text = "${receivingUserId.userTag()} получает " +
                "${receivedPoints.pointsString()} за десять или более :thumbsup: <$originalMessage|здесь> \nВсего очков: *$total*"

        client.chatPostMessage {
            it.channel(channelName)
                .unfurlLinks(true)
                .text(text)
        }
    }

    fun postLotteryWinnersToThread(winners: List<String>, points: Int, channelId: String, threadId: String) {
        val text = "В розыгрыше победили: ${winners.joinToString(", ", transform = { it.userTag() })}! " +
                "Поздравляем! Вы получаете по ${points.pointsString()}"

        client.chatPostMessage {
            it.channel(channelId)
                .threadTs(threadId)
                .text(text)
        }
    }

    fun postEphemeralMessage(channelId: String, userId: String, text: String) {
        client.chatPostEphemeral {
            it.channel(channelId)
                .user(userId)
                .text(text)
        }
    }

    fun postLeaderBoard(userId: String, channel: String, leaderBoard: List<User>) {
        var table = "*Топ-15 участников конкурса:*"
        var userPresentedInTop = false
        leaderBoard.take(15).forEachIndexed { index, user ->
            if (user.id == userId) userPresentedInTop = true
            val row = ROW_SEPARATOR +
                    EMOJI_MAP.getOrDefault(index + 1, DEFAULT_EMOJI) +
                    user.id.userTag() + " – " + user.points.pointsString()
            table += row
        }

        client.chatPostMessage {
            it.channel(channel)
                .mrkdwn(true)
                .text(table)
        }
        if (!userPresentedInTop) {
            val index = leaderBoard.indexOfFirst { it.id == userId }
            val builder = ChatPostEphemeralRequest.builder()
                .channel(channel)
                .user(userId)

            if (index == -1) {
                builder.text("Вас пока нет в базе, но не переживайте, это скорее всего какая-то ошибка. " +
                        "Мы все проверим, а вы пока можете задать какой-нибудь вопрос нашим спикерам :)")
                client.chatPostEphemeral(builder.build())
            } else {
                val score = leaderBoard[index].points
                val displayIndex = index + 1
                val text = "У вас $displayIndex-е место в рейтинге с результатом в ${score.pointsString()}. Участвуйте в розыгрышах, " +
                        "задавайте вопросы, и получайте больше очков!"
                builder.text(text)
                client.chatPostEphemeral(builder.build())
            }
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

    fun messageInfo(channelId: String, messageTimestamp: String): ReactionsGetResponse.Message {
        val request = ReactionsGetRequest.builder()
            .channel(channelId)
            .timestamp(messageTimestamp)
            .build()
        val response = client.reactionsGet(request)
        return response.message
    }

    fun slackUserInfo(userId: String): SlackUser {
        val request = UsersInfoRequest.builder()
            .user(userId)
            .build()
        val slackUser = client.usersInfo(request).user

        return SlackUser(slackUser.id, slackUser.realName, slackUser.isAdmin, slackUser.isBot)
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

    data class SlackUser(val userId: String, val userName: String, val isAdmin: Boolean, val isBot: Boolean)

    companion object {
        const val ROW_SEPARATOR = '\n'
        const val DEFAULT_EMOJI = ":point_right:"
        val EMOJI_MAP = mapOf<Int, String>(
            1 to ":one:",
            2 to ":two:",
            3 to ":three:",
            4 to ":four:",
            5 to ":five:",
            6 to ":six:",
            7 to ":seven:",
            8 to ":eight:",
            9 to ":nine:",
            10 to ":one::zero:",
            11 to ":one::one:",
            12 to ":one::two:",
            13 to ":one::three:",
            14 to ":one::four:",
            15 to ":one::five:"
        )
    }
}

