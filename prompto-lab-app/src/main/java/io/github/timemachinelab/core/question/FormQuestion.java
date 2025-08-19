package io.github.timemachinelab.core.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 表单问题类
 * 表单形式的提问实际上是其他提问类型的组合
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormQuestion extends BaseQuestion {
    
    /**
     * 表单字段列表
     */
    private List<FormField> fields;
    
    /**
     * 表单答案
     */
    private List<AnswerItem> answer;
    
    /**
     * 构造函数
     */
    public FormQuestion() {
        super(QuestionType.FORM);
    }


    @Data
    @AllArgsConstructor
    public static class AnswerItem{
        private String id;
        private List<String> value;
    }
}