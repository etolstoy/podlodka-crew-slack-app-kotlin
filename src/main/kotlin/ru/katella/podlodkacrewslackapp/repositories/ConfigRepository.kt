package ru.katella.podlodkacrewslackapp.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface ConfigRepository: JpaRepository<Config, Int>

@Entity
@Table(name = "config")
data class Config(@Id val id: Int, var gameNotificationsChannel: String = "testing")