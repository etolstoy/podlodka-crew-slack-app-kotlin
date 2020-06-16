package ru.katella.podlodkacrewslackapp.data.slack

import com.slack.api.methods.response.reactions.ReactionsGetResponse.Message as SlackMessage

class SessionStatsBuilder() {
    private val colSeparator = ","
    private var rowSeparator = "\r\n"

    fun build(sessionMessages: List<SlackMessage>): StatsFile {
        val meta = metaItems.map(SessionMetaItem::title)
        val reactions = rateReactions.map(Reaction::value)
        val total = listOf("Всего", "Рейтинг")
        val header = meta.plus(reactions).plus(total).joinToString(colSeparator)
        val sessionRows = sessionMessages.map { buildSessionRow(it) }
        return StatsFile( header + rowSeparator + sessionRows.joinToString(rowSeparator), "stats.csv")
    }

    private fun buildSessionRow(message: SlackMessage): String {
        val metaItems = metaItems.map { it.predicate(message) }
        //val reactionsCounts = rateReactions.map { Pair(it.value, message.reactionTotal(it.title)) }
        val reactionsCounts: Map<Int, Int>
        if (message.reactions == null) {
            println("Message with null reactions $message")
            reactionsCounts = mapOf(1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5)
        } else {
            reactionsCounts = rateReactions.associateBy( { it.value}, { message.reactionTotal(it.title) - 1 })
        }

        val sum = reactionsCounts.values.sum()
        val total = reactionsCounts.map { it.key * it.value }.sum() / sum.toFloat()
        return metaItems.plus(reactionsCounts.values).plus(listOf(sum, total)).joinToString(colSeparator)
    }

    class SessionMetaItem(val title: String, val predicate: (SlackMessage) -> String)

    private val metaItems = listOf(
        SessionMetaItem("Сессия") {
            val text = it.text
                .substringAfter("сессию ")
                .substringBefore(" с ")
                .substringBefore("\n")
            "\"$text\""
        },
        SessionMetaItem("Эксперт") {
            val text = it.text
                .substringAfter("с ")
                .substringBefore("\n")
            "\"$text\""
        },
        SessionMetaItem("Ведущий") {
            "\"${it.username}\""
        }
    )

    data class StatsFile(val content: String, val name: String)

    companion object {

        data class Reaction(val title: String, val value: Int)

        private val rateReactions = listOf(
            Reaction("fire", 5),
            Reaction("slightly_smiling_face",4),
            Reaction("neutral_face", 3),
            Reaction("white_frowning_face", 2),
            Reaction("face_with_symbols_on_mouth", 1)
        )
    }
}