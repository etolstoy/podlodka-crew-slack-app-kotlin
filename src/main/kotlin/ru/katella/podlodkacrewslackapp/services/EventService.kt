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
                var shouldCheckTextField = false
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

                                when (val command = parseCommand(innerPart.text)) {
                                    is PointsOperation -> processingService.processPoints(currentUser, userId!!, command, currentChannel)
                                    is Lottery -> processingService.processLottery(currentUser, userId!!, command, currentChannel, message.threadTs)
                                    is NoOp -> return
                                }

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

    private fun parseCommand(text: String): Operation {
        return when {
            text.contains("разыгрываем") -> {
                parseLotteryCommand(text)
            }
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

    private fun parseLotteryCommand(text: String): Operation {
        val numbers = Regex("[0-9]+").findAll(text)
            .map { it.value.toInt() }
            .toList()

        return if (numbers.size == 2) {
            Lottery(numbers[0], numbers[1])
        } else {
            NoOp
        }
    }
}

sealed class Operation
sealed class PointsOperation: Operation()

object NoOp : Operation()
object Increment : PointsOperation()
object Decrement : PointsOperation()
data class Increase(val by: Int): PointsOperation()
data class Decrease(val by: Int): PointsOperation()
data class Lottery(val participants: Int, val pointsPerParticipant: Int): Operation()
