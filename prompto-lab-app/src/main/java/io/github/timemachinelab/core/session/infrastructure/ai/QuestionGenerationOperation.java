package io.github.timemachinelab.core.session.infrastructure.ai;

import com.suifeng.sfchain.annotation.AIOp;
import com.suifeng.sfchain.core.BaseAIOperation;
import io.github.timemachinelab.core.question.BaseQuestion;
import io.github.timemachinelab.core.question.QuestionParser;
import io.github.timemachinelab.core.question.QuestionParseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@AIOp(value = "QUESTION_GENERATION_OP",
      description = "基于对话树和用户输入生成结构化问题的AI操作"
)
@Component
@Slf4j
public class QuestionGenerationOperation extends BaseAIOperation<QuestionGenerationOperation.QuestionGenerationRequest, QuestionGenerationOperation.QuestionGenerationResponse> {

    @Override
    public String buildPrompt(QuestionGenerationRequest input) {
        StringBuilder promptBuilder = new StringBuilder();
        
        // 添加全局提示词
        if (input.getGlobalPrompt() != null && !input.getGlobalPrompt().trim().isEmpty()) {
            promptBuilder.append(input.getGlobalPrompt());
            promptBuilder.append("\n\n");
        }
        
        // 添加对话树信息
        if (input.getConversationTree() != null && !input.getConversationTree().trim().isEmpty()) {
            promptBuilder.append("## 对话树结构\n");
            promptBuilder.append(input.getConversationTree());
            promptBuilder.append("\n\n");
        }
        
        // 添加用户输入
        promptBuilder.append("## 当前用户输入\n");
        promptBuilder.append(input.getUserInput());
        
        return promptBuilder.toString();
    }

    @Override
    protected QuestionGenerationOperation.QuestionGenerationResponse parseResult(String jsonContent, QuestionGenerationRequest input) {
        try {
            // 使用QuestionParser解析AI返回的JSON
            QuestionGenerationResponse question = QuestionParser.parseQuestion(jsonContent);
            log.info("成功生成问题，类型: {}", question.getClass().getSimpleName());
            return question;
            
        } catch (QuestionParseException e) {
            log.error("解析AI生成的问题失败: {}", e.toString());
            throw new RuntimeException("问题解析失败: " + e.getFailureReason(), e);
        } catch (Exception e) {
            log.error("处理AI响应时发生未知错误: {}", e.getMessage(), e);
            throw new RuntimeException("处理响应失败: " + e.getMessage(), e);
        }
    }

    @Data
    public static class QuestionGenerationRequest {
        /**
         * 全局提示词
         */
        private String globalPrompt;
        
        /**
         * 对话树结构
         */
        private String conversationTree;
        
        /**
         * 用户输入
         */
        private String userInput;
        
        /**
         * 额外的上下文信息
         */
        private java.util.Map<String, Object> context;
        
        public QuestionGenerationRequest() {
            this.context = new java.util.HashMap<>();
        }
        
        public QuestionGenerationRequest(String globalPrompt, String conversationTree, String userInput) {
            this();
            this.globalPrompt = globalPrompt;
            this.conversationTree = conversationTree;
            this.userInput = userInput;
        }
    }

    @Data
    @AllArgsConstructor
    public static class QuestionGenerationResponse {
        private BaseQuestion question;
        private String parentId;
    }
}