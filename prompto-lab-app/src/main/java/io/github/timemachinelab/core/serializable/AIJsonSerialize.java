package io.github.timemachinelab.core.serializable;

import com.alibaba.fastjson.JSONObject;

import io.github.timemachinelab.core.question.*;

public class AIJsonSerialize implements Serialize<BaseQuestion> {
    @Override
    public String serialize(BaseQuestion t) {
        if (t == null) {
            return null;
        }
        return JSONObject.toJSONString(t);
    }

    @Override
    public BaseQuestion deserialize(String str) {
        JSONObject jsonObject = JSONObject.parseObject(str);
        
        String typeStr = jsonObject.getString("type");
        if (typeStr == null) {
            return null;
        }
        
        QuestionType type = QuestionType.valueOf(typeStr);
        BaseQuestion baseQuestion = null;
        
        switch (type) {
            case INPUT:
                baseQuestion = jsonObject.toJavaObject(InputQuestion.class);
                break;
            case SINGLE:
                baseQuestion = jsonObject.toJavaObject(SingleChoiceQuestion.class);
                break;
            case MULTI:
                baseQuestion = jsonObject.toJavaObject(MultipleChoiceQuestion.class);
                break;
            case FORM:
                baseQuestion = jsonObject.toJavaObject(FormQuestion.class);
                break;
            default:
                throw new RuntimeException("Unknown question type: " + type);
        }
        return baseQuestion;
    }
    

}
