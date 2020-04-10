package ru.katella.podlodkacrewslackapp.slack

import com.slack.api.app_backend.events.EventHandler
import com.slack.api.bolt.App
import com.slack.api.bolt.handler.BoltEventHandler
import com.slack.api.bolt.handler.builtin.SlashCommandHandler
import com.slack.api.model.event.MessageBotEvent
import com.slack.api.model.event.MessageEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.regex.Pattern

@Configuration
class SlackApp {

    @Bean
    fun initSlackApp(): App {
        val app = App()
        app.command("hello", SlashCommandHandler { req, ctx ->
            println("DEBUG command hello received")
            return@SlashCommandHandler ctx.ack("What's up")
        })
        app.event(MessageEvent::class.java, BoltEventHandler { event, ctx ->
            print("DEBUG event ${event.eventId} received with args ${event.event.text}")
            ctx.client().chatPostMessage {
                it.channel("testing")
                    .text("event {${event.eventId}}received")
            }
            ctx.ack()
        })
        return app
    }
}