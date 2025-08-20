package io.github.timemachinelab.core.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 输入框问题类
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputQuestion extends BaseQuestion {
    
    /**
     * 问题答案
     */
    private String answer;
    
    /**
     * 构造函数
     */
    public InputQuestion() {
        super(QuestionType.INPUT);
    }

}