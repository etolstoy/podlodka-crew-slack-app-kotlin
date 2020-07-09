package ru.katella.podlodkacrewslackapp.data

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import java.util.*


@Configuration
class MailSenderConfig {

    @Bean
    fun getMailSender(): JavaMailSender {
        val mailSender = JavaMailSenderImpl()
        System.getenv("DATABASE_URL")
        mailSender.username = System.getenv("EMAIL_USER")// "podlodkacast@gmail.com"
        mailSender.password = System.getenv("EMAIL_PASSWORD")//"vEn6oay[Zdsp"
        mailSender.host = "smtp.gmail.com"
        mailSender.port = 587

        val properties = Properties()
        properties.put("mail.smtps.auth", "true")
        properties.put("mail.smtp.starttls.enable", "true")

        mailSender.javaMailProperties = properties
        return mailSender
    }
}