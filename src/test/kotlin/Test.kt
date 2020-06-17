import com.slack.api.methods.response.reactions.ReactionsGetResponse
import com.slack.api.model.MatchedItem
import com.slack.api.model.Reaction
import org.junit.Assert
import org.junit.Test
import ru.katella.podlodkacrewslackapp.data.slack.SessionStatsBuilder
import ru.katella.podlodkacrewslackapp.data.slack.isUseful
import ru.katella.podlodkacrewslackapp.services.ProcessingService.Companion.TRIGGERING_REACTIONS
import ru.katella.podlodkacrewslackapp.services.SlackService.MessageWithReactions
import java.io.File
import java.nio.charset.Charset


class Test {

    @Test
    fun `Message isUseful return false when message text contains any reaction`() {
        val message = ReactionsGetResponse.Message()
        message.text = "Ставьте :+1: если у вас болит"
        Assert.assertFalse(message.isUseful(TRIGGERING_REACTIONS))
    }

    @Test
    fun `Message isUseful return true when message text doesn't contains any reaction`() {
        val message = ReactionsGetResponse.Message()
        message.text = "Ставьте :+2: если у вас болит"
        Assert.assertTrue(message.isUseful(TRIGGERING_REACTIONS))
    }

    @Test
    fun `SessionStatsBuilder generated output is in correct format `() {
        val reactionsInfo = ReactionsGetResponse.Message()
        reactionsInfo.text = "Спасибо всем, кто пришел на сессию “Ретро не нужны“ с докладом от <@U0120PN4AN5> и разбором от <@U011PBBCJ2X>, <@U0151KJ6FR6> и Алексея Пименова\n"
        reactionsInfo.reactions = listOf(
            Reaction("fire", 20, mutableListOf<String>(),""),
            Reaction("slightly_smiling_face", 10, mutableListOf<String>(),""),
            Reaction("neutral_face", 2, mutableListOf<String>(),""),
            Reaction("white_frowning_face", 1, mutableListOf<String>(),""),
            Reaction("face_with_symbols_on_mouth", 2, mutableListOf<String>(),"")
        )

        val matchedItem = MatchedItem()
        matchedItem.text = reactionsInfo.text
        matchedItem.username = "igrekde"
        matchedItem.ts = "1588233618.366700"

        val messageWithReactions = MessageWithReactions(matchedItem, reactionsInfo)

        val result = SessionStatsBuilder().build(listOf(messageWithReactions))
        val output = File("src/test/resources/sessionStatsTestOutput.csv").readText(Charset.defaultCharset())

        Assert.assertEquals(output, result.content)
    }
}