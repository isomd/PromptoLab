package io.github.timemachinelab.core.question;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 表单字段类
 * 用于表单问题中的字段定义
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Data
@Builder
@NoArgsConstructor

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormField {
    
    /**
     * 字段标识
     */
    private String id;
    
    /**
     * 字段问题描述
     */
    private String question;
    
    /**
     * 字段类型（input、single、multi）
     */
    private String type;
    
    /**
     * 选项列表（仅single/multi类型需要）
     */
    private List<Option> options;
    
    /**
     * 字段的详细说明或引导（可选）
     */
    private String desc;
    
    /**
     * 权重分数
     */
    private String weight;
    
    /**
     * 构造函数 - 输入框字段
     * 
     * @param id 字段标识
     * @param question 字段问题描述
     */
    public FormField(String id, String question) {
        this.id = id;
        this.question = question;
        this.type = "input";
    }
    
    /**
     * 构造函数 - 选择字段
     * 
     * @param id 字段标识
     * @param question 字段问题描述
     * @param type 字段类型
     * @param options 选项列表
     */
    public FormField(String id, String question, String type, List<Option> options) {
        this.id = id;
        this.question = question;
        this.type = type;
        this.options = options;
    }
    
    /**
     * 构造函数 - 完整字段
     * 
     * @param id 字段标识
     * @param question 字段问题描述
     * @param type 字段类型
     * @param options 选项列表
     * @param desc 字段说明
     * @param weight 权重分数
     */
    public FormField(String id, String question, String type, List<Option> options, String desc, String weight) {
        this.id = id;
        this.question = question;
        this.type = type;
        this.options = options;
        this.desc = desc;
        this.weight = weight;
    }
    
    /**
     * 构造函数 - 完整字段（Double权重）
     * 
     * @param id 字段标识
     * @param question 字段问题描述
     * @param type 字段类型
     * @param options 选项列表
     * @param desc 字段说明
     * @param weight 权重分数
     */
    public FormField(String id, String question, String type, List<Option> options, String desc, Double weight) {
        this.id = id;
        this.question = question;
        this.type = type;
        this.options = options;
        this.desc = desc;
        this.weight = weight != null ? weight.toString() : null;
    }
}