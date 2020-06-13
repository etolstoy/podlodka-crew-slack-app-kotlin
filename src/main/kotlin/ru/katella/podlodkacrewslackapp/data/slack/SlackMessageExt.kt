package ru.katella.podlodkacrewslackapp.data.slack

import com.slack.api.methods.response.reactions.ReactionsGetResponse.Message

fun Message.isUseful(forbiddenReactions: List<String>) = forbiddenReactions.none { it in text }