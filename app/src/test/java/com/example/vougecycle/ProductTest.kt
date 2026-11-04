package com.example.vougecycle

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProductTest {

    @Test
    fun priceFormatted_zeroPrice_returnsR0() {
        val product = Product(price = 0.0)
        assertThat(product.priceFormatted()).isEqualTo("R 0.00")
    }

    @Test
    fun priceFormatted_wholeNumber_formatsWithTwoDecimals() {
        val product = Product(price = 1700.0)
        assertThat(product.priceFormatted()).isEqualTo("R 1,700.00")
    }

    @Test
    fun priceFormatted_decimalPrice_formatsWithCommaSeparator() {
        val product = Product(price = 1500.50)
        assertThat(product.priceFormatted()).isEqualTo("R 1,500.50")
    }

    @Test
    fun priceFormatted_largePrice_includesThousandsSeparators() {
        val product = Product(price = 12345.67)
        assertThat(product.priceFormatted()).isEqualTo("R 12,345.67")
    }

    @Test
    fun isSold_defaultsToFalse() {
        val product = Product()
        assertThat(product.isSold).isFalse()
    }

    @Test
    fun productEquality_sameFields_areEqual() {
        val a = Product(id = "1", title = "Shirt", price = 100.0)
        val b = Product(id = "1", title = "Shirt", price = 100.0)
        assertThat(a).isEqualTo(b)
    }
}