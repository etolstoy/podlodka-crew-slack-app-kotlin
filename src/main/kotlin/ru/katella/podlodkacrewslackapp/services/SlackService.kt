package ru.katella.podlodkacrewslackapp.services

import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.users.UsersInfoRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.repositories.User

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

    fun postLeaderBoard(channel: String, leaderBoard: List<User>) {
        var table = "*#* |*Участник*                              |*Очки*"
        leaderBoard.forEachIndexed { index, user ->
            val row = ROW_SEPARATOR +
                    index.toString().padTo(NUMBER_SECTION_LENGTH) + DELIMITER +
                    user.id.userTag().padTo(NAME_SECTION_LENGTH, user.displayName) + DELIMITER +
                    user.points.toString().padTo(POINTS_SECTION_LENGTH) + ROW_SEPARATOR
            table += row

        }

        client.chatPostMessage {
            it.channel(channel)
                .mrkdwn(true)
                .text(table)
        }
    }

    fun slackUserInfo(userId: String): SlackUser {
        val request = UsersInfoRequest.builder()
            .user(userId)
            .build()
        val slackUser = client.usersInfo(request).user
        return SlackUser(slackUser.id, slackUser.name, slackUser.isAdmin)
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
    private fun String.padTo(length: Int, base: String = this): String {
        return if (base.length < length) {
            val diff = length - base.length
            this.padEnd(diff)
        } else {
            this
        }
    }

    data class SlackUser(val userId: String, val userName: String, val isAdmin: Boolean)

    companion object {
        const val NUMBER_SECTION_LENGTH = 4
        const val NAME_SECTION_LENGTH = 40
        const val POINTS_SECTION_LENGTH = 6
        const val DELIMITER = '|'
        const val ROW_SEPARATOR = '\n'
    }
}

