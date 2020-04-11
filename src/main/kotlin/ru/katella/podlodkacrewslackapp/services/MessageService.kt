package ru.katella.podlodkacrewslackapp.services

import com.slack.api.model.block.RichTextBlock
import com.slack.api.model.block.element.RichTextSectionElement
import com.slack.api.model.event.MessageEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class MessageService {

    @Autowired
    lateinit var commandProcessingService: CommandProcessingService

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
                            shouldCheckTextField = true
                        }
                        is RichTextSectionElement.Text -> {
                            if (shouldCheckTextField) {
                                val command = commandProcessingService.parseCommand(innerPart.text)
                                commandProcessingService.processCommand(currentUser, userId!!, command, currentChannel)
                            }
                        }
                    }
                }
            }
        }
    }


}

sealed class Command

object NoOp : Command()
object Increment : Command()
object Decrement : Command()
class Increase(by: Int): Command()
class Decrease(by: Int): Command()
