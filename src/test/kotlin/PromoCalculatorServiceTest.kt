import com.slack.api.methods.response.reactions.ReactionsGetResponse
import com.slack.api.model.MatchedItem
import com.slack.api.model.Reaction
import org.junit.Assert
import org.junit.Test
import ru.katella.podlodkacrewslackapp.data.slack.SessionStatsBuilder
import ru.katella.podlodkacrewslackapp.data.slack.isUseful
import ru.katella.podlodkacrewslackapp.services.PriceService
import ru.katella.podlodkacrewslackapp.services.ProcessingService.Companion.TRIGGERING_REACTIONS
import ru.katella.podlodkacrewslackapp.services.PromoCalculatorService
import ru.katella.podlodkacrewslackapp.services.SlackService.MessageWithReactions
import ru.katella.podlodkacrewslackapp.utils.AirTablePromo
import java.io.File
import java.nio.charset.Charset


class PromoCalculatorServiceTest {
    val calculator = PromoCalculatorService()

    @Test
    fun `Calculator counts correct price for one offer and single fix price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0

        val expectedResult = promoPrice

        val result = calculator.countPrice(
                generateOffers(1, promoId, offerPrice),
                PriceService.Promo(
                    id = promoId,
                    price = promoPrice,
                    type = AirTablePromo.PROMO_TYPE_SINGLE,
                    priceType = AirTablePromo.PRICE_TYPE_FIXED,
                    isActive = true
        ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for two offers and single fix price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 2

        val expectedResult = offerPrice + promoPrice

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                    id = promoId,
                    price = promoPrice,
                    type = AirTablePromo.PROMO_TYPE_SINGLE,
                    priceType = AirTablePromo.PRICE_TYPE_FIXED,
                    isActive = true
        ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for two offers and unlimited fix price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 2

        val expectedResult = promoPrice * offerCount

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                    id = promoId,
                    price = promoPrice,
                    priceType = AirTablePromo.PRICE_TYPE_FIXED,
                    type = AirTablePromo.PROMO_TYPE_UNLIMITED,
                    isActive = true
        ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for one offer and single decrease price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0

        val expectedResult = offerPrice - promoPrice

        val result = calculator.countPrice(
                generateOffers(1, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        type = AirTablePromo.PROMO_TYPE_SINGLE,
                        isActive = true
                ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for two offers and single decrease price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 2

        val expectedResult = offerPrice * offerCount - promoPrice

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        type = AirTablePromo.PROMO_TYPE_SINGLE,
                        isActive = true
                ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for two offers and single decrease price with discount higher than price`() {
        val promoId = "promo_1"
        val offerPrice = 100.0
        val promoPrice = 1000.0
        val offerCount = 2

        val expectedResult = 0 + offerPrice

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        type = AirTablePromo.PROMO_TYPE_SINGLE,
                        isActive = true
                ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for two offers and unlimited decrease price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 2

        val expectedResult = (offerPrice - promoPrice) * offerCount

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        type = AirTablePromo.PROMO_TYPE_UNLIMITED,
                        isActive = true
                ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for two offers and unlimited decrease price with discount higher than price`() {
        val promoId = "promo_1"
        val offerPrice = 100.0
        val promoPrice = 1000.0
        val offerCount = 2

        val expectedResult = 0.0

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        type = AirTablePromo.PROMO_TYPE_UNLIMITED,
                        isActive = true
                ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for inactive promo`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0

        val expectedResult = offerPrice

        val result = calculator.countPrice(
                generateOffers(1, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_FIXED,
                        type = AirTablePromo.PROMO_TYPE_UNLIMITED,
                        isActive = false
                ))
        Assert.assertTrue(result == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for incorrect promo`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0

        val expectedResult = offerPrice

        val result = calculator.countPrice(
                generateOffers(1, promoId, offerPrice),
                PriceService.Promo(
                        id = "111222333",
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_FIXED,
                        type = AirTablePromo.PROMO_TYPE_UNLIMITED,
                        isActive = false
                ))
        Assert.assertTrue(result == expectedResult)
    }

    private fun generateOffers(count: Int, promoId: String, price: Number): List<PriceService.Offer> {
        val list = mutableListOf<PriceService.Offer>()
        for (i in 1..count) {
            list += PriceService.Offer(
                    id = "abcde",
                    price = price,
                    activePromoIds = listOf(promoId)
            )
        }
        return list.toList()
    }

}