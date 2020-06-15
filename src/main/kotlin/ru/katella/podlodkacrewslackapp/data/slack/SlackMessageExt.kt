package ru.katella.podlodkacrewslackapp.data.slack

import com.slack.api.methods.response.reactions.ReactionsGetResponse.Message

fun Message.isUseful(forbiddenReactions: List<String>) = forbiddenReactions.none { it in text }

fun Message.reactionTotal(reaction: String) = reactions.find { it.name == reaction }?.count ?: 0