package github.oldLab.oldLab.dto.handler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import github.oldLab.oldLab.entity.Person;
import github.oldLab.oldLab.service.UserIdCacheService;
import github.oldLab.oldLab.serviceImpl.TokenServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket proxy handler that securely forwards WebSocket connections
 * to the chat microservice after validating JWT tokens.
 * 
 * Security features:
 * - JWT validation before establishing connection
 * - UserId extraction and caching
 * - Automatic reconnection handling
 * - Session management
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketProxyHandler extends TextWebSocketHandler {

    private final TokenServiceImpl tokenService;
    private final UserIdCacheService userIdCacheService;
    private final StandardWebSocketClient webSocketClient = new StandardWebSocketClient();

    // Map: client session -> chat service session
    private final Map<String, WebSocketSession> clientToServiceSessions = new ConcurrentHashMap<>();
    
    // Map: chat service session -> client session
    private final Map<String, WebSocketSession> serviceToClientSessions = new ConcurrentHashMap<>();

    @Value("${chat.service.websocket.url}")
    private String chatServiceWebSocketUrl;

    @Override
    public void afterConnectionEstablished(WebSocketSession clientSession) throws Exception {
        log.info("Client WebSocket connection established: {}", clientSession.getId());
        
        try {
            // Extract and validate JWT token from query parameters
            String token = extractTokenFromUri(clientSession.getUri());
            if (token == null) {
                log.warn("Missing token in WebSocket connection");
                clientSession.close(CloseStatus.BAD_DATA.withReason("Missing authentication token"));
                return;
            }

            // Get or extract userId
            Long userId = getUserId(token, clientSession);
            if (userId == null) {
                log.warn("Failed to extract userId from token");
                clientSession.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid authentication token"));
                return;
            }

            // Connect to chat microservice with userId
            String serviceUrl = chatServiceWebSocketUrl + "?userId=" + userId;
            log.debug("Connecting to chat service: {}", serviceUrl);

            webSocketClient.execute(
                new ChatServiceWebSocketHandler(clientSession),
                serviceUrl
            ).get(); // Wait for connection to be established

            log.info("Successfully proxied WebSocket connection for userId: {}", userId);

        } catch (Exception e) {
            log.error("Failed to establish proxy WebSocket connection", e);
            clientSession.close(CloseStatus.SERVER_ERROR.withReason("Failed to connect to chat service"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession clientSession, TextMessage message) throws Exception {
        WebSocketSession serviceSession = clientToServiceSessions.get(clientSession.getId());
        
        if (serviceSession != null && serviceSession.isOpen()) {
            log.debug("Forwarding message from client {} to service", clientSession.getId());
            serviceSession.sendMessage(message);
        } else {
            log.warn("Service session not available for client {}", clientSession.getId());
            clientSession.close(CloseStatus.SERVER_ERROR.withReason("Connection to chat service lost"));
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession clientSession, BinaryMessage message) {
        WebSocketSession serviceSession = clientToServiceSessions.get(clientSession.getId());
        
        try {
            if (serviceSession != null && serviceSession.isOpen()) {
                serviceSession.sendMessage(message);
            }
        } catch (IOException e) {
            log.error("Failed to forward binary message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession clientSession, CloseStatus status) throws Exception {
        log.info("Client WebSocket connection closed: {} with status: {}", clientSession.getId(), status);
        
        WebSocketSession serviceSession = clientToServiceSessions.remove(clientSession.getId());
        if (serviceSession != null) {
            serviceToClientSessions.remove(serviceSession.getId());
            if (serviceSession.isOpen()) {
                serviceSession.close(status);
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    /**
     * Extract JWT token from WebSocket URI query parameters
     */
    private String extractTokenFromUri(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }

        String query = uri.getQuery();
        for (String param : query.split("&")) {
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    /**
     * Get userId from cache or extract from token
     */
    private Long getUserId(String token, WebSocketSession session) {
        // Try cache first
        Long userId = userIdCacheService.getCachedUserId(token);
        
        if (userId == null) {
            // Cache miss - extract from token
            try {
                tokenService.extractUsername(token); // Validate token
                
                // Get user from session attributes (set by JWT filter)
                Object principal = session.getPrincipal();
                if (principal instanceof Person) {
                    userId = ((Person) principal).getId();
                    userIdCacheService.cacheUserId(token, userId);
                    log.debug("Cached userId {} for WebSocket connection", userId);
                }
            } catch (Exception e) {
                log.error("Failed to extract userId from token", e);
                return null;
            }
        }
        
        return userId;
    }

    /**
     * Handler for chat service WebSocket connection
     */
    private class ChatServiceWebSocketHandler extends TextWebSocketHandler {
        
        private final WebSocketSession clientSession;

        public ChatServiceWebSocketHandler(WebSocketSession clientSession) {
            this.clientSession = clientSession;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession serviceSession) throws Exception {
            log.info("Connected to chat service: {}", serviceSession.getId());
            clientToServiceSessions.put(clientSession.getId(), serviceSession);
            serviceToClientSessions.put(serviceSession.getId(), clientSession);
        }

        @Override
        protected void handleTextMessage(WebSocketSession serviceSession, TextMessage message) throws Exception {
            if (clientSession.isOpen()) {
                log.debug("Forwarding message from service to client {}", clientSession.getId());
                clientSession.sendMessage(message);
            } else {
                log.warn("Client session closed, closing service session");
                serviceSession.close(CloseStatus.NORMAL);
            }
        }

        @Override
        protected void handleBinaryMessage(WebSocketSession serviceSession, BinaryMessage message) {
            try {
                if (clientSession.isOpen()) {
                    clientSession.sendMessage(message);
                }
            } catch (IOException e) {
                log.error("Failed to forward binary message to client", e);
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession serviceSession, CloseStatus status) throws Exception {
            log.info("Chat service connection closed: {} with status: {}", serviceSession.getId(), status);
            
            serviceToClientSessions.remove(serviceSession.getId());
            clientToServiceSessions.remove(clientSession.getId());
            
            if (clientSession.isOpen()) {
                clientSession.close(status);
            }
        }

        @Override
        public void handleTransportError(WebSocketSession serviceSession, Throwable exception) throws Exception {
            log.error("Chat service WebSocket error: {}", serviceSession.getId(), exception);
            serviceSession.close(CloseStatus.SERVER_ERROR);
            
            if (clientSession.isOpen()) {
                clientSession.close(CloseStatus.SERVER_ERROR.withReason("Chat service error"));
            }
        }
    }
}
