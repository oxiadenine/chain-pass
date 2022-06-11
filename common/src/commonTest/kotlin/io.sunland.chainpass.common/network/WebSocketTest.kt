package io.sunland.chainpass.common.network

import io.ktor.websocket.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class SocketMessageTest {
    private val validData = "test"
    private val invalidData = "@test@"
    private val errorMessage = "test"

    @Test
    fun testFrom() {
        SocketMessage.from(Frame.Text("$validData@true")).data.onSuccess { data -> assertEquals(validData, data) }
        SocketMessage.from(Frame.Text("$errorMessage@false")).data.onFailure { exception ->
            assertEquals(errorMessage, exception.message)
        }

        assertFails { SocketMessage.from(Frame.Text("${invalidData}@true")) }
        assertFails { SocketMessage.from(Frame.Text("${invalidData}@false")) }
    }

    @Test
    fun testToFrame() {
        assertEquals(
            Frame.Text("$validData@true").readText(),
            SocketMessage.success(validData).toFrame().readText()
        )
        assertEquals(
            Frame.Text("$errorMessage@false").readText(),
            SocketMessage.failure(errorMessage).toFrame().readText()
        )
    }
}