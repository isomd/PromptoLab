package io.github.timemachinelab.ainode;

import io.github.timemachinelab.sfchain.annotation.AIOp;
import io.github.timemachinelab.sfchain.core.BaseAIOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



/**
 * 描述: AI对话Demo节点
 * 专门用于演示一个简单的AI对话功能
 * 
 * @author suifeng
 * 日期: 2025/8/13
 */
@AIOp(value = "AI_CHAT_OP",
      description = "实现一个简单的AI对话功能示例",
      defaultModel = "gpt-5-chat")
@Component
@Slf4j
public class AIChatOperation extends BaseAIOperation<AIChatOperation.ChatRequest, AIChatOperation.ChatResponse> {

    /**
     * 构建对话提示
     */
    @Override
    public String buildPrompt(ChatRequest input) {
        return String.format("""
                你是一名智能AI助手，需要根据用户的提问进行回答。
                
                ## 用户提问
                %s
                
                ## 输出要求
                1. 请准确回答用户的问题
                2. 保持回答简洁、直接
                3. 避免任何多余或无关的内容
                4. 返回的答案必须是以下JSON格式：
                ```json
                {
                    "answer": "回答内容",
                    "userInput": "用户输入内容"
                }
                ```
                
                ## 注意事项
                - 确保返回的JSON有效且格式正确
                - 如果问题无法回答，请在回答中说明原因
                """, input.getQuestion());
    }

    /**
     * 解析AI返回的JSON结果
     */
    @Override
    protected ChatResponse parseResult(String jsonContent, ChatRequest input) {
        try {
            // 使用通用的JSON解析工具转换为对象
            return objectMapper.readValue(jsonContent, ChatResponse.class);
        } catch (Exception e) {
            log.warn("解析AI对话结果失败，使用默认响应: {}", e.getMessage());
            ChatResponse response = new ChatResponse();
            response.setAnswer("抱歉，我暂时无法回答这个问题。");
            response.setUserInput(input.getQuestion());
            return response;
        }
    }

    /**
     * 对话请求数据结构
     */
    @Data
    public static class ChatRequest {
        /**
         * 用户提问内容
         */
        private String question;

        public ChatRequest() {
            this.question = "你好，AI！";
        }

        public ChatRequest(String question) {
            this.question = question;
        }
    }

    /**
     * 对话响应数据结构
     */
    @Data
    public static class ChatResponse {
        /**
         * AI的回答内容
         */
        private String answer;

        /**
         * 用户的输入内容
         */
        private String userInput;
    }
}