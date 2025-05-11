package com.example.test_driver2

import okhttp3.*
import org.json.JSONObject

class SupabaseRealtimeClient(projectUrl: String, anonKey: String) {
    private val client = OkHttpClient()
    private lateinit var webSocket: WebSocket
    private var isJoined = false

    private val request = Request.Builder()
        .url("wss://$projectUrl/realtime/v1/websocket?apikey=$anonKey&vsn=1.0.0")
        .build()

    fun connect() {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                val joinMsg = JSONObject().apply {
                    put("topic", "broadcastChannel")
                    put("event", "phx_join")
                    put("payload", JSONObject().apply {
                        put("config", JSONObject().apply {
                            put("broadcast", JSONObject().apply {
                                put("self", true)
                                put("ack", true)
                            })
                        })
                    })
                    put("ref", "1")
                }
                ws.send(joinMsg.toString())
                isJoined = true
            }

            override fun onMessage(ws: WebSocket, text: String) {
                println("📩 Realtime Message: $text")
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                println("❌ WebSocket error: ${t.message}")
            }
        })
    }

    fun sendPositionBroadcast(data: JSONObject) {
        if (!isJoined) return

        val msg = JSONObject().apply {
            put("topic", "broadcastChannel")
            put("event", "broadcast")
            put("payload", JSONObject().apply {
                put("event", "pos")
                put("payload", data)
            })
            put("ref", "2")
        }

        webSocket.send(msg.toString())
    }

    fun disconnect() {
        webSocket.close(1000, "Closed")
    }
}
