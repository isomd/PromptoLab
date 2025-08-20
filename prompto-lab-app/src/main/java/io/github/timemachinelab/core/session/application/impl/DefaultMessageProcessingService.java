package io.github.timemachinelab.core.session.application.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.JsonObject;
import dev.langchain4j.internal.Json;
import io.github.timemachinelab.core.constant.AllPrompt;
import io.github.timemachinelab.core.qatree.QaTree;
import io.github.timemachinelab.core.qatree.QaTreeDomain;
import io.github.timemachinelab.core.session.application.MessageProcessingService;
import io.github.timemachinelab.core.session.application.SessionManagementService;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.web.dto.UnifiedAnswerRequest;
import io.github.timemachinelab.util.QaTreeSerializeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 默认消息处理服务实现
 * 提供基础的消息处理逻辑，可根据需要进行扩展
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Service
@Slf4j
public class DefaultMessageProcessingService implements MessageProcessingService {

    @Resource
    SessionManagementService sessionManagementService;
    @Resource
    QaTreeDomain qaTreeDomain;

    @Override
    public String processAnswer(UnifiedAnswerRequest request) {
        if (!validateAnswer(request)) {
            log.warn("无效的答案请求: {}", request);
            return "无效的回答格式";
        }
        
        // 将答案转换为可读文本
        String readableText = request.toReadableText();
        
        // 构建完整的回答消息
        StringBuilder message = new StringBuilder();
        message.append("用户回答了问题: ");
        message.append(readableText);
        
        log.info("处理答案请求 - 问题类型: {}, 内容: {}", request.getQuestionType(), readableText);
        
        return message.toString();
    }
    
    @Override
    public String preprocessMessage(String originalMessage, UnifiedAnswerRequest answerRequest,ConversationSession conversationSession) {
        try {

            JSONObject object = new JSONObject();
            object.put("prompt",AllPrompt.GLOBAL_PROMPT);
            object.put("tree", QaTreeSerializeUtil.serialize(conversationSession.getQaTree()));
            object.put("input", answerRequest.getAnswerString());
            return object.toString();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("输入加工失败",e);
        }

    }
    
    @Override
    public boolean validateAnswer(UnifiedAnswerRequest request) {
        if (request == null) {
            return false;
        }
        
        if (request.getQuestionType() == null || request.getAnswer() == null) {
            return false;
        }
        
        // 根据问题类型验证答案格式
        switch (request.getQuestionType().toLowerCase()) {
            case "single":
            case "multi":
                return request.getChoiceAnswer() != null && !request.getChoiceAnswer().isEmpty();
                
            case "input":
                String input = request.getInputAnswer();
                return input != null && !input.trim().isEmpty();
                
            case "form":
                return request.getFormAnswer() != null && !request.getFormAnswer().isEmpty();
                
            default:
                log.warn("未知的问题类型: {}", request.getQuestionType());
                return false;
        }
    }

}