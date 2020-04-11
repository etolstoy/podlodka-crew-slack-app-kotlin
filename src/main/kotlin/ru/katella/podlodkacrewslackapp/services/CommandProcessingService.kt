package ru.katella.podlodkacrewslackapp.services

import com.slack.api.methods.MethodsClient
import com.slack.api.model.block.LayoutBlock
import com.slack.api.model.block.RichTextBlock
import com.slack.api.model.block.element.BlockElement
import com.slack.api.model.block.element.RichTextSectionElement
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

typealias UserId = String

@Service
class CommandProcessingService {

    @Autowired
    lateinit var client: MethodsClient

    fun processCommand(fromUser: UserId, toUser: UserId, command: Command, channelId: String) {
        println("user $fromUser send $command to $toUser")


//        val fromUserSectionElement = RichTextSectionElement.User
//            .builder()
//            .userId(fromUser)
//            .build()
//        val textSectionElement = RichTextSectionElement.Text
//            .builder()
//            .text(" sent $command to ")
//            .build()
//        val toUserSectionElement = RichTextSectionElement.User
//            .builder()
//            .userId(fromUser)
//            .build()
//        val blockElement = RichTextSectionElement
//            .builder()
//            .elements(arrayListOf(fromUserSectionElement, textSectionElement, toUserSectionElement))
//            .build()
//        val blockElements = arrayListOf<BlockElement>(blockElement)
//        val blocks = arrayListOf<LayoutBlock>(RichTextBlock.builder().elements(blockElements).build())

        val text = "<@$fromUser> sent $command to <@$toUser>"

        client.chatPostMessage {
            it.channel(channelId)
                .text(text)
        }
    }

    fun parseCommand(text: String): Command {
        println(text)
        return when {
            text.contains("++") -> {
                Increment
            }
            text.contains("--") -> {
                Decrement
            }
            text.contains("+") -> {
                val delimiter = "+"
                val number = text.split(delimiter)[1].toIntOrNull()
                if (number != null) {
                    Increase(number)
                } else NoOp
            }
            text.contains("-") -> {
                val delimiter = "-"
                val number = text.split(delimiter)[1].toIntOrNull()
                if (number != null) {
                    Decrease(number)
                } else NoOp
            }
            else -> {
                NoOp
            }
        }
    }
}