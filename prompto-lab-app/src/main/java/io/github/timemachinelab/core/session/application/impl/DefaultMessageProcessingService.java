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
        
        // 获取会话并更新qaTree
        ConversationSession session = sessionManagementService.validateAndGetSession(request.getUserId(), request.getSessionId());
        if (session != null) {
            updateQaTreeWithAnswer(session, request);
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
    
    /**
     * 更新qaTree中的答案
     */
    private void updateQaTreeWithAnswer(ConversationSession session, UnifiedAnswerRequest request) {
        try {
            QaTree qaTree = session.getQaTree();
            if (qaTree == null) {
                log.warn("会话的qaTree为空，无法更新答案 - 会话ID: {}", session.getSessionId());
                return;
            }
            
            String nodeId = request.getNodeId();
            // 如果nodeId为'1'（根节点），使用根节点ID
            if ("1".equals(nodeId) && qaTree.getRoot() != null) {
                nodeId = qaTree.getRoot().getId();
            }
            
            // 根据问题类型准备答案数据
            Object answerData = prepareAnswerData(request);
            
            // 更新节点答案
             boolean updated = qaTreeDomain.updateNodeAnswer(qaTree, nodeId, answerData);
             if (updated) {
                 log.info("成功更新qaTree节点答案 - 会话ID: {}, 节点ID: {}", session.getSessionId(), nodeId);
                 // 会话数据已在内存中更新，无需额外保存操作
             } else {
                 log.warn("更新qaTree节点答案失败 - 会话ID: {}, 节点ID: {}", session.getSessionId(), nodeId);
             }
        } catch (Exception e) {
            log.error("更新qaTree答案时发生异常 - 会话ID: {}, 错误: {}", session.getSessionId(), e.getMessage(), e);
        }
    }
    
    /**
     * 根据问题类型准备答案数据
     */
    private Object prepareAnswerData(UnifiedAnswerRequest request) {
        switch (request.getQuestionType().toLowerCase()) {
            case "input":
                return request.getInputAnswer();
            case "single":
            case "multi":
                return request.getChoiceAnswer();
            case "form":
                return request.getFormAnswer();
            default:
                return request.getAnswerString();
        }
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