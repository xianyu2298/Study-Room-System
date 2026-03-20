package edu.jjxy.studyroom.backend.config;

import edu.jjxy.studyroom.backend.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 认证与连接管理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && accessor.getCommand() != null) {
            switch (accessor.getCommand()) {
                case CONNECT:
                    handleConnect(accessor);
                    break;
                case DISCONNECT:
                    handleDisconnect(accessor);
                    break;
                default:
                    break;
            }
        }
        return message;
    }

    private void handleConnect(StompHeaderAccessor accessor) {
        String token = accessor.getFirstNativeHeader("token");
        if (token == null || token.isEmpty()) {
            log.warn("WebSocket 连接缺少 token");
            throw new org.springframework.messaging.MessagingException("未登录");
        }

        try {
            var claims = jwtUtil.parseToken(token);
            Long userId = Long.valueOf(claims.getSubject());
            String sessionId = accessor.getSessionId();
            sessionUserMap.put(sessionId, userId);
            accessor.setUser(new StompPrincipal(userId));
            log.info("WebSocket 连接成功 - userId: {}, sessionId: {}", userId, sessionId);
        } catch (Exception e) {
            log.warn("WebSocket token 验证失败: {}", e.getMessage());
            throw new org.springframework.messaging.MessagingException("认证失败");
        }
    }

    private void handleDisconnect(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        Long userId = sessionUserMap.remove(sessionId);
        log.info("WebSocket 断开连接 - userId: {}, sessionId: {}", userId, sessionId);
    }

    @EventListener
    public void onApplicationEvent(SessionConnectEvent event) {
        log.debug("Session connected: {}", event.getMessage());
    }

    @EventListener
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        Long userId = sessionUserMap.remove(sessionId);
        log.info("WebSocket session 断开 - userId: {}, sessionId: {}", userId, sessionId);
    }

    private static class StompPrincipal implements java.security.Principal {
        private final Long userId;
        public StompPrincipal(Long userId) { this.userId = userId; }
        @Override public String getName() { return String.valueOf(userId); }
        public Long getUserId() { return userId; }
    }
}
