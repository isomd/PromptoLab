package io.github.timemachinelab.controller;

import io.github.timemachinelab.core.qatree.QaTree;
import io.github.timemachinelab.core.qatree.QaTreeDomain;
import io.github.timemachinelab.core.session.application.ConversationService;
import io.github.timemachinelab.core.session.application.MessageProcessingService;
import io.github.timemachinelab.core.session.application.SessionManagementService;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.ai.QuestionGenerationOperation;
import io.github.timemachinelab.core.session.infrastructure.web.dto.UnifiedAnswerRequest;
import io.github.timemachinelab.core.session.infrastructure.web.dto.MessageResponse;
import io.github.timemachinelab.entity.req.RetryRequest;
import io.github.timemachinelab.entity.resp.ApiResult;
import io.github.timemachinelab.entity.resp.RetryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
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
    private ConversationService conversationService;
    @Resource
    private MessageProcessingService messageProcessingService;
    @Resource
    private SessionManagementService sessionManagementService;
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    @Autowired
    private QaTreeDomain qaTreeDomain;

    /**
     * 建立SSE连接
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamConversation(@RequestParam(required = false) String sessionId, 
                                        @RequestParam String userId) {
        log.info("建立SSE连接 - 会话ID: {}, 用户ID: {}", sessionId, userId);

        boolean isNewSession = false;
        ConversationSession session = null;
        
        try {
            if (sessionId == null || sessionId.isEmpty()) {
                // 新建会话
                session = sessionManagementService.createNewSession(userId);
                sessionId = session.getSessionId();
                isNewSession = true;
                log.info("创建新会话 - 用户ID: {}, 会话ID: {}", userId, sessionId);
            } else {
                // 使用现有会话
                session = sessionManagementService.validateAndGetSession(userId, sessionId);
                if (session == null) {
                    log.warn("会话不存在或无效 - 用户ID: {}, 会话ID: {}", userId, sessionId);
                    // 创建新会话作为fallback
                    session = sessionManagementService.createNewSession(userId != null ? userId : "anonymous_" + UUID.randomUUID().toString().substring(0, 8));
                    sessionId = session.getSessionId();
                    isNewSession = true;
                }
            }
            
            SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
            sseEmitters.put(sessionId, emitter);
            
            // 连接建立时发送会话信息
            Map<String, Object> connectionData = new ConcurrentHashMap<>();
            connectionData.put("sessionId", sessionId);
            connectionData.put("userId", session.getUserId());
            connectionData.put("isNewSession", isNewSession);
            connectionData.put("timestamp", System.currentTimeMillis());
            
            // 根据会话状态返回nodeId
            if (isNewSession) {
                // 新会话返回根节点ID
                connectionData.put("nodeId", "1");
                log.info("新会话返回根节点ID: 1 - 会话: {}", sessionId);
            } else if (session.getQaTree() != null && session.getQaTree().getRoot() != null) {
                // 已存在会话，返回根节点ID（因为qaTree只有根节点）
                String rootNodeId = session.getQaTree().getRoot().getId();
                connectionData.put("nodeId", rootNodeId);
                log.info("已存在会话返回根节点ID: {} - 会话: {}", rootNodeId, sessionId);
                
                // 返回qaTree
                try {
                    String qaTreeJson = io.github.timemachinelab.util.QaTreeSerializeUtil.serialize(session.getQaTree());
                    connectionData.put("qaTree", qaTreeJson);
                } catch (Exception e) {
                    log.error("序列化qaTree失败: {}", e.getMessage());
                }
            } else {
                // 兜底情况，返回根节点ID
                connectionData.put("nodeId", "1");
                log.info("兜底返回根节点ID: 1 - 会话: {}", sessionId);
            }
            
            emitter.send(SseEmitter.event()
                 .name("connected")
                 .data(connectionData));
                 
             // 设置连接事件处理
             String finalSessionId = sessionId;
             emitter.onCompletion(() -> {
                 log.info("SSE连接完成: {}", finalSessionId);
             });

             emitter.onTimeout(() -> {
                 log.info("SSE连接超时: {}", finalSessionId);
                 sseEmitters.remove(finalSessionId);
             });
             
             emitter.onError((ex) -> {
                 log.error("SSE连接错误: {} - {}", finalSessionId, ex.getMessage());
                 sseEmitters.remove(finalSessionId);
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

            // 1. 强制要求sessionId
            if (request.getSessionId() == null || request.getSessionId().trim().isEmpty()) {
                log.warn("缺少必需的sessionId参数");
                return ResponseEntity.badRequest().body("sessionId参数是必需的");
            }

            // 2. 会话管理和验证
            String userId = request.getUserId();
            if (userId == null || userId.trim().isEmpty()) {
                log.warn("缺少必需的userId参数");
                return ResponseEntity.badRequest().body("userId参数是必需的");
            }

            // 3. 验证会话是否存在
            ConversationSession session = sessionManagementService.validateAndGetSession(userId, request.getSessionId());
            if (session == null) {
                log.warn("会话不存在或无效 - 用户ID: {}, 会话ID: {}", userId, request.getSessionId());
                return ResponseEntity.badRequest().body("会话不存在或无效");
            }

            // 4. nodeId验证逻辑
            String nodeId = request.getNodeId();
            if (nodeId == null || nodeId.trim().isEmpty()) {
                // nodeId为空，表示这是新建会话的第一个问题
                if (session.getQaTree() != null && session.getQaTree().getRoot() != null) {
                    log.warn("会话已存在qaTree，但nodeId为空 - 会话: {}", session.getSessionId());
                    return ResponseEntity.badRequest().body("现有会话必须提供nodeId");
                }
                log.info("新建会话的第一个问题 - 会话: {}", session.getSessionId());
            } else if ("1".equals(nodeId)) {
                // nodeId为'1'，表示这是根节点的回答
                if (session.getQaTree() == null || session.getQaTree().getRoot() == null) {
                    log.info("根节点回答，但qaTree未初始化 - 会话: {}", session.getSessionId());
                    // 允许继续处理，后续会创建qaTree
                } else {
                    log.info("根节点回答 - 会话: {}", session.getSessionId());
                }
            } else {
                // nodeId不为空且不是'root'，验证是否属于该会话
                if (!sessionManagementService.validateNodeId(session.getSessionId(), nodeId)) {
                    log.warn("无效的节点ID - 会话: {}, 节点: {}", session.getSessionId(), nodeId);
                    return ResponseEntity.badRequest().body("无效的节点ID");
                }
                log.info("更新现有节点 - 会话: {}, 节点: {}", session.getSessionId(), nodeId);
            }

            // 3. 验证答案格式
            if (!messageProcessingService.validateAnswer(request)) {
                log.warn("答案格式验证失败: {}", request);
                return ResponseEntity.badRequest().body("答案格式不正确");
            }

            QaTree qaTree = session.getQaTree();
            
            // 根据问题类型获取正确的答案数据
            Object answerData;
            switch (request.getQuestionType().toLowerCase()) {
                case "input":
                    answerData = request.getInputAnswer();
                    break;
                case "single":
                case "multi":
                    answerData = request.getChoiceAnswer();
                    break;
                case "form":
                    answerData = request.getFormAnswer();
                    break;
                default:
                    log.warn("未知的问题类型: {}", request.getQuestionType());
                    answerData = request.getAnswerString();
                    break;
            }
            
            qaTreeDomain.updateNodeAnswer(qaTree, request.getNodeId(), answerData);

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
     * 在AI回复时创建QA节点，填入question，answer留空等用户提交后再更新
     * 
     * @param sessionId 会话ID
     * @param response 消息响应对象
     */
    private void sendSseMessage(String sessionId, QuestionGenerationOperation.QuestionGenerationResponse response) {
        SseEmitter emitter = sseEmitters.get(sessionId);
        if (emitter != null) {
            try {
                String currentNodeId = null;
                
                // 1. 先将AI生成的新问题添加到QaTree（只填入question，answer留空）
                ConversationSession session = sessionManagementService.getSessionById(sessionId);
                if (session != null && session.getQaTree() != null && response.getQuestion() != null) {
                    // 使用QaTreeDomain添加新节点，answer字段会自动为空
                    // appendNode方法内部会调用session.getNextNodeId()获取新节点ID
                    QaTree qaTree = qaTreeDomain.appendNode(
                            session.getQaTree(),
                            response.getParentId(),
                            response.getQuestion(),
                            session
                    );
                    
                    // 获取刚刚创建的节点ID（当前计数器的值）
                    currentNodeId = String.valueOf(session.getNodeIdCounter().get());
                    
                    log.info("AI问题已添加到QaTree - 会话: {}, 父节点: {}, 新节点ID: {}, 问题类型: {}",
                            sessionId, response.getParentId(), currentNodeId, response.getQuestion().getType());
                } else {
                    log.warn("无法添加问题到QaTree - 会话: {}, session存在: {}, qaTree存在: {}, question存在: {}", 
                            sessionId, session != null, 
                            session != null && session.getQaTree() != null,
                            response.getQuestion() != null);
                }
                
                // 2. 创建修改后的响应对象，包含currentNodeId和parentNodeId
                Map<String, Object> modifiedResponse = new HashMap<>();
                modifiedResponse.put("question", response.getQuestion());
                modifiedResponse.put("currentNodeId", currentNodeId != null ? currentNodeId : response.getParentId());
                modifiedResponse.put("parentNodeId", response.getParentId());
                
                // 3. 发送SSE消息给前端
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(modifiedResponse));
                log.info("SSE消息发送成功 - 会话: {}, 当前节点ID: {}", sessionId, currentNodeId);
            } catch (IOException e) {
                log.error("SSE消息发送失败 - 会话: {}, 错误: {}", sessionId, e.getMessage());
                sseEmitters.remove(sessionId);
            } catch (Exception e) {
                log.error("添加问题到QaTree失败 - 会话: {}, 错误: {}", sessionId, e.getMessage());
                // 即使QaTree更新失败，仍然发送SSE消息给前端
                try {
                    Map<String, Object> fallbackResponse = new HashMap<>();
                    fallbackResponse.put("question", response.getQuestion());
                    fallbackResponse.put("currentNodeId", response.getParentId()); // 使用parentId作为fallback
                    fallbackResponse.put("parentNodeId", response.getParentId());
                    
                    emitter.send(SseEmitter.event()
                        .name("message")
                        .data(fallbackResponse));
                    log.info("SSE消息发送成功（QaTree更新失败但消息已发送） - 会话: {}", sessionId);
                } catch (IOException ioException) {
                    log.error("SSE消息发送失败 - 会话: {}, 错误: {}", sessionId, ioException.getMessage());
                    sseEmitters.remove(sessionId);
                }
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