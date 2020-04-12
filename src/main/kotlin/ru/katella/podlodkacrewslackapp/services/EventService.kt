package ru.katella.podlodkacrewslackapp.services

import com.slack.api.model.block.RichTextBlock
import com.slack.api.model.block.element.RichTextSectionElement
import com.slack.api.model.event.MessageEvent
import com.slack.api.model.event.ReactionAddedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventService {

    @Autowired
    lateinit var processingService: ProcessingService

    fun processMessage(message: MessageEvent) {

        val currentChannel = message.channel
        val currentUser = message.user
        message.blocks.forEach { block ->
            if (block is RichTextBlock) {
                var shouldCheckTextField = true
                var userId: String? = null

                val element = block.elements.first { it is RichTextSectionElement } as RichTextSectionElement

                element.elements.forEach { innerPart ->
                    when (innerPart) {
                        is RichTextSectionElement.User -> {
                            userId = innerPart.userId
                            shouldCheckTextField = !userId.isNullOrEmpty()
                        }
                        is RichTextSectionElement.Text -> {
                            if (shouldCheckTextField) {
                                val command = processingService.parseCommand(innerPart.text)
                                processingService.processPoints(currentUser, userId!!, command, currentChannel)
                            }
                        }
                    }
                }
            }
        }
    }

    fun processReaction(event: ReactionAddedEvent) {
        processingService.processNewReaction(
            event.item.channel,
            event.reaction,
            event.itemUser,
            event.user,
            event.item.ts)
    }


}

sealed class Operation

object NoOp : Operation()
object Increment : Operation()
object Decrement : Operation()
data class Increase(val by: Int): Operation()
data class Decrease(val by: Int): Operation()
