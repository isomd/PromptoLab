package io.github.timemachinelab.core.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 选项类
 * 用于单选题和多选题的选项
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Data
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Option {
    
    /**
     * 选项标识
     */
    private String id;
    
    /**
     * 选项显示文本
     */
    private String label;
    
    /**
     * 构造函数
     * 
     * @param id 选项标识
     * @param label 选项显示文本
     */
    public Option(String id, String label) {
        this.id = id;
        this.label = label;
    }
}