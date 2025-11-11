package github.oldLab.oldLab.configuration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import github.oldLab.oldLab.dto.handler.ChatWebSocketProxyHandler;
import lombok.RequiredArgsConstructor;

/**
 * WebSocket configuration for chat proxy.
 * Registers WebSocket endpoint for secure chat communication.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketProxyConfig implements WebSocketConfigurer {

    private final ChatWebSocketProxyHandler chatWebSocketProxyHandler;

    // @Value("${cors.allowed-origins}")
    // private List<String> allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketProxyHandler, "/ws/chat")
                .setAllowedOrigins("localhost:5173", "localhost:8081", "localhost:8082", "localhost:8083");
    }
}
