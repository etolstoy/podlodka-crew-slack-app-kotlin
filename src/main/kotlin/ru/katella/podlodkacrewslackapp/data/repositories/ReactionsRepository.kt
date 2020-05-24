package ru.katella.podlodkacrewslackapp.data.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface ReactionsRepository: JpaRepository<Message, String> {
    fun findByTimestampAndTeamIdAndChannel(timestamp: String, teamId: String, channel: String): List<Message>
    fun deleteByTeamId(teamId: String)
}

@Entity
@Table(name = "messages")
data class Message(@Id val timestamp: String, val teamId: String, val channel: String)



