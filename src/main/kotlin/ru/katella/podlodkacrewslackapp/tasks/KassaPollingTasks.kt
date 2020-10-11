package ru.katella.podlodkacrewslackapp.tasks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.katella.podlodkacrewslackapp.services.OrderCacheService
import ru.katella.podlodkacrewslackapp.services.OrderCreateService
import ru.katella.podlodkacrewslackapp.services.YandexKassaService

@Configuration
@EnableScheduling
class KassaPollingTasks {
    @Autowired
    lateinit var orderCacheService: OrderCacheService

    @Autowired
    lateinit var kassaService: YandexKassaService

    @Autowired
    lateinit var orderCreateService: OrderCreateService

    @Scheduled(fixedRate = 5000)
    fun pollForStatusUpdates() {
        // Проверяем, что есть pending запросы в нашей базе
        val orders = orderCacheService.obtainCachedOrders()

        // Проверяем статус этих запросов у Яндекс Кассы
        orders.forEach {
            println(it)
            val status = kassaService.getPaymentStatus(it.confirmationId)
            if (status == "cancelled" || status == "succeeded") {
                // В этом случае надо создать запись в AirTable, чтобы пользователю отправилось письмо
//                orderCreateService.createOrder(it)

                // Удаляем запись из кэша
            }
        }

    }

    // TODO: отдельно надо чекать синк кассы и эйртейбла
}