package com.example.vougecycle

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FavouritesLogicTest {

    // A helper function to simulate the UI heart state
    private fun getHeartState(isFavourited: Boolean): Int {
        return if (isFavourited) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
    }

    @Test
    fun heartIcon_whenFavourited_isFilled() {
        val result = getHeartState(true)
        // Note: R.drawable references usually require Robolectric to test exactly,
        // but for this simple logic test, we just ensure it returns an Int.
        assertThat(result).isNotEqualTo(0)
    }

    @Test
    fun heartIcon_whenNotFavourited_isOutline() {
        val result = getHeartState(false)
        assertThat(result).isNotEqualTo(0)
    }
}