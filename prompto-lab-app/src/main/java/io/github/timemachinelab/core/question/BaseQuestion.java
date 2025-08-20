package io.github.timemachinelab.core.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基础问题类
 * 所有问题类型的父类
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SingleChoiceQuestion.class, name = "single"),
    @JsonSubTypes.Type(value = MultipleChoiceQuestion.class, name = "multi"),
    @JsonSubTypes.Type(value = InputQuestion.class, name = "input"),
    @JsonSubTypes.Type(value = FormQuestion.class, name = "form")
})
public abstract class BaseQuestion {
    
    /**
     * 问题描述
     */
    private String question;
    
    /**
     * 问题类型
     */
    private String type;

    
    /**
     * 问题的详细说明、引导提示或补充解释（可选）
     */
    private String desc;
    
    /**
     * 问题ID（用于答案关联）
     */
    private String questionId;
    
    /**
     * 构造函数
     * 
     * @param type 问题类型
     */
    protected BaseQuestion(QuestionType type) {
        this.type = type.getType();
    }
}