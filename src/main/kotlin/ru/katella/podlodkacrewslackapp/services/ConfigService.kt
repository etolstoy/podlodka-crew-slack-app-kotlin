package ru.katella.podlodkacrewslackapp.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.katella.podlodkacrewslackapp.repositories.Config
import ru.katella.podlodkacrewslackapp.repositories.ConfigRepository

@Service
class ConfigService {

    @Autowired
    private lateinit var configRepository: ConfigRepository

    private var _config: Config? = null
    val config: Config
        get() {
            if (_config == null) {
                val dbConfig = configRepository.findById(0)
                _config = if (!dbConfig.isPresent) {
                    configRepository.saveAndFlush(Config(0))
                } else {
                    dbConfig.get()
                }
            }
            return _config ?: throw AssertionError("Wrong config")
        }

    val gameNotificationChannel: String by lazy { config.gameNotificationsChannel }

    fun changeGameNotificationChannel(channel: String) {
        val newConfig = config.copy(gameNotificationsChannel = channel)
        configRepository.saveAndFlush(newConfig)
        _config = newConfig
    }
}