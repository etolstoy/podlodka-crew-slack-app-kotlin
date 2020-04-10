package ru.katella.podlodkacrewslackapp.slack

import com.slack.api.Slack
import com.slack.api.bolt.App
import com.slack.api.bolt.handler.BoltEventHandler
import com.slack.api.bolt.handler.builtin.SlashCommandHandler
import com.slack.api.methods.MethodsClient
import com.slack.api.model.event.MessageEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.katella.podlodkacrewslackapp.services.MessageService

@Configuration
class SlackApp {

    @Autowired
    lateinit var messageService: MessageService

    @Bean
    fun initSlackApp(): App {
        val app = App()
        app.command("hello", SlashCommandHandler { req, ctx ->
            println("DEBUG command hello received")
            return@SlashCommandHandler ctx.ack("What's up")
        })
        app.event(MessageEvent::class.java, BoltEventHandler { event, ctx ->
            print("DEBUG event ${event.eventId} received with args ${event.event.text}")
            event.authedUsers.forEach { println(it) }
            messageService.processMessage(message = event.event)

            ctx.ack()
        })
        return app
    }

    @Bean
    fun methodClient(): MethodsClient {
        val token = System.getenv("SLACK_BOT_TOKEN")
        return Slack.getInstance().methods(token)
    }
}