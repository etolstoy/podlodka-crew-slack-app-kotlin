package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.repositories.Config
import ru.katella.podlodkacrewslackapp.repositories.ConfigRepository

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

    fun getGameNotificationChannel(teamId: String): String = getConfig(teamId).gameNotificationsChannel
}