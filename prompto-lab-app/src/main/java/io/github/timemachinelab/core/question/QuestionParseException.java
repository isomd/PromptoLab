package io.github.timemachinelab.core.question;

/**
 * 问题解析异常
 * 
 * @author suifeng
 * 日期: 2025/8/20
 */
public class QuestionParseException extends Exception {
    
    private final String jsonContent;
    private final String failureReason;
    
    public QuestionParseException(String message, String jsonContent, String failureReason) {
        super(message);
        this.jsonContent = jsonContent;
        this.failureReason = failureReason;
    }
    
    public QuestionParseException(String message, String jsonContent, String failureReason, Throwable cause) {
        super(message, cause);
        this.jsonContent = jsonContent;
        this.failureReason = failureReason;
    }
    
    public String getJsonContent() {
        return jsonContent;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    @Override
    public String toString() {
        return String.format("QuestionParseException: %s\n原因: %s\nJSON内容: %s", 
                           getMessage(), failureReason, jsonContent);
    }
}