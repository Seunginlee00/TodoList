package com.my.boot.auth.security.socket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DuplicateLoginAlertSender {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendDuplicateLoginAlert(String userId) {
        messagingTemplate.convertAndSend("/topic/login/" + userId,
                Map.of("type", "DUPLICATE_LOGIN", "message", "다른 곳에서 로그인 되었습니다."));
    }
}

