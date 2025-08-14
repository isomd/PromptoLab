package io.github.timemachinelab.core.session.infrastructure.web;

import io.github.timemachinelab.core.session.application.ConversationService;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.web.dto.MessageRequest;

import io.github.timemachinelab.core.session.infrastructure.web.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
@RequiredArgsConstructor
public class ConversationController {
    
    private final ConversationService conversationService;
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    
    @PostMapping("/start")
    public ConversationSession startConversation(@RequestParam String userId) {
        return conversationService.createSession(userId);
    }
    
    @GetMapping(value = "/sse/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamConversation(@PathVariable String sessionId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitters.put(sessionId, emitter);
        
        emitter.onCompletion(() -> sseEmitters.remove(sessionId));
        emitter.onTimeout(() -> sseEmitters.remove(sessionId));
        emitter.onError((ex) -> sseEmitters.remove(sessionId));
        
        return emitter;
    }
    
    @PostMapping("/message")
    public void addMessage(@RequestBody MessageRequest request) {
        // 核心业务流程：用户消息 -> AI服务 -> SSE流式返回
        conversationService.processUserMessage(
                request.getSessionId(),
                request.getContent(),
                response -> sendSseMessage(request.getSessionId(), response)
        );
//        switch (request.getType()) {
//            case USER_TEXT:
//
//
//            case USER_SELECTION:
//                conversationService.handleUserSelection(request.getSessionId(), request.getQuestionId(), request.getSelectedOption());
//                sendSseMessage(request.getSessionId(), MessageResponse.userAnswer(request.getQuestionId(), "选择了选项: " + request.getSelectedOption()));
//                break;
//        }
    }
    
    @GetMapping("/session/{sessionId}")
    public ConversationSession getSession(@PathVariable String sessionId) {
        return conversationService.getSession(sessionId);
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