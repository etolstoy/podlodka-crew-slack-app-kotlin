import com.slack.api.methods.response.reactions.ReactionsGetResponse
import org.junit.Assert
import org.junit.Test
import ru.katella.podlodkacrewslackapp.data.slack.isUseful
import ru.katella.podlodkacrewslackapp.services.ProcessingService.Companion.TRIGGERING_REACTIONS

class Test {

    @Test
    fun `isUseful return false when message text contains any reaction`() {
        val message = ReactionsGetResponse.Message()
        message.text = "Ставьте :+1: если у вас болит"
        Assert.assertFalse(message.isUseful(TRIGGERING_REACTIONS))
    }

    @Test
    fun `isUseful return true when message text doesn't contains any reaction`() {
        val message = ReactionsGetResponse.Message()
        message.text = "Ставьте :+2: если у вас болит"
        Assert.assertTrue(message.isUseful(TRIGGERING_REACTIONS))
    }
}