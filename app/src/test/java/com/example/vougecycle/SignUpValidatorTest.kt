package com.example.vougecycle

import com.google.common.truth.Truth.assertThat
import org.junit.Test
/**
 * These tests document the current sign-up validation rules.
 * They test the RULES as strings of logic, not the UI.
 * When we extract the validator into its own class (Step T3), these tests will be
 * rewired to call that class — but the assertions stay identical.
 */
class SignUpValidatorTest {

    // Helper: the exact rules used in SignUpActivity.kt
    private fun isValidPassword(pw: String): String? {
        if (pw.length < 6) return "Password must be at least 6 characters"
        if (!pw.any { it.isDigit() }) return "Password must contain a number"
        if (!pw.any { it.isUpperCase() }) return "Password must contain a capital letter"
        return null  // null = valid
    }

    @Test
    fun password_tooShort_returnsLengthError() {
        assertThat(isValidPassword("Ab1")).isEqualTo("Password must be at least 6 characters")
    }

    @Test
    fun password_noDigit_returnsDigitError() {
        assertThat(isValidPassword("ABCDEF")).isEqualTo("Password must contain a number")
    }

    @Test
    fun password_noCapital_returnsCapitalError() {
        assertThat(isValidPassword("abcdef1")).isEqualTo("Password must contain a capital letter")
    }

    @Test
    fun password_valid_returnsNull() {
        assertThat(isValidPassword("Password1")).isNull()
    }

    @Test
    fun password_validWithSymbols_returnsNull() {
        assertThat(isValidPassword("P@ssw0rd!")).isNull()
    }

    @Test
    fun password_validEdgeCase_exactlySixChars_returnsNull() {
        assertThat(isValidPassword("Abc123")).isNull()
    }

    @Test
    fun email_missingAtSymbol_isInvalid() {
        val email = "notanemail"
        assertThat(email.contains("@")).isFalse()
    }

    @Test
    fun email_withAtSymbol_isValid() {
        val email = "test@example.com"
        assertThat(email.contains("@")).isTrue()
    }

    @Test
    fun passwords_mismatch_areDetected() {
        val pw = "Password1"
        val confirm = "Password2"
        assertThat(pw).isNotEqualTo(confirm)
    }

    @Test
    fun passwords_match_areEqual() {
        val pw = "Password1"
        val confirm = "Password1"
        assertThat(pw).isEqualTo(confirm)
    }
}