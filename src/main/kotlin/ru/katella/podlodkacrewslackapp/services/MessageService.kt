package ru.katella.podlodkacrewslackapp.services

import com.slack.api.bolt.App
import com.slack.api.methods.MethodsClient
import com.slack.api.methods.request.users.UsersInfoRequest
import com.slack.api.model.block.RichTextBlock
import com.slack.api.model.block.element.RichTextElement
import com.slack.api.model.block.element.RichTextSectionElement
import com.slack.api.model.block.element.UsersSelectElement
import com.slack.api.model.event.MessageEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.slack.SlackApp
import kotlin.contracts.contract

@Service
class MessageService {



    @Autowired
    lateinit var likeService: LikeService

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
                            if (shouldCheckTextField && blockContainsIncrementLogic(innerPart.text)) {
                                likeService.processLike(currentUser, userId!!, innerPart.text, currentChannel)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun blockContainsIncrementLogic(text: String): Boolean {
        return text.contains("++")
    }
}