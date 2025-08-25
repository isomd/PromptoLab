package io.github.timemachinelab.controller;

import com.alibaba.fastjson2.JSON;
import io.github.timemachinelab.core.constant.AllPrompt;
import io.github.timemachinelab.core.fingerprint.FingerprintService;
import io.github.timemachinelab.core.fingerprint.UserFingerprint;
import io.github.timemachinelab.core.session.application.MessageProcessingService;
import io.github.timemachinelab.core.session.application.SessionManagementService;
import io.github.timemachinelab.core.session.application.SseNotificationService;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.ai.GenPromptOperation;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private MessageProcessingService messageProcessingService;
    @Resource
    private SessionManagementService sessionManagementService;
    @Resource
    private SseNotificationService sseNotificationService;
    @Resource
    private FingerprintService fingerprintService;

    /**
     * 建立SSE连接
     * 1. 生成指纹(如果不存在)，返回空的sessionList
     * 2. 如果生成的指纹已经存在，获取对应的sessionList返回
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

            sseNotificationService.sendWelcomeMessage(fingerprint, connectionData);

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
     * @param request 重试请求参数
     * @return 重试结果
     */
    @PostMapping("/retry")
    public ResponseEntity<ApiResult<RetryResponse>> retry(@Valid @RequestBody RetryRequest request) {
        try {
            log.info("收到重试请求 - nodeId: {}, sessionId: {}, whyretry: {}",
                    request.getNodeId(), request.getSessionId(), request.getWhyretry());

            // 使用应用服务验证节点存在性
            //todo: 有可能水平越权 不传userId的话
            if (!sessionManagementService.validateNodeExists(request.getSessionId(), request.getNodeId())) {
                log.warn("节点不存在 - nodeId: {}, sessionId: {}", request.getNodeId(), request.getSessionId());
                return ResponseEntity.badRequest().body(ApiResult.error("指定的节点不存在"));
            }

            // 使用应用服务获取问题内容
            String question = sessionManagementService.getNodeQuestion(request.getSessionId(), request.getNodeId());
            if (question == null) {
                log.warn("节点问题内容为空 - nodeId: {}, sessionId: {}", request.getNodeId(), request.getSessionId());
                return ResponseEntity.badRequest().body(ApiResult.error("节点问题内容为空"));
            }

            // 获取会话对象
            ConversationSession session = sessionManagementService.getSessionById(request.getSessionId());
            if (session == null) {
                log.warn("会话不存在 - sessionId: {}", request.getSessionId());
                return ResponseEntity.badRequest().body(ApiResult.error("会话不存在"));
            }

            // 移除要重试的节点（AI会基于parentId重新创建节点）
            boolean nodeRemoved = sessionManagementService.removeNode(request.getSessionId(), request.getNodeId());
            if (!nodeRemoved) {
                log.warn("移除节点失败，但继续处理重试 - sessionId: {}, nodeId: {}",
                        request.getSessionId(), request.getNodeId());
            }

            // 使用MessageProcessingService处理重试消息
            String processedMessage = messageProcessingService.processRetryMessage(
                    request.getSessionId(),
                    request.getNodeId(),
                    request.getWhyretry(),
                    session
            );

            // 发送处理后的消息给AI服务
            messageProcessingService.processAndSendMessage(session, processedMessage);

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
     * 处理统一答案请求（基于用户指纹）
     * 支持单选、多选、输入框、表单等多种问题类型的回答
     * 逻辑: 如果没有带sessionId，默认就是新建对话，不需要传入NodeId
     *       如果带了sessionId就不是新建会话，需要nodeId
     *
     * @param request 统一答案请求
     * @param httpRequest HTTP请求对象
     * @return 处理结果
     */
    @PostMapping("/message")
    public ResponseEntity<String> processAnswer(@Validated @RequestBody UnifiedAnswerRequest request,
                                               HttpServletRequest httpRequest) {
        try {
            log.info("接收到答案请求 - 会话ID: {}, 节点ID: {}, 问题类型: {}",
                    request.getSessionId(),
                    request.getNodeId(),
                    request.getQuestionType());

            // 1. 从请求头获取和验证用户指纹（与SSE连接保持一致）
            UserFingerprint userFingerprint = fingerprintService.getOrCreateUserFingerprint(httpRequest);
            String fingerprint = userFingerprint.getFingerprint();
            
            log.info("用户指纹: {}, 访问次数: {}", fingerprint, userFingerprint.getVisitCount());
            
            // 2. 验证请求体中的userId是否与指纹匹配（可选验证）
            String requestUserId = request.getUserId();
            if (requestUserId != null && !requestUserId.equals(fingerprint)) {
                log.warn("请求体中的userId({})与请求头指纹({})不匹配，以请求头指纹为准", requestUserId, fingerprint);
            }

            // 4. 根据sessionId是否为空判断新建还是继续会话
            String sessionId = request.getSessionId();
            boolean isNewConversation = (sessionId == null || sessionId.trim().isEmpty());

            ConversationSession session;

            if (isNewConversation) {
                // 新建对话，不需要nodeId
                log.info("新建对话 - 用户指纹: {}", fingerprint);

                // 创建新会话
                session = sessionManagementService.createNewSession(fingerprint);
                sessionId = session.getSessionId();

                log.info("已创建新会话 - 用户指纹: {}, 会话ID: {}", fingerprint, sessionId);

                // 新对话不需要验证nodeId，直接跳过验证
            } else {
                // 继续现有对话，需要验证sessionId和nodeId
                log.info("继续现有对话 - 用户指纹: {}, 会话ID: {}", fingerprint, sessionId);

                // 验证会话是否存在
                session = sessionManagementService.validateAndGetSession(fingerprint, sessionId);
                if (session == null) {
                    log.warn("会话不存在或无效 - 用户指纹: {}, 会话ID: {}", fingerprint, sessionId);
                    return ResponseEntity.badRequest().body("会话不存在或无效");
                }

                // 验证nodeId
                String nodeId = request.getNodeId();
                if (nodeId == null || nodeId.trim().isEmpty()) {
                    log.warn("继续会话必须提供nodeId - 会话ID: {}", sessionId);
                    return ResponseEntity.badRequest().body("继续会话必须提供nodeId");
                }

                // 验证nodeId是否属于该会话
                if (!sessionManagementService.validateNodeId(sessionId, nodeId)) {
                    log.warn("无效的节点ID - 会话: {}, 节点: {}", sessionId, nodeId);
                    return ResponseEntity.badRequest().body("无效的节点ID");
                }

                log.info("验证通过 - 会话: {}, 节点: {}", sessionId, nodeId);
            }

            // 5. 验证答案格式
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

        } catch (Exception e) {
            log.error("处理答案失败 - 会话ID: {}, 错误: {}",
                    (request.getSessionId() != null ? request.getSessionId() : "新建会话"),
                    e.getMessage(), e);
            return ResponseEntity.internalServerError().body("答案处理失败: " + e.getMessage());
        }
    }

    @PostMapping("/gen-prompt")
    public ResponseEntity<String> genPrompt(@RequestBody GenPromptRequest request) {
        GenPromptOperation.GpResponse gpResponse = new GenPromptOperation.GpResponse();
        gpResponse.setGenPrompt(AllPrompt.GEN_PROMPT_AGENT_PROMPT);
        sseNotificationService.sendWelcomeMessage(request.getSessionId(), JSON.toJSONString(gpResponse));
        return ResponseEntity.ok("生成提示词");
    }

    
    /**
     * 获取SSE连接状态
     */
    @GetMapping("/sse-status")
    public ResponseEntity<Map<String, Object>> getSseStatus() {
        return ResponseEntity.ok(sseNotificationService.getSseStatus());
    }
}