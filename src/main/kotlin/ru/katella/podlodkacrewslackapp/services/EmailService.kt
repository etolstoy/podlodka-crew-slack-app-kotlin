package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService {

    @Autowired
    lateinit var emailSender: JavaMailSender

    fun sendEmail(to: String, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.from = "podlodkacast@gmail.com"
        message.setTo(to)
        message.subject = subject
        message.text = text

        emailSender.send(message)
    }
}