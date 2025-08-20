package io.github.timemachinelab.core.question;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * 问题JSON解析工具类
 * 使用责任链模式依次尝试解析不同类型的问题
 * 
 * @author suifeng
 * 日期: 2025/1/27
 */
@Slf4j
public class QuestionParser {
    
    /**
     * 问题类型解析链，按顺序尝试解析
     */
    private static final List<Class<? extends BaseQuestion>> QUESTION_TYPES = Arrays.asList(
        FormQuestion.class,
        SingleChoiceQuestion.class,
        MultipleChoiceQuestion.class,
        InputQuestion.class
    );
    
    /**
     * 解析JSON字符串为BaseQuestion对象
     * 依次尝试解析成不同类型，直到成功为止
     * 
     * @param jsonStr JSON字符串
     * @return BaseQuestion对象
     * @throws QuestionParseException 解析失败时抛出异常
     */
    public static BaseQuestion parseQuestion(String jsonStr) throws QuestionParseException {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            throw new QuestionParseException("JSON字符串不能为空", jsonStr, "输入为空或null");
        }
        
        // 先解析为JSONObject以便验证字段
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(jsonStr);
        } catch (JSONException e) {
            throw new QuestionParseException("JSON格式错误", jsonStr, "JSON语法不正确: " + e.getMessage(), e);
        }
        
        // 收集所有解析失败的原因
        List<String> failureReasons = new ArrayList<>();
        
        // 依次尝试解析成不同类型
        for (Class<? extends BaseQuestion> questionType : QUESTION_TYPES) {
            try {
                BaseQuestion question = JSON.parseObject(jsonStr, questionType);
                if (question != null) {
                    String validationResult = validateQuestion(question, jsonObject);
                    if (validationResult == null) {
                        log.info("成功解析为: {}", questionType.getSimpleName());
                        return question;
                    } else {
                        failureReasons.add(questionType.getSimpleName() + ": " + validationResult);
                    }
                } else {
                    failureReasons.add(questionType.getSimpleName() + ": 解析结果为null");
                }
            } catch (JSONException e) {
                failureReasons.add(questionType.getSimpleName() + ": JSON解析异常 - " + e.getMessage());
            } catch (Exception e) {
                failureReasons.add(questionType.getSimpleName() + ": 未知异常 - " + e.getMessage());
            }
        }
        
        // 所有类型都解析失败，抛出详细异常
        String allFailures = String.join("; ", failureReasons);
        throw new QuestionParseException(
            "无法解析为任何已知的问题类型", 
            jsonStr, 
            "所有类型解析失败: " + allFailures
        );
    }
    
    /**
     * 验证解析后的问题对象是否有效
     * 
     * @param question 解析后的问题对象
     * @param jsonObject 原始JSON对象
     * @return 验证失败原因，null表示验证通过
     */
    private static String validateQuestion(BaseQuestion question, JSONObject jsonObject) {
        // 首先验证type字段是否存在
        if (!jsonObject.containsKey("type")) {
            return "缺少必需的type字段";
        }
        
        String type = jsonObject.getString("type");
        if (type == null || type.trim().isEmpty()) {
            return "type字段值为空";
        }
        
        if (question instanceof FormQuestion) {
            // 表单问题必须type为"form"且有fields字段
            if (!"form".equals(type)) {
                return "type字段值应为'form'，实际为'" + type + "'";
            }
            if (!jsonObject.containsKey("fields")) {
                return "表单问题缺少fields字段";
            }
            if (((FormQuestion) question).getFields() == null || ((FormQuestion) question).getFields().isEmpty()) {
                return "表单问题的fields字段为空";
            }
        } else if (question instanceof SingleChoiceQuestion) {
            // 单选问题必须type为"single"且有options字段
            if (!"single".equals(type)) {
                return "type字段值应为'single'，实际为'" + type + "'";
            }
            if (!jsonObject.containsKey("options")) {
                return "单选问题缺少options字段";
            }
            if (((SingleChoiceQuestion) question).getOptions() == null || ((SingleChoiceQuestion) question).getOptions().isEmpty()) {
                return "单选问题的options字段为空";
            }
        } else if (question instanceof MultipleChoiceQuestion) {
            // 多选问题必须type为"multi"且有options字段
            if (!"multi".equals(type)) {
                return "type字段值应为'multi'，实际为'" + type + "'";
            }
            if (!jsonObject.containsKey("options")) {
                return "多选问题缺少options字段";
            }
            if (((MultipleChoiceQuestion) question).getOptions() == null || ((MultipleChoiceQuestion) question).getOptions().isEmpty()) {
                return "多选问题的options字段为空";
            }
        } else if (question instanceof InputQuestion) {
            // 输入问题必须type为"input"且有question字段
            if (!"input".equals(type)) {
                return "type字段值应为'input'，实际为'" + type + "'";
            }
            if (!jsonObject.containsKey("question")) {
                return "输入问题缺少question字段";
            }
            if (question.getQuestion() == null || question.getQuestion().trim().isEmpty()) {
                return "输入问题的question字段为空";
            }
        } else {
            return "未知的问题类型: " + question.getClass().getSimpleName();
        }
        
        return null; // 验证通过
    }
    
    /**
     * 解析JSON字符串为指定类型的问题对象
     * 
     * @param jsonStr JSON字符串
     * @param clazz 目标类型
     * @param <T> 问题类型
     * @return 指定类型的问题对象
     * @throws QuestionParseException 解析失败时抛出异常
     */
    public static <T extends BaseQuestion> T parseQuestion(String jsonStr, Class<T> clazz) throws QuestionParseException {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            throw new QuestionParseException("JSON字符串不能为空", jsonStr, "输入为空或null");
        }
        
        try {
            T result = JSON.parseObject(jsonStr, clazz);
            if (result == null) {
                throw new QuestionParseException(
                    "解析为指定类型失败", 
                    jsonStr, 
                    "无法解析为" + clazz.getSimpleName() + "类型"
                );
            }
            return result;
        } catch (JSONException e) {
            throw new QuestionParseException(
                "JSON解析失败", 
                jsonStr, 
                "解析为" + clazz.getSimpleName() + "时发生异常: " + e.getMessage(), 
                e
            );
        }
    }
    
    /**
     * 将问题对象转换为JSON字符串
     * 
     * @param question 问题对象
     * @return JSON字符串
     * @throws QuestionParseException 转换失败时抛出异常
     */
    public static String toJson(BaseQuestion question) throws QuestionParseException {
        if (question == null) {
            throw new QuestionParseException("问题对象不能为空", null, "输入对象为null");
        }
        
        try {
            return JSON.toJSONString(question);
        } catch (Exception e) {
            throw new QuestionParseException(
                "对象转JSON失败", 
                question.toString(), 
                "序列化异常: " + e.getMessage(), 
                e
            );
        }
    }
}