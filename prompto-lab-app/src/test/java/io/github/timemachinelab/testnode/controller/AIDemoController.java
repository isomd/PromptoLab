package io.github.timemachinelab.testnode.controller;

import com.suifeng.sfchain.core.AIService;
import io.github.timemachinelab.testnode.ainode.AIChatOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 描述: AI服务Demo控制器
 * 演示如何使用AIService调用AI节点
 * 
 * @author suifeng
 * 日期: 2025/1/20
 */
@RestController
@RequestMapping("/api/ai")
@Slf4j
public class AIDemoController {
    
    @Resource
    private AIService aiService;
    
    /**
     * 简单AI对话接口
     * 
     * @param question 用户问题
     * @return AI回答
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestParam String question) {
        try {
            // 创建请求对象
            AIChatOperation.ChatRequest request = new AIChatOperation.ChatRequest(question);
            
            // 调用AI服务
            AIChatOperation.ChatResponse response = aiService.execute("AI_CHAT_OP", request);
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", response);
            result.put("message", "AI对话成功");
            
            return result;
            
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "AI对话失败: " + e.getMessage());
            
            return result;
        }
    }
}