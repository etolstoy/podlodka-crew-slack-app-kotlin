package ru.katella.podlodkacrewslackapp.tasks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import ru.katella.podlodkacrewslackapp.services.OrderCacheService
import ru.katella.podlodkacrewslackapp.services.CRMService
import ru.katella.podlodkacrewslackapp.services.YandexKassaService

@Configuration
@EnableScheduling
class KassaPollingTasks {
    @Autowired
    lateinit var orderCacheService: OrderCacheService

    @Autowired
    lateinit var kassaService: YandexKassaService

    @Autowired
    lateinit var crmService: CRMService

    @Scheduled(fixedRate = 5000)
    fun pollForStatusUpdates() {
        // Проверяем, что есть pending запросы в нашей базе
        val orders = orderCacheService.obtainCachedOrders()

        // Проверяем статус этих запросов у Яндекс Кассы
        orders.forEach {
            val status = kassaService.getPaymentStatus(it.confirmationId)
            if (status == "cancelled" || status == "succeeded") {
                // В этом случае надо создать занести в базу все новые записи – Order, Participant, Ticket


                // Удаляем запись из кэша
            }
        }

    }

    // TODO: отдельно надо чекать синк кассы и эйртейбла
}