package ru.katella.podlodkacrewslackapp.slack

import com.slack.api.app_backend.SlackSignature
import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.middleware.Middleware
import com.slack.api.bolt.middleware.builtin.RequestVerification
import ru.katella.podlodkacrewslackapp.slack.security.CustomRequestVerification

class ExtendedSlackBoltApp: App() {

    override fun buildDefaultMiddlewareList(appConfig: AppConfig?): MutableList<Middleware> {
        val list = super.buildDefaultMiddlewareList(appConfig)
        val workspacePairs = System.getenv("SLACK_WORKSPACES_WITH_SECRETS").split(",")
        val verifiers = workspacePairs
            .map { it.split(":") }
            .map { it[1] }
            .map { SlackSignature.Verifier(SlackSignature.Generator(it)) }


        val securityMiddleware = CustomRequestVerification(verifiers)
        list.replaceAll {
            if (it is RequestVerification) {
                securityMiddleware
            } else {
                it
            }
        }
        return list
    }


}