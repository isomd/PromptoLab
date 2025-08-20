package io.github.timemachinelab.core.session.application;

import io.github.timemachinelab.core.qatree.QaTree;
import io.github.timemachinelab.core.qatree.QaTreeDomain;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 会话管理服务
 * 负责用户与会话的映射管理、nodeId验证等功能
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Service
@Slf4j
public class SessionManagementService {
    
    // 用户ID到会话ID的映射
    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();
    
    // 会话存储
    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();

    @Resource
    private QaTreeDomain qaTreeDomain;
    /**
     * 创建或获取用户会话
     * 如果sessionId为null，创建新会话；否则验证并返回现有会话
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID（可为null）
     * @return 会话对象
     */
    public ConversationSession getOrCreateSession(String userId, String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            // 创建新会话
            return createNewSession(userId);
        } else {
            // 验证现有会话
            return validateAndGetSession(userId, sessionId);
        }
    }
    
    /**
     * 验证nodeId是否属于指定会话
     * 
     * @param sessionId 会话ID
     * @param nodeId 节点ID
     * @return 是否有效
     */
    public boolean validateNodeId(String sessionId, String nodeId) {
        ConversationSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("会话不存在: {}", sessionId);
            return false;
        }
        
        // 检查节点是否存在于qaTree中
        boolean exists = session.getQaTree().getNodeById(nodeId) != null;
        if (!exists) {
            log.warn("节点不存在 - 会话: {}, 节点: {}", sessionId, nodeId);
        }
        
        return exists;
    }
    
    /**
     * 获取用户当前会话
     * 
     * @param userId 用户ID
     * @return 会话对象
     */
    public ConversationSession getUserCurrentSession(String userId) {
        String sessionId = userSessionMap.get(userId);
        return sessionId != null ? sessions.get(sessionId) : null;
    }

    /**
     * 创建新会话
     */
    private ConversationSession createNewSession(String userId) {
        // 如果用户已有会话，先清理旧的映射
        String oldSessionId = userSessionMap.get(userId);
        if (oldSessionId != null) {
            sessions.remove(oldSessionId);
            log.info("清理用户旧会话 - 用户: {}, 旧会话: {}", userId, oldSessionId);
        }
        
        // 生成新的sessionId
        String newSessionId = UUID.randomUUID().toString();
        QaTree tree = createDefaultQaTree();
        
        // 创建新会话
        ConversationSession session = new ConversationSession(userId, newSessionId, tree);

        // 建立映射关系
        userSessionMap.put(userId, session.getSessionId());
        sessions.put(session.getSessionId(), session);

        log.info("创建新会话 - 用户: {}, 会话: {}", userId, session.getSessionId());
        return session;
    }
    
    /**
     * 验证并获取现有会话
     */
    public ConversationSession validateAndGetSession(String userId, String sessionId) {
        ConversationSession session = sessions.get(sessionId);
        
        if (session == null) {
            log.warn("会话不存在，创建新会话 - 用户: {}, 请求会话: {}", userId, sessionId);
            return createNewSession(userId);
        }
        
        // 验证会话是否属于该用户
        if (!session.getUserId().equals(userId)) {
            log.warn("会话不属于该用户，创建新会话 - 用户: {}, 会话: {}, 会话所有者: {}", 
                    userId, sessionId, session.getUserId());
            return createNewSession(userId);
        }
        
        // 更新用户会话映射（防止映射不一致）
        userSessionMap.put(userId, sessionId);
        
        log.debug("验证会话成功 - 用户: {}, 会话: {}", userId, sessionId);
        return session;
    }
    
    /**
     * 清理会话
     * 
     * @param sessionId 会话ID
     */
    public void removeSession(String sessionId) {
        ConversationSession session = sessions.remove(sessionId);
        if (session != null) {
            userSessionMap.remove(session.getUserId());
            log.info("清理会话 - 用户: {}, 会话: {}", session.getUserId(), sessionId);
        }
    }
    
    /**
     * 获取会话统计信息
     */
    public Map<String, Object> getSessionStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalSessions", sessions.size());
        stats.put("activeUsers", userSessionMap.size());
        stats.put("timestamp", System.currentTimeMillis());
        return stats;
    }

    private QaTree createDefaultQaTree() {

        QaTree tree = qaTreeDomain.createTree("default");

        return tree;
    }
}