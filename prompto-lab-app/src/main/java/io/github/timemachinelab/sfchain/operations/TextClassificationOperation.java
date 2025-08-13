package io.github.timemachinelab.sfchain.operations;

import io.github.timemachinelab.sfchain.annotation.AIOp;
import io.github.timemachinelab.sfchain.core.BaseAIOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static io.github.timemachinelab.sfchain.constants.AIOperationConstant.TEXT_CLASSIFICATION_OP;

/**
 * 描述: 文本分类操作 - 对文本进行情感分析或主题分类
 * 
 * @author suifeng
 * 日期: 2025/8/11
 */
@Slf4j
@Component
@AIOp(
    value = TEXT_CLASSIFICATION_OP,
    description = "对输入文本进行分类，支持情感分析、主题分类等",
    defaultModel = "deepseek-ai/DeepSeek-V3"
)
public class TextClassificationOperation extends BaseAIOperation<TextClassificationOperation.ClassificationRequest, TextClassificationOperation.ClassificationResult> {

    @Override
    protected String buildPrompt(ClassificationRequest request) {
        return String.format("""
            请对以下文本进行分类：
            
            文本内容：
            %s
            
            分类类型：%s
            
            请根据分类类型对文本进行分析，并以JSON格式返回结果：
            
            ```json
            {
                "category": "分类结果",
                "confidence": 0.95,
                "reason": "分类理由"
            }
            ```
            
            注意：
            - category: 分类结果（如：正面/负面/中性，或具体的主题类别）
            - confidence: 置信度（0-1之间的数值）
            - reason: 简要说明分类的理由
            """, request.getText(), request.getClassificationType());
    }
    
    /**
     * 解析AI返回的JSON为最终结果
     */
    @Override
    protected ClassificationResult parseResult(String jsonContent, ClassificationRequest input) {
        try {
            // 解析AI返回的JSON
            ClassificationResult result = parseJsonToObject(jsonContent, ClassificationResult.class);
            
            // 验证分类结果
            if (result.getCategory() == null || result.getCategory().trim().isEmpty()) {
                throw new RuntimeException("分类结果为空");
            }
            
            // 确保置信度在合理范围内
            if (result.getConfidence() < 0.0 || result.getConfidence() > 1.0) {
                result.setConfidence(Math.max(0.0, Math.min(1.0, result.getConfidence())));
            }
            
            // 根据请求类型进行额外的后处理
            if (input != null) {
                // 例如：根据分类类型进行特殊处理
                if ("情感分析".contains(input.getClassificationType())) {
                    // 情感分析的特殊处理逻辑
                    validateSentimentResult(result);
                } else if ("垃圾邮件检测".contains(input.getClassificationType())) {
                    // 垃圾邮件检测的特殊处理逻辑
                    validateSpamDetectionResult(result);
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("解析分类结果失败: {}", e.getMessage(), e);
            throw new RuntimeException("解析分类结果失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证情感分析结果
     */
    private void validateSentimentResult(ClassificationResult result) {
        if (result.getCategory() != null) {
            String category = result.getCategory().toLowerCase();
            if (!category.contains("正面") && !category.contains("负面") && !category.contains("中性")) {
                log.warn("情感分析结果可能不准确: {}", result.getCategory());
            }
        }
    }
    
    /**
     * 验证垃圾邮件检测结果
     */
    private void validateSpamDetectionResult(ClassificationResult result) {
        if (result.getCategory() != null) {
            String category = result.getCategory().toLowerCase();
            if (!category.contains("垃圾") && !category.contains("正常") && !category.contains("spam") && !category.contains("ham")) {
                log.warn("垃圾邮件检测结果可能不准确: {}", result.getCategory());
            }
        }
    }
    
    /**
     * 分类请求
     */
    @Data
    public static class ClassificationRequest {
        /**
         * 待分类的文本
         */
        private String text;
        
        /**
         * 分类类型（如：情感分析、主题分类、垃圾邮件检测等）
         */
        private String classificationType;
        
        public ClassificationRequest() {}
        
        public ClassificationRequest(String text, String classificationType) {
            this.text = text;
            this.classificationType = classificationType;
        }
    }
    
    /**
     * 分类结果
     */
    @Data
    public static class ClassificationResult {
        /**
         * 分类结果
         */
        private String category;
        
        /**
         * 置信度（0-1）
         */
        private Double confidence;
        
        /**
         * 分类理由
         */
        private String reason;
        
        /**
         * 是否为高置信度结果
         */
        public boolean isHighConfidence() {
            return confidence != null && confidence >= 0.8;
        }
        
        /**
         * 获取置信度等级
         */
        public String getConfidenceLevel() {
            if (confidence == null) return "未知";
            if (confidence >= 0.9) return "很高";
            if (confidence >= 0.7) return "高";
            if (confidence >= 0.5) return "中等";
            return "低";
        }
        
        @Override
        public String toString() {
            return String.format(
                "ClassificationResult{category='%s', confidence=%.2f (%s), reason='%s'}",
                category, confidence, getConfidenceLevel(), reason
            );
        }
    }
}