import org.junit.Assert
import org.junit.Test
import ru.katella.podlodkacrewslackapp.services.PriceService
import ru.katella.podlodkacrewslackapp.services.PromoCalculatorService
import ru.katella.podlodkacrewslackapp.utils.AirTablePromo


class PromoCalculatorServiceTest {
    val calculator = PromoCalculatorService()

    @Test
    fun `Calculator counts correct price for one offer and single fix price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val usageLeft = 1

        val expectedResult = promoPrice

        val result = calculator.countPrice(
                generateOffers(1, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        type = AirTablePromo.PROMO_TYPE_LIMITED,
                        priceType = AirTablePromo.PRICE_TYPE_FIXED,
                        isActive = true,
                        usageLeft = usageLeft
        ))
        Assert.assertTrue(result.bulkPrice == expectedResult)
        Assert.assertTrue(result.promoUsageLeft == 0)
    }

    @Test
    fun `Calculator counts correct price for two offers and single fix price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 2
        val usageLeft = 1

        val expectedResult = offerPrice + promoPrice

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        type = AirTablePromo.PROMO_TYPE_LIMITED,
                        priceType = AirTablePromo.PRICE_TYPE_FIXED,
                        isActive = true,
                        usageLeft = usageLeft
        ))
        val firstPromoOffer = result.promoOffers[0]
        val lastPromoOffer = result.promoOffers[1]

        Assert.assertTrue(result.bulkPrice == expectedResult)
        Assert.assertTrue(firstPromoOffer.promoPrice == promoPrice)
        Assert.assertTrue(lastPromoOffer.promoPrice == offerPrice)
        Assert.assertTrue(result.promoUsageLeft == 0)
    }

    @Test
    fun `Calculator counts correct price for five offers and 3-usage limited fix price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 5
        val usageLeft = 3

        val expectedResult = usageLeft * promoPrice + (offerCount - usageLeft) * offerPrice

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        type = AirTablePromo.PROMO_TYPE_LIMITED,
                        priceType = AirTablePromo.PRICE_TYPE_FIXED,
                        isActive = true,
                        usageLeft = usageLeft
                ))
        Assert.assertTrue(result.bulkPrice == expectedResult)
        Assert.assertTrue(result.promoUsageLeft == 0)
    }

    @Test
    fun `Calculator counts correct price for two offers and unlimited fix price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 2
        val usageLeft = 1

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
        Assert.assertTrue(result.bulkPrice == expectedResult)
        Assert.assertTrue(result.promoUsageLeft == 0)
    }

    @Test
    fun `Calculator counts correct price for one offer and single decrease price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val usageLeft = 1

        val expectedResult = offerPrice - promoPrice

        val result = calculator.countPrice(
                generateOffers(1, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        type = AirTablePromo.PROMO_TYPE_LIMITED,
                        isActive = true,
                        usageLeft = usageLeft
                ))
        Assert.assertTrue(result.bulkPrice == expectedResult)
        Assert.assertTrue(result.promoUsageLeft == 0)
    }

    @Test
    fun `Calculator counts correct price for two offers and single decrease price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 2
        val usageLeft = 1

        val expectedResult = offerPrice * offerCount - promoPrice

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        type = AirTablePromo.PROMO_TYPE_LIMITED,
                        isActive = true,
                        usageLeft = usageLeft
                ))
        Assert.assertTrue(result.bulkPrice == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for five offers and 3-usage limited decrease price`() {
        val promoId = "promo_1"
        val offerPrice = 1000.0
        val promoPrice = 100.0
        val offerCount = 5
        val usageLeft = 3

        val expectedResult = usageLeft * (offerPrice - promoPrice) + (offerCount - usageLeft) * offerPrice

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        type = AirTablePromo.PROMO_TYPE_LIMITED,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        isActive = true,
                        usageLeft = usageLeft
                ))
        Assert.assertTrue(result.bulkPrice == expectedResult)
    }

    @Test
    fun `Calculator counts correct price for two offers and single decrease price with discount higher than price`() {
        val promoId = "promo_1"
        val offerPrice = 100.0
        val promoPrice = 1000.0
        val offerCount = 2
        val usageLeft = 1

        val expectedResult = 0 + offerPrice

        val result = calculator.countPrice(
                generateOffers(offerCount, promoId, offerPrice),
                PriceService.Promo(
                        id = promoId,
                        price = promoPrice,
                        priceType = AirTablePromo.PRICE_TYPE_DECREASE,
                        type = AirTablePromo.PROMO_TYPE_LIMITED,
                        isActive = true,
                        usageLeft = usageLeft
                ))
        Assert.assertTrue(result.bulkPrice == expectedResult)
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
        val firstPromoOffer = result.promoOffers[0]
        val lastPromoOffer = result.promoOffers[1]

        Assert.assertTrue(result.bulkPrice == expectedResult)
        Assert.assertTrue(firstPromoOffer.promoPrice == offerPrice - promoPrice)
        Assert.assertTrue(lastPromoOffer.promoPrice == offerPrice - promoPrice)
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
        Assert.assertTrue(result.bulkPrice == expectedResult)
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
        Assert.assertTrue(result.bulkPrice == expectedResult)
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
        Assert.assertTrue(result.bulkPrice == expectedResult)
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