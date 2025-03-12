package cli

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class ArgumentsParserTest {

    @Test
    fun testValidUrl() {
        assertTrue(isValidUrl("http://example.com"))
        assertTrue(isValidUrl("https://example.com/path"))
    }

    @Test
    fun testInvalidUrl() {
        assertFalse(isValidUrl("ftp://example.com"))
        assertFalse(isValidUrl("example.com"))
        assertFalse(isValidUrl(""))
    }

    @Test
    fun testValidHash() {
        val validHash = "a".repeat(64)
        assertTrue(isValidHash(validHash))
    }

    @Test
    fun testInvalidHash() {
        val invalidHash = "g".repeat(64)
        assertFalse(isValidHash(invalidHash))

        val tooShortHash = "a".repeat(63)
        assertFalse(isValidHash(tooShortHash))
    }

    @Test
    fun testParseArgumentsValid() {
        val args = arrayOf("http://example.com", "a".repeat(64))
        val result = parseArguments(args, System.out.writer())
        assertEquals("http://example.com", result?.first)
        assertEquals("a".repeat(64), result?.second)
    }

    @Test
    fun testParseArgumentsInvalidUrl() {
        val args = arrayOf("invalid-url", "a".repeat(64))
        val result = parseArguments(args, System.out.writer())
        assertEquals(null, result)
    }

    @Test
    fun testParseArgumentsInvalidHash() {
        val args = arrayOf("http://example.com", "z".repeat(63))
        val result = parseArguments(args, System.out.writer())
        assertEquals(null, result)
    }
}
