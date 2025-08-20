package io.github.timemachinelab.core.session.infrastructure.web;

import io.github.timemachinelab.core.session.application.ConversationService;
import io.github.timemachinelab.core.session.application.MessageProcessingService;
import io.github.timemachinelab.core.session.application.SessionManagementService;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.web.dto.MessageRequest;
import io.github.timemachinelab.core.session.infrastructure.web.dto.UnifiedAnswerRequest;
import io.github.timemachinelab.core.session.infrastructure.web.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {
    
    private final ConversationService conversationService;
    private final MessageProcessingService messageProcessingService;
    private final SessionManagementService sessionManagementService;
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    
    @GetMapping(value = "/sse/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamConversation(@PathVariable String sessionId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitters.put(sessionId, emitter);
        
        emitter.onCompletion(() -> sseEmitters.remove(sessionId));
        emitter.onTimeout(() -> sseEmitters.remove(sessionId));
        emitter.onError((ex) -> sseEmitters.remove(sessionId));
        
        return emitter;
    }
    

    
    /**
     * 从MessageRequest中提取用户ID
     */
    private String extractUserIdFromMessageRequest(MessageRequest request) {
        // 临时实现：使用会话ID作为用户ID（仅用于演示）
        return request.getSessionId() != null ? "user_" + request.getSessionId().hashCode() : "anonymous_" + System.currentTimeMillis();
    }
    
    /**
     * 从请求中提取用户ID
     * TODO: 根据您的认证机制实现此方法
     */
    private String extractUserIdFromRequest(UnifiedAnswerRequest request) {
        // 这里需要根据您的认证机制来实现
        // 可能从JWT token、session、或其他方式获取用户ID
        
        // 临时实现：使用会话ID作为用户ID（仅用于演示）
        return request.getSessionId() != null ? "user_" + request.getSessionId().hashCode() : "anonymous_" + System.currentTimeMillis();
    }

    private void sendSseMessage(String sessionId, MessageResponse response) {
        SseEmitter emitter = sseEmitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(response));
            } catch (IOException e) {
                sseEmitters.remove(sessionId);
            }
        }
    }
}