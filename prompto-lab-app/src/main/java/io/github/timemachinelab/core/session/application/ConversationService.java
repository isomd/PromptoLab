package io.github.timemachinelab.core.session.application;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.timemachinelab.core.qatree.QaTree;
import io.github.timemachinelab.core.qatree.QaTreeDomain;
import io.github.timemachinelab.core.qatree.QaTreeNode;
import io.github.timemachinelab.core.question.BaseQuestion;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import io.github.timemachinelab.core.session.infrastructure.ai.QuestionGenerationOperation;
import io.github.timemachinelab.core.session.infrastructure.web.dto.MessageResponse;
import io.github.timemachinelab.core.session.infrastructure.ai.ConversationOperation;
import io.github.timemachinelab.sfchain.core.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    @Resource
    private final AIService aiService;
    @Resource
    private SessionManagementService sessionManagementService;
    private final QaTreeDomain qaTreeDomain;

    
    public void processUserMessage(String userId, String userMessage, Consumer<QuestionGenerationOperation.QuestionGenerationResponse> sseCallback) {
        ConversationSession session = sessionManagementService.getUserLatestSession(userId);
        if (session == null) {
            log.warn("会话不存在");
            return;
        }

        processAIResponse(userMessage, sseCallback);
    }
    
    private void processAIResponse(String userMessage, Consumer<QuestionGenerationOperation.QuestionGenerationResponse> sseCallback) {
        try {

            JSONObject object = JSON.parseObject(userMessage);

            // 创建AI请求
            QuestionGenerationOperation.QuestionGenerationRequest request = new QuestionGenerationOperation.QuestionGenerationRequest(object.getString("prompt"),object.getString("tree"),object.getString("input"));
            // 调用AI服务
            QuestionGenerationOperation.QuestionGenerationResponse aiResponse = aiService.execute("QUESTION_GENERATION_OP", request);

            sseCallback.accept(aiResponse);
            log.info("AI服务调用成功: {}", aiResponse);
            
        } catch (Exception e) {
            log.error("AI服务调用失败: {}", e.getMessage(), e);
            // 降级处理
            String fallbackResponse = "抱歉，我暂时无法处理您的请求，请稍后再试。";
            String nodeId = "ai_" + System.currentTimeMillis();
        }
    }


}