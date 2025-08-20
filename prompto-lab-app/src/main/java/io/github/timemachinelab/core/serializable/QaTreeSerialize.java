package io.github.timemachinelab.core.serializable;

import io.github.timemachinelab.core.qatree.*;
import io.github.timemachinelab.core.question.*;
import io.github.timemachinelab.core.serializable.QaTreeSerialize.JsonNode.JsonNodeBuilder;
import io.netty.handler.codec.json.JsonObjectDecoder;
import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import dev.langchain4j.internal.Json;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.*;

public class QaTreeSerialize implements Serialize<QaTree> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serialize(QaTree t) throws JsonProcessingException {
        if (t == null || t.getRoot() == null) {
            return "[]";
        }

        List<JsonNode> result = new ArrayList<>();

        firstOrderTraversal(t.getRoot(), null, result);
        
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }
    
    private void firstOrderTraversal(QaTreeNode node, String parentId, List<JsonNode> result) throws JsonProcessingException {
        if (node == null) {
            return;
        }
        
        // 获取子节点列表
        List<QaTreeNode> children = new ArrayList<>();

        if (node.getChildren() != null) {
            children.addAll(node.getChildren().values());
        }
        
        // 访问当前节点
        JsonNode jsonNode = JsonNode.Convert2JsonNode(node, parentId);

        result.add(jsonNode);

        // 先序遍历
        for (QaTreeNode child : children) {
            firstOrderTraversal(child, node.getId(), result);
        }
    }

    @Override
    public QaTree deserialize(String str) {
        throw new UnsupportedOperationException("反序列化功能暂未实现");
    }
    
    /**
     * 根据选项id查找对应的标签
     * @param options 选项列表
     * @param id 选项id
     * @return 选项标签，如果未找到则返回null
     */
    private static String findOptionLabel(List<Option> options, String id) {
        if (options == null || id == null) {
            return null;
        }
        for (Option option : options) {
            if (id.equals(option.getId())) {
                return option.getLabel();
            }
        }
        return null;
    }
    
    @Builder
    static class JsonNode {
        String nodeId;
        String parentId;
        String question;
        String answer;

        static JsonNode Convert2JsonNode(QaTreeNode node, String parentId) throws JsonProcessingException {
            String question = "";
            String answer = "";

            BaseQuestion qa = node.getQa();
            if (qa != null) {
                question = qa.getQuestion() != null ? qa.getQuestion() : "";
                
                // 根据问题类型获取答案
                QuestionType type = QuestionType.fromString(qa.getType());
                if (type != null) {
                    switch (type) {
                        case INPUT:
                            InputQuestion inputQA = (InputQuestion) qa;
                            answer = inputQA.getAnswer() != null ? inputQA.getAnswer() : "";
                            break;
                        case SINGLE:
                            SingleChoiceQuestion singleQA = (SingleChoiceQuestion) qa;
                            if (singleQA.getAnswer() != null && !singleQA.getAnswer().isEmpty()) {
                                List<String> answerLabels = new ArrayList<>();
                                for (String answerId : singleQA.getAnswer()) {
                                    String label = findOptionLabel(singleQA.getOptions(), answerId);
                                    answerLabels.add(label != null ? label : answerId);
                                }
                                answer = String.join(",", answerLabels);
                            }
                            break;
                        case MULTI:
                            MultipleChoiceQuestion multiQA = (MultipleChoiceQuestion) qa;
                            if (multiQA.getAnswer() != null && !multiQA.getAnswer().isEmpty()) {
                                List<String> answerLabels = new ArrayList<>();
                                for (String answerId : multiQA.getAnswer()) {
                                    String label = findOptionLabel(multiQA.getOptions(), answerId);
                                    answerLabels.add(label != null ? label : answerId);
                                }
                                answer = String.join(",", answerLabels);
                            }
                            break;
                        case FORM:
                            FormQuestion formQA = (FormQuestion) qa;
                            // 将FormField转换为TempFormQuestion的JSON格式拼接到question后面
                            if (formQA.getFields() != null && !formQA.getFields().isEmpty()) {
                                List<TempFormQuestion> tempQuestions = formQA.getFields().stream()
                                    .map(field -> {
                                        return TempFormQuestion.builder()
                                            .id(field.getId())
                                            .question(field.getQuestion())
                                            .type(field.getType())
                                            .options(field.getOptions())
                                            .desc(field.getDesc())
                                            .build();
                                    })
                                    .collect(java.util.stream.Collectors.toList());
                                String fieldsJson = JSONObject.toJSONString(tempQuestions);
                                question = (question != null ? question : "") + fieldsJson;
                            }
                            // 将AnswerItem转换为JSON格式作为answer
                            if (formQA.getAnswer() != null && !formQA.getAnswer().isEmpty()) {
                                answer = JSONObject.toJSONString(formQA.getAnswer());
                            }
                            break;
                        default:
                            break;
                    }
                }
            }

            return JsonNode.builder()
                .nodeId(node.getId())
                .parentId(parentId)
                .question(question)
                .answer(answer)
                .build();
        }
    }
    // 临时FormQuestion
    @Data
    @Builder
    static class TempFormQuestion {
        public String id;
        public String question;
        public String type;
        public List<Option> options;
        public String desc;
    }
}
