package io.github.timemachinelab.core.session.infrastructure.ai;

import com.suifeng.sfchain.annotation.AIOp;
import com.suifeng.sfchain.core.BaseAIOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

//@AIOp(value = "CONVERSATION_OP",
//      description = "基于QATree的智能会话操作，支持上下文理解和结构化对话",
//      defaultModel = "gpt-4-turbo",
//      requireJsonOutput = true,
//      supportThinking = true,
//      defaultMaxTokens = 2048,
//      defaultTemperature = 0.8)
//@Component
@Slf4j
public class ConversationOperation extends BaseAIOperation<ConversationOperation.ConversationRequest, ConversationOperation.ConversationResponse> {

    @Override
    public String buildPrompt(ConversationRequest input) {
        StringBuilder promptBuilder = new StringBuilder();
        
        promptBuilder.append("""
                你是一名智能AI助手，专门负责与用户进行结构化对话。
                你需要根据用户的输入和对话历史，提供有价值的回复。
                
                ## 对话上下文
                会话ID: %s
                当前节点ID: %s
                """.formatted(input.getSessionId(), input.getCurrentNodeId()));
        
        // 添加对话历史
        if (input.getConversationHistory() != null && !input.getConversationHistory().isEmpty()) {
            promptBuilder.append("\n## 对话历史\n");
            for (int i = 0; i < input.getConversationHistory().size(); i++) {
                ConversationHistory history = input.getConversationHistory().get(i);
                promptBuilder.append(String.format("%d. %s: %s\n", 
                    i + 1, history.getRole(), history.getContent()));
            }
        }
        
        promptBuilder.append("""
                
                ## 当前用户输入
                %s
                
                ## 回复要求
                1. 根据用户输入和对话历史，提供有针对性的回复
                2. 如果用户提出需求（如生成系统、解决问题等），可以提供选择题让用户进一步明确需求
                3. 保持回复简洁、专业、有帮助
                4. 必须返回以下JSON格式：
                
                ```json
                {
                    "answer": "你的回复内容",
                    "responseType": "TEXT",
                    "options": [],
                    "nextQuestionSuggestion": "建议的下一个问题（可选）",
                    "metadata": {
                        "confidence": 0.95,
                        "category": "general"
                    }
                }
                ```
                
                ## 响应类型说明
                - TEXT: 普通文本回复
                - SELECTION: 选择题回复（需要填充options数组）
                - FORM: 表单回复（需要用户填写信息）
                
                ## 注意事项
                - 确保JSON格式正确且有效
                - 如果是选择题，options数组不能为空
                - confidence值范围0-1，表示回答的置信度
                - category可以是：general, technical, creative, problem_solving等
                """.formatted(input.getUserMessage()));
        
        return promptBuilder.toString();
    }

    @Override
    protected ConversationResponse parseResult(String jsonContent, ConversationRequest input) {
        try {
            ConversationResponse response = objectMapper.readValue(jsonContent, ConversationResponse.class);
            
            // 验证响应完整性
            if (response.getAnswer() == null || response.getAnswer().trim().isEmpty()) {
                log.warn("AI回复内容为空，使用默认回复");
                response.setAnswer("抱歉，我需要更多信息来帮助您。");
            }
            
            if (response.getResponseType() == null) {
                response.setResponseType(ResponseType.TEXT);
            }
            
            // 如果是选择题但没有选项，转为普通文本
            if (response.getResponseType() == ResponseType.SELECTION && 
                (response.getOptions() == null || response.getOptions().length == 0)) {
                log.warn("选择题类型但无选项，转为文本类型");
                response.setResponseType(ResponseType.TEXT);
            }
            
            return response;
            
        } catch (Exception e) {
            log.warn("解析AI会话结果失败，使用默认响应: {}", e.getMessage());
            
            ConversationResponse fallbackResponse = new ConversationResponse();
            fallbackResponse.setAnswer("我理解了您的需求，让我为您提供更详细的帮助。");
            fallbackResponse.setResponseType(ResponseType.TEXT);
            fallbackResponse.setOptions(new String[0]);
            
            ConversationMetadata metadata = new ConversationMetadata();
            metadata.setConfidence(0.5);
            metadata.setCategory("general");
            fallbackResponse.setMetadata(metadata);
            
            return fallbackResponse;
        }
    }

    @Data
    public static class ConversationRequest {
        private String sessionId;
        private String currentNodeId;
        private String userMessage;
        private java.util.List<ConversationHistory> conversationHistory;
        private java.util.Map<String, Object> context;
        
        public ConversationRequest() {
            this.conversationHistory = new java.util.ArrayList<>();
            this.context = new java.util.HashMap<>();
        }
        
        public ConversationRequest(String sessionId, String currentNodeId, String userMessage) {
            this();
            this.sessionId = sessionId;
            this.currentNodeId = currentNodeId;
            this.userMessage = userMessage;
        }
    }

    @Data
    public static class ConversationResponse {
        private String answer;
        private ResponseType responseType;
        private String[] options;
        private String nextQuestionSuggestion;
        private ConversationMetadata metadata;
        
        public ConversationResponse() {
            this.options = new String[0];
            this.metadata = new ConversationMetadata();
        }
    }

    @Data
    public static class ConversationHistory {
        private String role; // "user" or "assistant"
        private String content;
        private String nodeId;
        private Long timestamp;
        
        public ConversationHistory() {}
        
        public ConversationHistory(String role, String content, String nodeId) {
            this.role = role;
            this.content = content;
            this.nodeId = nodeId;
            this.timestamp = System.currentTimeMillis();
        }
    }

    @Data
    public static class ConversationMetadata {
        private Double confidence;
        private String category;
        private java.util.Map<String, Object> additionalInfo;
        
        public ConversationMetadata() {
            this.additionalInfo = new java.util.HashMap<>();
        }
    }

    public enum ResponseType {
        TEXT,
        SELECTION,
        FORM
    }
}