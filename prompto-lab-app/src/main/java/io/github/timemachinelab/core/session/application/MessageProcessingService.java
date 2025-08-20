package io.github.timemachinelab.core.session.application;

import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.web.dto.UnifiedAnswerRequest;

/**
 * 消息处理服务接口
 * 用于处理和加工用户消息，然后传递给大模型
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
public interface MessageProcessingService {
    
    /**
     * 处理统一答案请求
     * 将用户的回答转换为适合大模型处理的格式
     * 
     * @param request 统一答案请求
     * @return 处理后的消息内容
     */
    String processAnswer(UnifiedAnswerRequest request);
    
    /**
     * 预处理消息
     * 在发送给大模型之前对消息进行加工
     *
     * @param originalMessage 原始消息
     * @param answerRequest 答案请求（可能为null）
     * @return 加工后的消息
     */
    String preprocessMessage(String originalMessage, UnifiedAnswerRequest answerRequest, ConversationSession conversationSession);
    
    /**
     * 验证答案格式
     * 
     * @param request 答案请求
     * @return 是否有效
     */
    boolean validateAnswer(UnifiedAnswerRequest request);
}