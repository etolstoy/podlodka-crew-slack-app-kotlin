package ru.katella.podlodkacrewslackapp.tasks

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import ru.katella.podlodkacrewslackapp.data.repositories.Order
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
        if (orders == null || orders.count() == 0) {
            return
        }

        // Группируем по ID заказа кассы, чтобы апдейтить все одним махом
        val kassaOrders = mutableMapOf<String, MutableList<Order>>()
        orders.forEach {
            if (kassaOrders[it.confirmationId] == null) {
                kassaOrders[it.confirmationId] = mutableListOf()
            }
            kassaOrders[it.confirmationId]?.add(it)
        }

        // Проверяем статус этих запросов у Яндекс Кассы
        kassaOrders.keys.forEach {
            val status = kassaService.getPaymentStatus(it)
            println(status)
            if (status == "cancelled" || status == "succeeded") {
                println(status + " " + it)
                // В этом случае надо создать занести в базу все новые записи – Order, Participant, Ticket
                kassaOrders[it]?.forEach { order ->
                    if (crmService.createOrder(order, status)) {
                        // Удаляем из кеша
                        orderCacheService.removeOrder(order)
                    }
                }
            }
        }
    }

    // TODO: Реализовать периодическую проверку того, что все последние записи в базе Кассы (скажем, за неделю), посинканы с AirTable. Это поможет закрыться от рассинхрона.
}