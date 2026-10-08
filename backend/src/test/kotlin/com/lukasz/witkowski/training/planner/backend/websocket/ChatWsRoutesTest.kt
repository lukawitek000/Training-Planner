package com.lukasz.witkowski.training.planner.backend.websocket

import com.lukasz.witkowski.training.planner.backend.module
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatWsRoutesTest {
    @Test
    fun `websocket chat endpoint connects and echos messages`() =
        testApplication {
            application { module() }

            val client =
                createClient {
                    install(WebSockets)
                }

            client.webSocket("/ws/chat") {
                // Receive welcome frame
                val welcomeFrame = incoming.receive() as Frame.Text
                assertTrue(welcomeFrame.readText().contains("Connected"))

                // Send test frame
                send(Frame.Text("Hello Server"))

                // Receive echo frame
                val echoFrame = incoming.receive() as Frame.Text
                assertEquals("Echo: Hello Server", echoFrame.readText())
            }
        }
}
