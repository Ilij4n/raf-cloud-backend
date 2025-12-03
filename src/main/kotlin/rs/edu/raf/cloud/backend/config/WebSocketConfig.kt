package rs.edu.raf.cloud.backend.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // Enables a simple in-memory broker
        // Clients will subscribe to "/topic/machines" to receive updates
        config.enableSimpleBroker("/topic")

        // Clients send messages to endpoints starting with "/app" (not needed for this project yet, but standard)
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // The endpoint the frontend connects to.
        // e.g. socket = new WebSocket('ws://localhost:8080/ws');
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // Allow Angular (localhost:4200) to connect
            .withSockJS() // Enable SockJS fallback options
    }
}