package io.github.timemachinelab.core.question;

/**
 * 问题类型枚举
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
public enum QuestionType {
    
    /**
     * 单选题
     */
    SINGLE("single"),
    
    /**
     * 多选题
     */
    MULTI("multi"),
    
    /**
     * 输入框
     */
    INPUT("input"),
    
    /**
     * 表单
     */
    FORM("form");
    
    private final String type;
    
    QuestionType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    /**
     * 根据字符串获取问题类型
     * 
     * @param type 类型字符串
     * @return 问题类型
     */
    public static QuestionType fromString(String type) {
        for (QuestionType questionType : QuestionType.values()) {
            if (questionType.type.equals(type)) {
                return questionType;
            }
        }
        throw new IllegalArgumentException("未知的问题类型: " + type);
    }
}