package com.lukasz.witkowski.training.planner.backend.routing

import com.lukasz.witkowski.training.planner.dto.common.ApiRoutes
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import java.util.Collections

private val connections: MutableSet<String> = Collections.synchronizedSet(LinkedHashSet())

fun Route.chatWsRoutes() {
    webSocket(ApiRoutes.WebSockets.CHAT) {
        val sessionKey = this.hashCode().toString()
        connections.add(sessionKey)
        try {
            send(Frame.Text("Connected to Training Planner Chat WebSocket server."))
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val receivedText = frame.readText()
                    send(Frame.Text("Echo: $receivedText"))
                }
            }
        } finally {
            connections.remove(sessionKey)
        }
    }
}
