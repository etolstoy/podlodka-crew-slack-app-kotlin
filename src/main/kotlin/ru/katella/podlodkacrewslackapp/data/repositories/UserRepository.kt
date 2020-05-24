package ru.katella.podlodkacrewslackapp.data.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface UserRepository: JpaRepository<User, String> {
    fun findByTeamId(teamId: String): List<User>
}

@Entity
@Table(name = "users")
data class User(@Id val id: String, val displayName: String, val teamId: String, val isAdmin: Boolean, var points: Int = 0)