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

    // SSE连接管理 - 基于用户指纹的一对一关系
    private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

    /**
     * 注册SSE连接
     *
     * @param fingerprint 用户指纹
     * @param emitter SSE发射器
     */
    public void registerSseConnection(String fingerprint, SseEmitter emitter) {
        // 如果用户已有连接，先移除旧连接
        SseEmitter oldEmitter = sseEmitters.get(fingerprint);
        if (oldEmitter != null) {
            try {
                oldEmitter.complete();
                log.info("关闭旧SSE连接 - 用户指纹: {}", fingerprint);
            } catch (Exception e) {
                log.warn("关闭旧SSE连接失败 - 用户指纹: {}, 错误: {}", fingerprint, e.getMessage());
            }
        }

        sseEmitters.put(fingerprint, emitter);
        log.info("SSE连接已注册 - 用户指纹: {}", fingerprint);
    }

    /**
     * 移除SSE连接
     *
     * @param fingerprint 用户指纹
     */
    public void removeSseConnection(String fingerprint) {
        sseEmitters.remove(fingerprint);
        log.info("SSE连接已移除 - 用户指纹: {}", fingerprint);
    }
    
    /**
     * 发送SSE消息给客户端
     * 
     * @param sessionId 会话ID
     * @param response 消息响应对象
     */
    public void sendSseMessage(String sessionId, QuestionGenerationOperation.QuestionGenerationResponse response) {
        // 通过sessionId找到对应的用户指纹
        ConversationSession session = sessionManagementService.getSessionById(sessionId);
        
        // 验证输入参数
        if (session == null) {
            log.warn("会话不存在 - 会话ID: {}", sessionId);
            return;
        }
        
        if (response == null) {
            log.warn("AI响应对象为空 - 会话: {}", sessionId);
            // 发送错误消息
            sendErrorMessage(session, "AI响应对象为空", "RESPONSE_NULL");
            return;
        }

        String parentId = response.getParentId();
        if (parentId == null || parentId.trim().isEmpty()) {
            log.warn("父节点ID为空 - 会话: {}", sessionId);
            // 发送错误消息
            sendErrorMessage(session, "父节点ID为空", "PARENT_ID_EMPTY");
            return;
        }
        
        // 验证QaTree是否存在
        QaTree qaTree = session.getQaTree();
        if (qaTree == null) {
            log.warn("QaTree不存在 - 会话: {}, 无法验证父节点: {}", sessionId, parentId);
            // 发送错误消息
            sendErrorMessage(session, "QaTree不存在", "QATREE_NOT_FOUND");
            return;
        }
        
        // 验证父节点是否存在
        if (!qaTreeDomain.containsNode(qaTree, parentId)) {
            log.warn("父节点不存在 - 会话: {}, parentId: {}, 当前QaTree节点数量: {}", 
                    sessionId, parentId, qaTree.getNodeCount());
            
            // 记录QaTree的所有节点ID以便调试
            if (qaTree.getNodeCount() > 0) {
                log.info("当前QaTree中的所有节点ID: {}", qaTree.getNodeMap().keySet());
            }
            
            // 发送错误消息
            sendErrorMessage(session, "父节点不存在", "PARENT_NODE_NOT_FOUND");
            return;
        }
        
        // 获取用户指纹和SSE连接
        String fingerprint = session.getUserId(); // userId就是指纹
        SseEmitter emitter = sseEmitters.get(fingerprint);
        if (emitter == null) {
            log.warn("SSE连接不存在 - 会话: {}, 用户指纹: {}, 当前连接数: {}", 
                    sessionId, fingerprint, sseEmitters.size());
            // 可以考虑将消息缓存或记录到数据库，以便用户重连后可以收到
            log.info("考虑将消息缓存以便后续处理 - 问题: {}", 
                    response.getQuestion() != null ? response.getQuestion().getQuestion() : "null");
            // 发送错误消息
            sendErrorMessage(session, "SSE连接不存在", "SSE_CONNECTION_NOT_FOUND");
            return;
        }
        
        try {
            String currentNodeId = null;
            
            log.info("开始处理AI问题 - 会话: {}, 父节点: {}, 问题类型: {}", 
                    sessionId, parentId, response.getQuestion() != null ? response.getQuestion().getType() : "null");

            // 1. 先将AI生成的新问题添加到QaTree（只填入question，answer留空）
            if (response.getQuestion() != null) {
                // 使用QaTreeDomain添加新节点，answer字段会自动为空
                qaTreeDomain.appendNode(qaTree, parentId, response.getQuestion(), session);
                
                // 获取刚刚创建的节点ID（当前计数器的值）
                currentNodeId = String.valueOf(session.getNodeIdCounter().get());
                
                log.info("AI问题已添加到QaTree - 会话: {}, 父节点: {}, 新节点ID: {}, 问题类型: {}",
                        sessionId, parentId, currentNodeId, response.getQuestion().getType());
            } else {
                log.warn("AI响应中问题对象为空 - 会话: {}", sessionId);
            }
            
            // 2. 创建修改后的响应对象，包含currentNodeId和parentNodeId
            Map<String, Object> modifiedResponse = new HashMap<>();
            modifiedResponse.put("question", response.getQuestion());
            modifiedResponse.put("currentNodeId", currentNodeId != null ? currentNodeId : parentId);
            modifiedResponse.put("parentNodeId", parentId);
            
            // 3. 发送SSE消息给前端
            emitter.send(SseEmitter.event()
                .name("message")
                .data(modifiedResponse));
            log.info("SSE消息发送成功 - 会话: {}, 当前节点ID: {}", sessionId, currentNodeId);
            
        } catch (IOException e) {
            log.error("SSE消息发送失败 - 会话: {}, 用户指纹: {}, 错误: {}", sessionId, fingerprint, e.getMessage());
            sseEmitters.remove(fingerprint);
            // 发送错误消息
            sendErrorMessage(session, "SSE消息发送失败: " + e.getMessage(), "SSE_SEND_FAILED");
        } catch (Exception e) {
            log.error("添加问题到QaTree失败 - 会话: {}, 错误: {}", sessionId, e.getMessage());
            // 即使QaTree更新失败，仍然发送SSE消息给前端
            try {
                Map<String, Object> fallbackResponse = new HashMap<>();
                fallbackResponse.put("question", response.getQuestion());
                fallbackResponse.put("currentNodeId", parentId); // 使用parentId作为fallback
                fallbackResponse.put("parentNodeId", parentId);
                
                emitter.send(SseEmitter.event()
                    .name("message")
                    .data(fallbackResponse));
                log.info("SSE消息发送成功（QaTree更新失败但消息已发送） - 会话: {}", sessionId);
            } catch (IOException ioException) {
                log.error("SSE消息发送失败 - 会话: {}, 用户指纹: {}, 错误: {}", sessionId, fingerprint, ioException.getMessage());
                sseEmitters.remove(fingerprint);
                // 发送错误消息
                sendErrorMessage(session, "SSE消息发送失败: " + ioException.getMessage(), "SSE_SEND_FAILED");
            }
        }
    }

    /**
     * 获取SSE连接状态
     *
     * @return 连接状态信息
     */
    public Map<String, Object> getSseStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("connectedFingerprints", sseEmitters.keySet()); // 改为显示指纹列表
        status.put("totalConnections", sseEmitters.size());
        status.put("timestamp", System.currentTimeMillis());
        return status;
    }

    /**
     * 发送欢迎消息
     *
     * @param fingerprint 用户指纹
     * @param message 欢迎消息内容
     */
    public void sendWelcomeMessage(String fingerprint, String message) {
        SseEmitter emitter = sseEmitters.get(fingerprint);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(message));
                log.info("欢迎消息发送成功 - 用户指纹: {}", fingerprint);
            } catch (IOException e) {
                log.error("欢迎消息发送失败 - 用户指纹: {}, 错误: {}", fingerprint, e.getMessage());
                sseEmitters.remove(fingerprint);
            }
        }
    }

    /**
     * 发送连接数据
     *
     * @param fingerprint 用户指纹
     * @param connectionData 连接数据
     */
    public void sendWelcomeMessage(String fingerprint, Map<String, Object> connectionData) {
        SseEmitter emitter = sseEmitters.get(fingerprint);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(connectionData));
                log.info("连接数据发送成功 - 用户指纹: {}", fingerprint);
            } catch (IOException e) {
                log.error("连接数据发送失败 - 用户指纹: {}, 错误: {}", fingerprint, e.getMessage());
                sseEmitters.remove(fingerprint);
            }
        }
    }
    
    /**
     * 发送错误消息给客户端
     * 确保错误时能够立即响应，不会卡住
     * 
     * @param session 会话对象
     * @param errorMessage 错误消息
     * @param errorCode 错误代码
     */
    private void sendErrorMessage(ConversationSession session, String errorMessage, String errorCode) {
        if (session == null) {
            log.warn("无法发送错误消息，会话对象为空");
            return;
        }
        
        String fingerprint = session.getUserId();
        sendErrorMessage(fingerprint, errorMessage, errorCode);
    }
    
    /**
     * 发送错误消息给客户端
     * 确保错误时能够立即响应，不会卡住
     * 
     * @param fingerprint 用户指纹
     * @param errorMessage 错误消息
     * @param errorCode 错误代码
     */
    private void sendErrorMessage(String fingerprint, String errorMessage, String errorCode) {
        SseEmitter emitter = sseEmitters.get(fingerprint);
        if (emitter != null) {
            try {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", true);
                errorResponse.put("errorCode", errorCode);
                errorResponse.put("errorMessage", errorMessage);
                errorResponse.put("timestamp", System.currentTimeMillis());
                
                emitter.send(SseEmitter.event()
                    .name("error")
                    .data(errorResponse));
                    
                log.info("错误消息已发送 - 用户指纹: {}, 错误代码: {}, 错误消息: {}", fingerprint, errorCode, errorMessage);
            } catch (IOException e) {
                log.error("发送错误消息失败 - 用户指纹: {}, 错误: {}", fingerprint, e.getMessage());
                // 如果发送错误消息失败，移除连接
                sseEmitters.remove(fingerprint);
            }
        }
    }
}