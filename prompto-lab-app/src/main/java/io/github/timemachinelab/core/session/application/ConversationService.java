package io.github.timemachinelab.core.session.application;

import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.web.dto.MessageResponse;
import io.github.timemachinelab.core.session.infrastructure.ai.ConversationOperation;
import io.github.timemachinelab.sfchain.core.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {
    
    private final AIService aiService;

    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();
    
    public ConversationSession createSession(String userId) {
        ConversationSession session = new ConversationSession(userId);
        sessions.put(session.getSessionId(), session);
        return session;
    }
    
    public ConversationSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public void processUserMessage(String sessionId, String userMessage, Consumer<MessageResponse> sseCallback) {
        ConversationSession session = sessions.get(sessionId);
        if (session == null) {
            log.warn("会话不存在: {}", sessionId);
            return;
        }
        
        // 1. 添加用户消息到会话历史
        session.addMessage(userMessage, "user");
        
        // 2. 发送用户消息确认
        sseCallback.accept(MessageResponse.userAnswer("user_" + System.currentTimeMillis(), userMessage));
        
        // 3. 调用AI服务获取回复
        processAIResponse(session, userMessage, sseCallback);
    }
    
    private void processAIResponse(ConversationSession session, String userMessage, Consumer<MessageResponse> sseCallback) {
        try {
            // 构建对话历史
            List<ConversationOperation.ConversationHistory> history = buildConversationHistory(session);
            
            // 创建AI请求
            ConversationOperation.ConversationRequest request = new ConversationOperation.ConversationRequest(
                session.getSessionId(),
                "current",
                userMessage
            );
            request.setConversationHistory(history);
            
            // 调用AI服务
            ConversationOperation.ConversationResponse aiResponse = aiService.execute("CONVERSATION_OP", request);
            log.info("AI服务调用成功: {}", aiResponse);

            // 添加AI回复到会话历史
            session.addMessage(aiResponse.getAnswer(), "assistant");
            
            // 根据响应类型处理AI回复
            String nodeId = "ai_" + System.currentTimeMillis();
            sseCallback.accept(MessageResponse.aiAnswer("ai_" + System.currentTimeMillis(), aiResponse.getAnswer()));
//            if (aiResponse.getResponseType() == ConversationOperation.ResponseType.SELECTION) {
//                // 选择题类型
//                sseCallback.accept(MessageResponse.aiSelectionQuestion(nodeId, aiResponse.getAnswer(), aiResponse.getOptions()));
//            } else {
//                // 普通文本回复
//                sseCallback.accept(MessageResponse.aiQuestion(nodeId, aiResponse.getAnswer()));
//            }
            
        } catch (Exception e) {
            log.error("AI服务调用失败: {}", e.getMessage(), e);
            // 降级处理
            String fallbackResponse = "抱歉，我暂时无法处理您的请求，请稍后再试。";
            session.addMessage(fallbackResponse, "assistant");
            String nodeId = "ai_" + System.currentTimeMillis();
            sseCallback.accept(MessageResponse.aiQuestion(nodeId, fallbackResponse));
        }
    }
    
    private List<ConversationOperation.ConversationHistory> buildConversationHistory(ConversationSession session) {
        List<ConversationOperation.ConversationHistory> history = new ArrayList<>();
        
        // 从会话消息构建对话历史
        for (ConversationSession.ConversationMessage message : session.getMessages()) {
            ConversationOperation.ConversationHistory historyItem = new ConversationOperation.ConversationHistory(
                message.getRole(),
                message.getContent(),
                message.getRole() + "_" + message.getTimestamp().toString()
            );
            history.add(historyItem);
        }
        
        return history;
    }


}