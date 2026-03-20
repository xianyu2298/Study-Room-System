package edu.jjxy.studyroom.backend.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 推送服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 推送消息给指定用户
     */
    public void pushToUser(Long userId, String destination, Object payload) {
        try {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId),
                    destination,
                    payload
            );
            log.debug("WebSocket 推送 - userId: {}, destination: {}", userId, destination);
        } catch (Exception e) {
            log.warn("WebSocket 推送失败 - userId: {}, error: {}", userId, e.getMessage());
        }
    }

    /**
     * 推送座位状态变更给订阅者
     */
    public void pushSeatStatusUpdate(Long userId, Long seatId, Integer status) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "seat_update");
        payload.put("seatId", seatId);
        payload.put("status", status);
        pushToUser(userId, "/queue/seat", payload);
    }

    /**
     * 推送预约状态变更
     */
    public void pushReserveStatusUpdate(Long userId, Long reserveId, Integer status, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "reserve_update");
        payload.put("reserveId", reserveId);
        payload.put("status", status);
        payload.put("message", message);
        pushToUser(userId, "/queue/reserve", payload);
    }

    /**
     * 推送预约提醒
     */
    public void pushReserveReminder(Long userId, Long reserveId, String roomName, String seatNo, Integer minutesBefore) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "reserve_reminder");
        payload.put("reserveId", reserveId);
        payload.put("roomName", roomName);
        payload.put("seatNo", seatNo);
        payload.put("minutesBefore", minutesBefore);
        payload.put("message", String.format("您的预约将在 %d 分钟后开始，请及时签到", minutesBefore));
        pushToUser(userId, "/queue/remind", payload);
    }
}
