package ru.katella.podlodkacrewslackapp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface ReactionsRepository: JpaRepository<Message, String> {
    fun findByTimestampAndChannel(timestamp: String, channel: String): List<Message>
}

@Entity
@Table(name = "messages")
data class Message(@Id val timestamp: String, val channel: String)



