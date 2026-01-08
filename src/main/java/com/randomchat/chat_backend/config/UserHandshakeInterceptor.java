package com.randomchat.chat_backend.config;

import com.randomchat.chat_backend.security.AuthUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class UserHandshakeInterceptor implements HandshakeInterceptor {

    private final AuthUtil authUtil;

    public UserHandshakeInterceptor(AuthUtil authUtil) {
        this.authUtil = authUtil;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        String token = null;
        String query = request.getURI().getQuery(); // "token=..."
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    token = param.substring("token=".length());
                    break;
                }
            }
        }

        if (token != null && !token.isEmpty()) {
            try {
                String userId = authUtil.getUsernameFromToken(token);
                if (userId != null && !userId.isEmpty()) {
                    attributes.put("userId", userId); // stored in WebSocket session
                    return true;
                }
            } catch (Exception e) {
                // Token invalid
                return false;
            }
        }
        return false; // Reject handshake if no valid token found
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // nothing needed
    }
}
