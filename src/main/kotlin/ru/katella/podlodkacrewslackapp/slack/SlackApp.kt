package ru.katella.podlodkacrewslackapp.slack

import com.slack.api.Slack
import com.slack.api.bolt.App
import com.slack.api.bolt.handler.BoltEventHandler
import com.slack.api.bolt.handler.builtin.SlashCommandHandler
import com.slack.api.methods.MethodsClient
import com.slack.api.model.event.AppMentionEvent
import com.slack.api.model.event.MessageEvent
import com.slack.api.model.event.ReactionAddedEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.katella.podlodkacrewslackapp.services.EventService

@Configuration
class SlackApp {

    @Autowired
    private lateinit var eventService: EventService

    @Bean
    fun initSlackApp(): App {
        val app = App()
        app.command("hello", SlashCommandHandler { _, ctx ->
            return@SlashCommandHandler ctx.ack("What's up")
        })
        app.event(MessageEvent::class.java, BoltEventHandler { event, ctx ->
            eventService.processMessage(message = event.event)

            ctx.ack()
        })
        app.event(ReactionAddedEvent::class.java, BoltEventHandler { event, ctx ->
            eventService.processReaction(event.event)
            ctx.ack()
        })
        app.event(AppMentionEvent::class.java) { event, ctx ->
            eventService.processAppMention(event = event.event)
            return@event ctx.ack()
        }
        return app
    }

    @Bean
    fun methodClient(): MethodsClient {
        val token = System.getenv("SLACK_BOT_TOKEN")
        return Slack.getInstance().methods(token)
    }
}