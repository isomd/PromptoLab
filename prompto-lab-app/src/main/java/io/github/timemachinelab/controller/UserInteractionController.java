package io.github.timemachinelab.controller;

import com.alibaba.fastjson.JSONObject;
import io.github.timemachinelab.core.fingerprint.FingerprintService;
import io.github.timemachinelab.core.fingerprint.UserFingerprint;
import io.github.timemachinelab.core.session.application.ConversationService;
import io.github.timemachinelab.core.session.application.MessageProcessingService;
import io.github.timemachinelab.core.session.application.SessionManagementService;
import io.github.timemachinelab.core.session.application.SseNotificationService;
import io.github.timemachinelab.core.session.application.SseValidationService;
import io.github.timemachinelab.core.session.application.SessionException;
import io.github.timemachinelab.core.session.application.SseValidationService;
import io.github.timemachinelab.core.session.application.SessionProcessingService;
import io.github.timemachinelab.core.session.application.RetryProcessingService;

import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.web.dto.GenPromptRequest;
import io.github.timemachinelab.core.session.infrastructure.web.dto.UnifiedAnswerRequest;
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
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户交互控制器
 * 提供用户交互相关的API接口
 *
 * @author welsir
 * @date 2025/1/20
 */
@Slf4j
@RestController
@RequestMapping("/api/user-interaction")
@Validated
public class UserInteractionController {
    @Resource
    private MessageProcessingService messageProcessingService;
    @Resource
    private SessionManagementService sessionManagementService;
    @Resource
    private SseNotificationService sseNotificationService;
    @Resource
    private FingerprintService fingerprintService;
    @Resource
    private ConversationService conversationService;
    @Resource
    private SseValidationService sseValidationService;
    @Resource
    private SessionProcessingService sessionProcessingService;
    @Resource
    private RetryProcessingService retryProcessingService;

    /**
     * 建立SSE连接
     * 1. 生成指纹(如果不存在)，返回空的sessionList
     * 2. 如果生成的指纹已经存在，获取对应的sessionList返回
     *
     * @param request HTTP请求对象
     * @return SSE连接
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamConversation(HttpServletRequest request) {
        log.info("建立SSE连接 - 来源IP: {}", request.getRemoteAddr());

        try {
            // 1. 自动生成或获取用户指纹
            UserFingerprint userFingerprint = fingerprintService.getOrCreateUserFingerprint(request);
            String fingerprint = userFingerprint.getFingerprint();

            log.info("用户指纹: {}, 是否新用户: {}, 访问次数: {}",
                    fingerprint,
                    userFingerprint.getVisitCount() == 1,
                    userFingerprint.getVisitCount());

            // 2. 使用指纹作为用户ID

            // 3. 获取用户的会话ID列表
            List<String> sessionList = sessionManagementService.getUserSessionIds(fingerprint);
            log.info("用户指纹: {} 对应的会话ID列表: {}", fingerprint, sessionList);

            // 4. 创建SSE连接（使用指纹作为连接标识）
            SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
            sseNotificationService.registerSseConnection(fingerprint, emitter);

            // 5. 连接建立时发送用户信息和sessionList
            Map<String, Object> connectionData = new ConcurrentHashMap<>();
            connectionData.put("fingerprintId", fingerprint);
            connectionData.put("sessionList", sessionList); // 空集合或现有会话列表
            connectionData.put("isNewUser", userFingerprint.getVisitCount() == 1);
            connectionData.put("displayName", userFingerprint.getDisplayName());
            connectionData.put("visitCount", userFingerprint.getVisitCount());
            connectionData.put("timestamp", System.currentTimeMillis());

            // 将Map转换为JSONObject以生成标准JSON格式
            JSONObject jsonData = new JSONObject(connectionData);
            sseNotificationService.sendSuccessMessage(fingerprint, jsonData.toJSONString());

            // 设置连接事件处理
            emitter.onCompletion(() -> {
                log.info("SSE连接完成: {}", fingerprint);
            });

            emitter.onTimeout(() -> {
                log.info("SSE连接超时: {}", fingerprint);
                sseNotificationService.removeSseConnection(fingerprint);
            });

            emitter.onError((ex) -> {
                log.error("SSE连接错误: {} - {}", fingerprint, ex.getMessage());
                sseNotificationService.removeSseConnection(fingerprint);
            });

            return emitter;

        } catch (Exception e) {
            log.error("建立SSE连接失败: {}", e.getMessage());
            SseEmitter errorEmitter = new SseEmitter(Long.MAX_VALUE);
            try {
                errorEmitter.send(SseEmitter.event()
                        .name("error")
                        .data("连接建立失败: " + e.getMessage()));
            } catch (IOException ioException) {
                log.error("发送错误消息失败: {}", ioException.getMessage());
            }
            return errorEmitter;
        }
    }

    /**
     * 重试接口
     *
     * @param request     重试请求参数
     * @param httpRequest HTTP请求对象
     * @return 重试结果
     */
    @PostMapping("/retry")
    public ResponseEntity<ApiResult<RetryResponse>> retry(@Valid @RequestBody RetryRequest request, HttpServletRequest httpRequest) 
            throws SessionException {
        log.info("收到重试请求 - nodeId: {}, sessionId: {}, whyretry: {}",
                request.getNodeId(), request.getSessionId(), request.getWhyretry());

        // 1. 验证SSE连接并获取用户指纹
        String fingerprint = sseValidationService.validateAndGetFingerprint(httpRequest);
        
        // 2. 处理重试请求（验证和处理逻辑）
        RetryProcessingService.RetryProcessingResult retryResult = 
            retryProcessingService.processRetryRequest(fingerprint, request);
        
        // 3. 发送处理后的消息给AI服务
        messageProcessingService.processAndSendMessage(
            retryResult.getSession(), 
            retryResult.getProcessedMessage()
        );
        
        // 4. 构建响应数据
        RetryResponse response = retryProcessingService.buildRetryResponse(request);
        
        log.info("重试请求处理成功 - nodeId: {}, sessionId: {}",
                request.getNodeId(), request.getSessionId());

        return ResponseEntity.ok(ApiResult.success("重试请求处理成功", response));
    }

    /**
     * 处理统一答案请求（基于用户指纹）
     * 支持单选、多选、输入框、表单等多种问题类型的回答
     * 逻辑: 如果没有带sessionId，默认就是新建对话，不需要传入NodeId
     * 如果带了sessionId就不是新建会话
     *
     * @param request     统一答案请求
     * @param httpRequest HTTP请求对象
     * @return 处理结果
     */
    @PostMapping("/message")
    public ResponseEntity<String> processAnswer(@Validated @RequestBody UnifiedAnswerRequest request,
                                                HttpServletRequest httpRequest) throws SessionException {
        log.info("接收到答案请求 - 会话ID: {}, 问题类型: {}",
                request.getSessionId(),
                request.getQuestionType());

        // 1. 验证SSE连接并获取用户指纹
        String fingerprint = sseValidationService.validateAndGetFingerprint(httpRequest);

        // 2. 处理会话请求（新建或继续会话）
        SessionProcessingService.SessionProcessingResult sessionResult = 
            sessionProcessingService.processSessionRequest(fingerprint, request);
        
        ConversationSession session = sessionResult.getSession();

        // 3. 验证答案格式
        if (!messageProcessingService.validateAnswer(request)) {
            log.warn("答案格式验证失败: {}", request);
            return ResponseEntity.badRequest().body("答案格式不正确");
        }

        // 6. 处理答案并转换为消息
        String processedMessage = messageProcessingService.preprocessMessage(
                null,
                request,
                session
        );

        // 7. 发送处理后的消息给AI服务
        messageProcessingService.processAndSendMessage(session, processedMessage);

        return ResponseEntity.ok("答案处理成功");
    }

    @PostMapping("/gen-prompt")
    public ResponseEntity<String> genPrompt(@RequestBody GenPromptRequest request, HttpServletRequest httpRequest) throws SessionException {
        // 1. 验证SSE连接并获取用户指纹
        String fingerprint = sseValidationService.validateAndGetFingerprint(httpRequest);
        log.info("处理genPrompt请求 - 指纹: {}, sessionId: {}", fingerprint, request.getSessionId());

        // 2. 获取或创建会话
        ConversationSession session = sessionManagementService.getOrCreateSession(fingerprint, request.getSessionId());
        if (session == null) {
            log.error("会话创建或获取失败 - 指纹: {}, sessionId: {}", fingerprint, request.getSessionId());
            sseNotificationService.sendErrorMessage(fingerprint, "会话创建失败，请重试");
            return ResponseEntity.internalServerError().body("会话处理失败");
        }

        // 4. 调用AI服务生成提示词
        conversationService.genPrompt(session.getSessionId(), response -> {
            try {

                // 5. 更新currentNode - 在AI回答后创建新节点
                String newNodeId = session.getNextNodeId();
                session.setCurrentNode(newNodeId);
                session.setUpdateTime(LocalDateTime.now());

                // 发送AI生成的提示词
                sseNotificationService.sendSuccessMessage(fingerprint, response.getGenPrompt());

                log.info("genPrompt处理完成 - 会话: {}, 新节点: {}", session.getSessionId(), newNodeId);
            } catch (Exception e) {
                log.error("SSE消息发送失败: {}", e.getMessage(), e);
                // 确保SSE有响应，即使是错误消息
                sseNotificationService.sendErrorMessage(fingerprint, "提示词生成完成，但响应发送失败");
            }
        });

        return ResponseEntity.ok("提示词生成请求已处理");
    }


    /**
     * 获取SSE连接状态
     */
    @GetMapping("/sse-status")
    public ResponseEntity<Map<String, Object>> getSseStatus() {
        return ResponseEntity.ok(sseNotificationService.getSseStatus());
    }
}