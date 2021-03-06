package ru.katella.podlodkacrewslackapp.data.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface ConfigRepository: JpaRepository<Config, String>

@Entity
@Table(name = "config")
data class Config(@Id val teamId: String,
                  var gameNotificationsChannel: String = "scoring",
                  var gameIsActive: Boolean = false)