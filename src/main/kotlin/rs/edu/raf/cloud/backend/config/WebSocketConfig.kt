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
        // All the STOMP SEND requests made to the destination with prefix /topic will be fanned out to all the listeners by the broker
        config.enableSimpleBroker("/topic")

        // All the send requests made to the /app destination will be taken in by our backend
        // We do not have client -> server communication in our project, though this is common practice
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // The endpoint to which the subscribers will send an http request to upgrade their connection to a socket
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*") // Allow any origin to connect
    }
}