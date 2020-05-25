package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.data.repositories.Config
import ru.katella.podlodkacrewslackapp.data.repositories.ConfigRepository

@Service
class ConfigService {

    @Autowired
    private lateinit var configRepository: ConfigRepository

    private val configsCache = mutableMapOf<String, Config>()

    fun getConfig(teamId: String): Config {
        return configsCache.getOrPut(teamId) {
            val dbConfig = configRepository.findById(teamId)
            if (!dbConfig.isPresent) {
                configRepository.saveAndFlush(Config(teamId))
            } else {
                dbConfig.get()
            }
        }
    }

    fun isGameActive(teamId: String): Boolean = getConfig(teamId).gameIsActive

    fun setGameActive(teamId: String, isActive: Boolean) {
        val config = getConfig(teamId)
        if (config.gameIsActive == isActive) {
            return
        } else {
            val newConfig = config.copy(gameIsActive = isActive)
            configRepository.saveAndFlush(newConfig)
            configsCache[teamId] = newConfig
        }
    }

    fun getGameNotificationChannel(teamId: String): String = getConfig(teamId).gameNotificationsChannel
}