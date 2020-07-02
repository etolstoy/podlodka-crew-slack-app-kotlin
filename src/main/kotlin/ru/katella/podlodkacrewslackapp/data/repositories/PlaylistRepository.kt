package ru.katella.podlodkacrewslackapp.data.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Repository
interface PlaylistRepository: JpaRepository<Playlist, String> {
    fun findByName(name: String): List<Playlist>
}

@Entity
@Table(name = "playlist")
data class Playlist(@Id val name: String,
                  var url: String)