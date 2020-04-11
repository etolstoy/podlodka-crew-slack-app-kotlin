package ru.katella.podlodkacrewslackapp

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface UserRepository: JpaRepository<User, String>

@Entity
@Table(name = "users")
data class User(@Id val id: String, var likesLimit: Int, var likesReceived: Int)