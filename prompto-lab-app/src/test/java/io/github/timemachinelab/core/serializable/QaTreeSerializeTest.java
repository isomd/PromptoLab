package io.github.timemachinelab.core.serializable;

import io.github.timemachinelab.core.qatree.*;
import io.github.timemachinelab.core.question.*;
import io.github.timemachinelab.util.QaTreeSerializeUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class QaTreeSerializeTest {
    
    private QaTreeSerializeUtil qaTreeSerialize;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        qaTreeSerialize = new QaTreeSerializeUtil();
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void testSerializeTreeWithMultipleQuestionTypes() throws Exception {
        // 创建根节点 - 输入问题
        InputQuestion rootQuestion = new InputQuestion();
        rootQuestion.setQuestion("请输入您的基本信息");
        rootQuestion.setAnswer("用户基本信息收集");
        QaTreeNode rootNode = new QaTreeNode(rootQuestion);
        
        // 创建输入问题节点 (3个)
        InputQuestion inputQ1 = new InputQuestion();
        inputQ1.setQuestion("请输入您的姓名");
        inputQ1.setAnswer("张三");
        QaTreeNode inputNode1 = new QaTreeNode(inputQ1);
        
        InputQuestion inputQ2 = new InputQuestion();
        inputQ2.setQuestion("请输入您的邮箱");
        inputQ2.setAnswer("zhangsan@example.com");
        QaTreeNode inputNode2 = new QaTreeNode(inputQ2);
        
        InputQuestion inputQ3 = new InputQuestion();
        inputQ3.setQuestion("请输入您的电话号码");
        inputQ3.setAnswer("13800138000");
        QaTreeNode inputNode3 = new QaTreeNode(inputQ3);
        
        // 创建单选问题节点 (4个)
        List<Option> genderOptions = Arrays.asList(
            new Option("male", "男"),
            new Option("female", "女")
        );
        
        SingleChoiceQuestion singleQ1 = new SingleChoiceQuestion();
        singleQ1.setQuestion("请选择您的性别");
        singleQ1.setOptions(genderOptions);
        singleQ1.setAnswer(Arrays.asList("male"));
        QaTreeNode singleNode1 = new QaTreeNode(singleQ1);
        
        List<Option> ageOptions = Arrays.asList(
            new Option("18-25", "18-25岁"),
            new Option("26-35", "26-35岁"),
            new Option("36-45", "36-45岁"),
            new Option("46+", "46岁以上")
        );
        
        SingleChoiceQuestion singleQ2 = new SingleChoiceQuestion();
        singleQ2.setQuestion("请选择您的年龄段");
        singleQ2.setOptions(ageOptions);
        singleQ2.setAnswer(Arrays.asList("26-35"));
        QaTreeNode singleNode2 = new QaTreeNode(singleQ2);
        
        List<Option> educationOptions = Arrays.asList(
            new Option("high_school", "高中"),
            new Option("bachelor", "本科"),
            new Option("master", "硕士"),
            new Option("phd", "博士")
        );
        
        SingleChoiceQuestion singleQ3 = new SingleChoiceQuestion();
        singleQ3.setQuestion("请选择您的学历");
        singleQ3.setOptions(educationOptions);
        singleQ3.setAnswer(Arrays.asList("bachelor"));
        QaTreeNode singleNode3 = new QaTreeNode(singleQ3);
        
        SingleChoiceQuestion singleQ4 = new SingleChoiceQuestion();
        singleQ4.setQuestion("请选择您的工作状态");
        singleQ4.setOptions(Arrays.asList(
            new Option("employed", "在职"),
            new Option("unemployed", "待业"),
            new Option("student", "学生")
        ));
        singleQ4.setAnswer(Arrays.asList("employed"));
        QaTreeNode singleNode4 = new QaTreeNode(singleQ4);
        
        // 创建多选问题节点 (3个)
        List<Option> hobbyOptions = Arrays.asList(
            new Option("reading", "阅读"),
            new Option("sports", "运动"),
            new Option("music", "音乐"),
            new Option("travel", "旅行"),
            new Option("cooking", "烹饪")
        );
        
        MultipleChoiceQuestion multiQ1 = new MultipleChoiceQuestion();
        multiQ1.setQuestion("请选择您的兴趣爱好");
        multiQ1.setOptions(hobbyOptions);
        multiQ1.setAnswer(Arrays.asList("reading", "music", "travel"));
        QaTreeNode multiNode1 = new QaTreeNode(multiQ1);
        
        MultipleChoiceQuestion multiQ2 = new MultipleChoiceQuestion();
        multiQ2.setQuestion("请选择您熟悉的编程语言");
        multiQ2.setOptions(Arrays.asList(
            new Option("java", "Java"),
            new Option("python", "Python"),
            new Option("javascript", "JavaScript"),
            new Option("cpp", "C++")
        ));
        multiQ2.setAnswer(Arrays.asList("java", "python"));
        QaTreeNode multiNode2 = new QaTreeNode(multiQ2);
        
        MultipleChoiceQuestion multiQ3 = new MultipleChoiceQuestion();
        multiQ3.setQuestion("请选择您使用过的开发工具");
        multiQ3.setOptions(Arrays.asList(
            new Option("idea", "IntelliJ IDEA"),
            new Option("vscode", "VS Code"),
            new Option("eclipse", "Eclipse"),
            new Option("vim", "Vim")
        ));
        multiQ3.setAnswer(Arrays.asList("idea", "vscode"));
        QaTreeNode multiNode3 = new QaTreeNode(multiQ3);
        
        // 创建表单问题节点 (4个)
        FormQuestion formQ1 = new FormQuestion();
        formQ1.setQuestion("个人详细信息表单");
        formQ1.setFields(Arrays.asList(
            FormField.builder()
                .id("name")
                .question("姓名")
                .type("input")
                .desc("请输入真实姓名")
                .build(),
            FormField.builder()
                .id("gender")
                .question("性别")
                .type("single")
                .options(genderOptions)
                .build()
        ));
        formQ1.setAnswer(Arrays.asList(
            new FormQuestion.AnswerItem("name", Arrays.asList("李四")),
            new FormQuestion.AnswerItem("gender", Arrays.asList("female"))
        ));
        QaTreeNode formNode1 = new QaTreeNode(formQ1);
        
        FormQuestion formQ2 = new FormQuestion();
        formQ2.setQuestion("工作经历表单");
        formQ2.setFields(Arrays.asList(
            FormField.builder()
                .id("company")
                .question("公司名称")
                .type("input")
                .build(),
            FormField.builder()
                .id("position")
                .question("职位")
                .type("input")
                .build()
        ));
        formQ2.setAnswer(Arrays.asList(
            new FormQuestion.AnswerItem("company", Arrays.asList("ABC科技公司")),
            new FormQuestion.AnswerItem("position", Arrays.asList("软件工程师"))
        ));
        QaTreeNode formNode2 = new QaTreeNode(formQ2);
        
        FormQuestion formQ3 = new FormQuestion();
        formQ3.setQuestion("技能评估表单");
        formQ3.setFields(Arrays.asList(
            FormField.builder()
                .id("skill_level")
                .question("技能水平")
                .type("single")
                .options(Arrays.asList(
                    new Option("beginner", "初级"),
                    new Option("intermediate", "中级"),
                    new Option("advanced", "高级")
                ))
                .build()
        ));
        formQ3.setAnswer(Arrays.asList(
            new FormQuestion.AnswerItem("skill_level", Arrays.asList("intermediate"))
        ));
        QaTreeNode formNode3 = new QaTreeNode(formQ3);
        
        FormQuestion formQ4 = new FormQuestion();
        formQ4.setQuestion("联系方式表单");
        formQ4.setFields(Arrays.asList(
            FormField.builder()
                .id("phone")
                .question("电话")
                .type("input")
                .build(),
            FormField.builder()
                .id("email")
                .question("邮箱")
                .type("input")
                .build(),
            FormField.builder()
                .id("contact_time")
                .question("联系时间")
                .type("multi")
                .options(Arrays.asList(
                    new Option("morning", "上午"),
                    new Option("afternoon", "下午"),
                    new Option("evening", "晚上")
                ))
                .build()
        ));
        formQ4.setAnswer(Arrays.asList(
            new FormQuestion.AnswerItem("phone", Arrays.asList("13900139000")),
            new FormQuestion.AnswerItem("email", Arrays.asList("lisi@example.com")),
            new FormQuestion.AnswerItem("contact_time", Arrays.asList("morning", "evening"))
        ));
        QaTreeNode formNode4 = new QaTreeNode(formQ4);
        
        // 构建树结构
        rootNode.append(inputNode1);
        rootNode.append(inputNode2);
        rootNode.append(inputNode3);
        rootNode.append(singleNode1);
        rootNode.append(singleNode2);
        rootNode.append(singleNode3);
        rootNode.append(singleNode4);
        rootNode.append(multiNode1);
        rootNode.append(multiNode2);
        rootNode.append(multiNode3);
        rootNode.append(formNode1);
        rootNode.append(formNode2);
        rootNode.append(formNode3);
        rootNode.append(formNode4);
        
        QaTree tree = new QaTree(rootNode);
        
        // 序列化
        String result = qaTreeSerialize.serialize(tree);
        
        // 验证结果
        assertNotNull(result);
        List<Map<String, Object>> jsonNodes = objectMapper.readValue(result, new TypeReference<List<Map<String, Object>>>(){});
        assertEquals(15, jsonNodes.size()); // 1个根节点 + 3个输入 + 4个单选 + 3个多选 + 4个表单
        
        // 验证根节点
        Map<String, Object> rootJsonNode = jsonNodes.get(0);
        assertEquals(rootNode.getId(), rootJsonNode.get("nodeId"));
        assertNull(rootJsonNode.get("parentId"));
        assertEquals("请输入您的基本信息", rootJsonNode.get("question"));
        assertEquals("用户基本信息收集", rootJsonNode.get("answer"));
        
        // 验证所有子节点都有正确的父ID
        for (int i = 1; i < jsonNodes.size(); i++) {
            Map<String, Object> childJsonNode = jsonNodes.get(i);
            assertEquals(rootNode.getId(), childJsonNode.get("parentId"));
            assertNotNull(childJsonNode.get("nodeId"));
            assertNotNull(childJsonNode.get("question"));
        }
        
        // 验证输入问题的答案格式
        long inputQuestionCount = jsonNodes.stream()
            .skip(1) // 跳过根节点
            .filter(node -> {
                String question = (String) node.get("question");
                String answer = (String) node.get("answer");
                return question.contains("请输入") && !answer.contains("[") && !answer.contains("{");
            })
            .count();
        assertEquals(3, inputQuestionCount);
        
        // 验证单选问题的答案格式（应该是标签，不是ID）
        long singleChoiceCount = jsonNodes.stream()
            .skip(1)
            .filter(node -> {
                String question = (String) node.get("question");
                String answer = (String) node.get("answer");
                return question.contains("请选择") && !answer.contains(",") && 
                       (answer.equals("男") || answer.equals("26-35岁") || answer.equals("本科") || answer.equals("在职"));
            })
            .count();
        assertEquals(4, singleChoiceCount);
        
        // 验证多选问题的答案格式（应该是逗号分隔的标签）
        long multiChoiceCount = jsonNodes.stream()
            .skip(1)
            .filter(node -> {
                String answer = (String) node.get("answer");
                return answer.contains(",") && (answer.contains("阅读") || answer.contains("Java") || answer.contains("IntelliJ IDEA"));
            })
            .count();
        assertEquals(3, multiChoiceCount);
        
        // 验证表单问题的格式（question包含JSON，answer包含JSON）
        long formQuestionCount = jsonNodes.stream()
            .skip(1)
            .filter(node -> {
                String question = (String) node.get("question");
                String answer = (String) node.get("answer");
                return question.contains("表单") && question.contains("[") && answer.contains("[");
            })
            .count();
        assertEquals(4, formQuestionCount);
    }
}