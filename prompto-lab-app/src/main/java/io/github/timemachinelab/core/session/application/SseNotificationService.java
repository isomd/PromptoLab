package io.github.timemachinelab.core.session.application;

import io.github.timemachinelab.core.qatree.QaTree;
import io.github.timemachinelab.core.qatree.QaTreeDomain;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.ai.QuestionGenerationOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE通知服务
 * 负责管理SSE连接和发送消息给客户端
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Service
@Slf4j
public class SseNotificationService {
    
    @Resource
    private SessionManagementService sessionManagementService;
    @Resource
    private QaTreeDomain qaTreeDomain;
    
    // SSE连接管理
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    
    /**
     * 注册SSE连接
     * 
     * @param sessionId 会话ID
     * @param emitter SSE发射器
     */
    public void registerSseConnection(String sessionId, SseEmitter emitter) {
        sseEmitters.put(sessionId, emitter);
        log.info("SSE连接已注册 - 会话: {}", sessionId);
    }
    
    /**
     * 移除SSE连接
     * 
     * @param sessionId 会话ID
     */
    public void removeSseConnection(String sessionId) {
        sseEmitters.remove(sessionId);
        log.info("SSE连接已移除 - 会话: {}", sessionId);
    }
    
    /**
     * 发送SSE消息给客户端
     * 
     * @param sessionId 会话ID
     * @param response 消息响应对象
     */
    public void sendSseMessage(String sessionId, QuestionGenerationOperation.QuestionGenerationResponse response) {
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
     * 
     * @return 连接状态信息
     */
    public Map<String, Object> getSseStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("connectedSessions", sseEmitters.keySet());
        status.put("totalConnections", sseEmitters.size());
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }
    
    /**
     * 发送欢迎消息
     * 
     * @param sessionId 会话ID
     * @param message 欢迎消息内容
     */
    public void sendWelcomeMessage(String sessionId, String message) {
        SseEmitter emitter = sseEmitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(message));
                log.info("欢迎消息发送成功 - 会话: {}", sessionId);
            } catch (IOException e) {
                log.error("欢迎消息发送失败 - 会话: {}, 错误: {}", sessionId, e.getMessage());
                sseEmitters.remove(sessionId);
            }
        }
    }
    
    /**
     * 发送连接数据
     * 
     * @param sessionId 会话ID
     * @param connectionData 连接数据
     */
    public void sendWelcomeMessage(String sessionId, Map<String, Object> connectionData) {
        SseEmitter emitter = sseEmitters.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(connectionData));
                log.info("连接数据发送成功 - 会话: {}", sessionId);
            } catch (IOException e) {
                log.error("连接数据发送失败 - 会话: {}, 错误: {}", sessionId, e.getMessage());
                sseEmitters.remove(sessionId);
            }
        }
    }
}