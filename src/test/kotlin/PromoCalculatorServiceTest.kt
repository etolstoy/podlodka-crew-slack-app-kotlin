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


class PromoCalculatorServiceTest {

    @Test
    fun `Message isUseful return false when message text contains any reaction`() {
        val message = ReactionsGetResponse.Message()
        message.text = "Ставьте :+1: если у вас болит"
        Assert.assertFalse(message.isUseful(TRIGGERING_REACTIONS))
    }

}