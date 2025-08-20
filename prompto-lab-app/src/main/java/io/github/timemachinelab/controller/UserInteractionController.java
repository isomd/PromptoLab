package io.github.timemachinelab.controller;

import io.github.timemachinelab.core.session.application.ConversationService;
import io.github.timemachinelab.core.session.application.MessageProcessingService;
import io.github.timemachinelab.core.session.application.SessionManagementService;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.web.dto.UnifiedAnswerRequest;
import io.github.timemachinelab.core.session.infrastructure.web.dto.MessageResponse;
import io.github.timemachinelab.entity.req.RetryRequest;
import io.github.timemachinelab.entity.resp.ApiResult;
import io.github.timemachinelab.entity.resp.RetryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户交互控制器
 * 提供用户交互相关的API接口
 * 
 * @author suifeng
 * @date 2025/1/20
 */
@Slf4j
@RestController
@RequestMapping("/api/user-interaction")
@Validated
public class UserInteractionController {

    @Resource
    private ConversationService conversationService;
    @Resource
    private MessageProcessingService messageProcessingService;
    @Resource
    private SessionManagementService sessionManagementService;
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    
    /**
     * 建立SSE连接
     */
    @GetMapping(value = "/sse/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamConversation(@PathVariable String sessionId) {
        log.info("建立SSE连接 - 会话ID: {}", sessionId);
        
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitters.put(sessionId, emitter);
        
        // 连接建立时发送欢迎消息
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("SSE连接已建立，会话ID: " + sessionId));
        } catch (IOException e) {
            log.error("发送欢迎消息失败: {}", e.getMessage());
        }
        
        // 设置连接事件处理
        emitter.onCompletion(() -> {
            log.info("SSE连接完成: {}", sessionId);
            sseEmitters.remove(sessionId);
        });
        
        emitter.onTimeout(() -> {
            log.info("SSE连接超时: {}", sessionId);
            sseEmitters.remove(sessionId);
        });
        
        emitter.onError((ex) -> {
            log.error("SSE连接错误: {} - {}", sessionId, ex.getMessage());
            sseEmitters.remove(sessionId);
        });
        
        return emitter;
    }

    /**
     * 重试接口
     * 
     * @param request 重试请求参数
     * @return 重试结果
     */
    @PostMapping("/retry")
    public ResponseEntity<ApiResult<RetryResponse>> retry(@Valid @RequestBody RetryRequest request) {
        try {
            log.info("收到重试请求 - nodeId: {}, sessionId: {}, whyretry: {}", 
                    request.getNodeId(), request.getSessionId(), request.getWhyretry());



            // 构建响应数据
            RetryResponse response = RetryResponse.builder()
                    .nodeId(request.getNodeId())
                    .sessionId(request.getSessionId())
                    .whyretry(request.getWhyretry())
                    .processTime(System.currentTimeMillis())
                    .build();
            
            log.info("重试请求处理成功 - nodeId: {}, sessionId: {}", 
                    request.getNodeId(), request.getSessionId());
            
            return ResponseEntity.ok(ApiResult.success("重试请求处理成功", response));
            
        } catch (Exception e) {
            log.error("重试请求处理失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResult.serverError("重试请求处理失败: " + e.getMessage()));
        }
    }

    /**
     * 处理统一答案请求
     * 支持单选、多选、输入框、表单等多种问题类型的回答
     */
    @PostMapping("/message")
    public ResponseEntity<String> processAnswer(@Validated @RequestBody UnifiedAnswerRequest request) {
        try {
            log.info("接收到答案请求 - 会话ID: {}, 节点ID: {}, 问题类型: {}",
                    request.getSessionId(),
                    request.getNodeId(),
                    request.getQuestionType());

            // 1. 会话管理和验证
            String userId = request.getUserId();

            ConversationSession session = sessionManagementService.getOrCreateSession(userId, request.getSessionId());

            // 2. 验证nodeId是否属于该会话
            if (request.getNodeId() != null && !sessionManagementService.validateNodeId(session.getSessionId(), request.getNodeId())) {
                log.warn("无效的节点ID - 会话: {}, 节点: {}", session.getSessionId(), request.getNodeId());
                return ResponseEntity.badRequest().body("无效的节点ID");
            }

            // 3. 验证答案格式
            if (!messageProcessingService.validateAnswer(request)) {
                log.warn("答案格式验证失败: {}", request);
                return ResponseEntity.badRequest().body("答案格式不正确");
            }

            // 4. 处理答案并转换为消息
            String processedMessage = messageProcessingService.preprocessMessage(
                    null, // 没有额外的原始消息
                    request,
                    session
            );

            // 5. 发送处理后的消息给AI服务
            conversationService.processUserMessage(
                    session.getUserId(),
                    processedMessage,
                    response -> sendSseMessage(session.getSessionId(), response)
            );

            return ResponseEntity.ok("答案处理成功");

        } catch (Exception e) {
            log.error("处理答案失败 - 会话ID: {}, 错误: {}", request.getSessionId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().body("答案处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过SSE发送消息给客户端
     * 
     * @param sessionId 会话ID
     * @param response 消息响应对象
     */
    private void sendSseMessage(String sessionId, MessageResponse response) {
        SseEmitter emitter = sseEmitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(response));
                log.debug("SSE消息发送成功 - 会话: {}, 消息类型: {}", sessionId, response.getType());
            } catch (IOException e) {
                log.error("SSE消息发送失败 - 会话: {}, 错误: {}", sessionId, e.getMessage());
                sseEmitters.remove(sessionId);
            }
        } else {
            log.warn("SSE连接不存在 - 会话: {}", sessionId);
        }
    }
    
    /**
     * 获取SSE连接状态
     */
    @GetMapping("/sse-status")
    public ResponseEntity<Map<String, Object>> getSseStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("connectedSessions", sseEmitters.keySet());
        status.put("totalConnections", sseEmitters.size());
        status.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(status);
    }
}