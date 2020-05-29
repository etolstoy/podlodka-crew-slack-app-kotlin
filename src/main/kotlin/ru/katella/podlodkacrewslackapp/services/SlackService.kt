package ru.katella.podlodkacrewslackapp.services

import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.chat.ChatPostEphemeralRequest
import com.slack.api.methods.response.reactions.ReactionsGetResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.repositories.User
import kotlin.math.absoluteValue

@Service
class SlackService {

    @Autowired
    lateinit var client: MethodsClient
    @Autowired
    lateinit var configService: ConfigService

    fun postUserReceivedPointsFrom(teamId: String, receivingUserId: String, donatingUserId: String, receivedPoints: Int, total: Int) {
        val channelName = configService.getGameNotificationChannel(teamId)

        val text = "${receivingUserId.userTag()} получает " +
                "${receivedPoints.pointsString()} от " +
                "${donatingUserId.userTag()}. " +
                "Всего очков: $total"

        client.chatPostMessage {
            it.token(getSlackToken(teamId))
                .channel(channelName)
                .text(text)
        }
    }

    fun postUserReceivedPointsForLottery(teamId: String, receivingUserId: String, receivedPoints: Int, total: Int) {
        val channelName = configService.getGameNotificationChannel(teamId)

        val text = "${receivingUserId.userTag()} получает " +
                "${receivedPoints.pointsString()} за участие в голосовании и капельку везения! " +
                "Всего очков: $total"

        client.chatPostMessage {
            it.token(getSlackToken(teamId))
                .channel(channelName)
                .text(text)
        }
    }

    fun postUserReceivedPointsForReactions(teamId: String, originalMessage: String, receivingUserId: String, receivedPoints: Int, total: Int) {
        val channelName = configService.getGameNotificationChannel(teamId)

        val text = "${receivingUserId.userTag()} получает " +
                "${receivedPoints.pointsString()} за десять или более :thumbsup: <$originalMessage|здесь> \nВсего очков: *$total*"

        client.chatPostMessage {
            it.token(getSlackToken(teamId))
                .channel(channelName)
                .unfurlLinks(true)
                .text(text)
        }
    }

    fun postLotteryWinnersToThread(teamId: String, winners: List<String>, points: Int, channelId: String, threadId: String) {
        val text = "В розыгрыше победили: ${winners.joinToString(", ", transform = { it.userTag() })}! " +
                "Поздравляем! Вы получаете по ${points.pointsString()}"

        client.chatPostMessage {
            it.token(getSlackToken(teamId))
                .channel(channelId)
                .threadTs(threadId)
                .text(text)
        }
    }

    fun postEphemeralMessage(teamId: String, channelId: String, userId: String, text: String) {
        client.chatPostEphemeral {
            it.token(getSlackToken(teamId))
                .channel(channelId)
                .user(userId)
                .text(text)
        }
    }

    fun postLeaderBoard(teamId: String, userId: String, channel: String, leaderBoard: List<User>) {
        var table = "*Топ-15 участников конкурса:*"
        var isUserPresentedInTop = false
        leaderBoard.take(20).forEachIndexed { index, user ->
            val outputIndex = index + 1
            if (user.id == userId) isUserPresentedInTop = true
            val separator: String = if (index == 15) {
                "$ROW_SEPARATOR*Участники, которым нужно чуть-чуть поднажать, чтобы ворваться в топ!*:runner::muscle::top:"
            } else {
                ROW_SEPARATOR.toString()
            }

            val row = separator +
                    EMOJI_MAP.getOrElse(outputIndex, { if (outputIndex < 10) "  $outputIndex. " else "$outputIndex. "} )+
                    user.id.userTag() + " – " + user.points.pointsString()
            table += row
        }

        client.chatPostMessage {
            it.token(getSlackToken(teamId))
                .channel(channel)
                .mrkdwn(true)
                .text(table)
        }
        if (!isUserPresentedInTop) {
            val index = leaderBoard.indexOfFirst { it.id == userId }
            val builder = ChatPostEphemeralRequest.builder()
                .token(getSlackToken(teamId))
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

    fun postFavoriteHost(teamId: String, channel: String, hostId: String) {

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
            it.token(getSlackToken(teamId))
                .channel(channel)
                .text(randomText)
        }
    }

    fun messageInfo(teamId: String, channelId: String, messageTimestamp: String): ReactionsGetResponse.Message {
        val response = client.reactionsGet {
            it.token(getSlackToken(teamId))
                .channel(channelId)
                .timestamp(messageTimestamp)
        }
        return response.message
    }

    fun slackUsersList(teamId: String): List<SlackUser> {
        val users = client.usersList {
            it.token(getSlackToken(teamId))
        }
        return users.members.map { SlackUser(it.id, it.realName, it.teamId, it.isAdmin, it.isBot) }
    }

    fun slackAdminsList(teamId: String): List<SlackUser> = slackUsersList(teamId).filter { it.isAdmin }

    fun slackBotsList(teamId: String): List<SlackUser> = slackUsersList(teamId).filter { it.isBot }

    fun slackUserInfo(teamId: String, userId: String): SlackUser {
        val slackUser = client.usersInfo {
            it.token(getSlackToken(teamId))
                .user(userId)
        }.user

        return SlackUser(slackUser.id, slackUser.realName, slackUser.teamId, slackUser.isAdmin, slackUser.isBot)
    }

    fun isUserAdmin(teamId: String, userId: String): Boolean {
        return try {
            val user = slackUserInfo(teamId, userId)
            user.isAdmin
        } catch (ex: Exception) {
            false
        }
    }

    private fun String.userTag(): String = "<@$this>"

    private fun Int.pointsString(): String {
        var result = "$this "
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

    private fun getSlackToken(teamId: String): String = System.getenv("SLACK_BOT_TOKEN_$teamId")

    data class SlackUser(val userId: String, val userName: String, val teamId: String, val isAdmin: Boolean, val isBot: Boolean)

    companion object {
        const val ROW_SEPARATOR = '\n'
        val EMOJI_MAP = mapOf(
            1 to ":first_place_medal: ",
            2 to ":second_place_medal: ",
            3 to ":third_place_medal: "
        )
    }
}

