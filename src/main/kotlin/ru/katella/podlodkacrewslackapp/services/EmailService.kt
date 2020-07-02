package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService {

    @Autowired
    lateinit var emailSender: JavaMailSender

    @Value("\${shop.email}")
    lateinit var fromEmail: String

    fun sendEmail(to: String, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.from = fromEmail
        message.setTo(to)
        message.subject = subject
        message.text = text

        emailSender.send(message)
    }
}