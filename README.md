# 问题类型系统

基于提供的四种JSON结构体构建的Java问题类型系统，支持单选、多选、输入框和表单四种问题类型。

## 类结构

### 核心类

- **QuestionType**: 问题类型枚举
- **BaseQuestion**: 所有问题类型的抽象基类
- **BaseAnswer**: 所有答案类型的抽象基类
- **Option**: 选项类，用于单选和多选题
- **FormField**: 表单字段类
- **FormAnswerItem**: 表单答案项类
- **QuestionFactory**: 问题工厂类，提供便捷的创建方法
- **AnswerFactory**: 答案工厂类，提供便捷的创建方法

### 具体问题类型

1. **SingleChoiceQuestion**: 单选题
2. **MultipleChoiceQuestion**: 多选题
3. **InputQuestion**: 输入框问题
4. **FormQuestion**: 表单问题（其他问题类型的组合）

### 具体答案类型

1. **ChoiceAnswer**: 选择题答案（单选/多选）
2. **InputAnswer**: 输入框答案
3. **FormAnswer**: 表单答案

## JSON结构对应关系

### 1. 单选题 (SingleChoiceQuestion)
```json
{
  "question": "您的年龄段是？",
  "type": "SINGLE",
  "parentId": null,
  "desc": "请选择您的年龄段",
  "questionId": "age_range_001",
  "options": [
    {"id": "18-25", "label": "18-25岁"},
    {"id": "26-35", "label": "26-35岁"},
    {"id": "36-45", "label": "36-45岁"},
    {"id": "46+", "label": "46岁以上"}
  ],
  "answer": {
    "questionId": "age_range_001",
    "answer": ["26-35"]
  }
}
```

### 2. 多选题 (MultipleChoiceQuestion)
```json
{
  "question": "您感兴趣的技术领域有哪些？",
  "type": "MULTI",
  "parentId": null,
  "desc": "请选择您感兴趣的技术领域（可多选）",
  "questionId": "tech_interests_002",
  "options": [
    {"id": "frontend", "label": "前端开发"},
    {"id": "backend", "label": "后端开发"},
    {"id": "mobile", "label": "移动开发"},
    {"id": "ai", "label": "人工智能"}
  ],
  "answer": {
    "questionId": "tech_interests_002",
    "answer": ["frontend", "backend", "ai"]
  }
}
```

### 3. 输入框 (InputQuestion)
```json
{
  "question": "请描述您的项目需求",
  "type": "INPUT",
  "parentId": null,
  "desc": "请详细描述您希望开发的项目功能和要求",
  "questionId": "project_desc_003",
  "answer": {
    "questionId": "project_desc_003",
    "answer": "需要开发一个电商网站，包含用户注册登录、商品展示、购物车、订单管理等功能"
  }
}
```

### 4. 表单 (FormQuestion)
```json
{
  "question": "请填写用户信息",
  "type": "FORM",
  "parentId": null,
  "desc": "请填写完整的用户信息表单",
  "questionId": "user_info_form_004",
  "fields": [
    {
      "id": "name",
      "question": "姓名",
      "type": "input",
      "options": null,
      "desc": "请输入您的真实姓名",
      "weight": "1.0"
    },
    {
      "id": "gender",
      "question": "性别",
      "type": "single",
      "options": [
        {"id": "male", "label": "男"},
        {"id": "female", "label": "女"}
      ],
      "desc": "请选择您的性别",
      "weight": "0.5"
    },
    {
      "id": "hobbies",
      "question": "兴趣爱好",
      "type": "multi",
      "options": [
        {"id": "reading", "label": "阅读"},
        {"id": "sports", "label": "运动"},
        {"id": "music", "label": "音乐"}
      ],
      "desc": "请选择您的兴趣爱好",
      "weight": "1.5"
    }
  ],
  "answer": {
    "questionId": "user_info_form_004",
    "answer": [
      {
        "id": "name",
        "value": ["张三"]
      },
      {
        "id": "gender",
        "value": ["male"]
      },
      {
        "id": "hobbies",
        "value": ["reading", "sports"]
      }
    ]
  }
}
```

## 使用示例

### 1. 使用工厂方法创建问题

```java
// 创建单选题
SingleChoiceQuestion singleChoice = QuestionFactory.createSingleChoice(
    "您更喜欢哪种编程语言？",
    "conversation_001",
    QuestionFactory.createOptions(
        "java", "Java",
        "python", "Python",
        "javascript", "JavaScript"
    ),
    "请选择您最熟悉的编程语言"
);

// 创建多选题
MultipleChoiceQuestion multipleChoice = QuestionFactory.createMultipleChoice(
    "您使用过哪些开发工具？",
    "conversation_001",
    Arrays.asList(
        new Option("idea", "IntelliJ IDEA"),
        new Option("vscode", "Visual Studio Code"),
        new Option("eclipse", "Eclipse")
    )
);

// 创建输入框问题
InputQuestion inputQuestion = QuestionFactory.createInput(
    "请描述您的项目需求",
    "conversation_001",
    "请详细描述您想要开发的项目功能和要求"
);

// 创建表单问题
FormQuestion formQuestion = QuestionFactory.createForm(
    "请填写项目信息",
    "conversation_001",
    Arrays.asList(
        QuestionFactory.createInputField("project_name", "项目名称"),
        QuestionFactory.createChoiceField(
            "project_type",
            "项目类型",
            "single",
            Arrays.asList(
                new Option("web", "Web应用"),
                new Option("mobile", "移动应用")
            )
        )
    )
);
```

### 2. 使用Builder模式创建问题

```java
// 使用Builder创建单选题
SingleChoiceQuestion question = SingleChoiceQuestion.builder()
    .question("您的工作经验？")
    .parentId("conversation_002")
    .options(Arrays.asList(
        Option.builder().id("junior").label("1-3年").build(),
        Option.builder().id("middle").label("3-5年").build(),
        Option.builder().id("senior").label("5年以上").build()
    ))
    .desc("请选择您的工作经验范围")
    .build();
```

### 3. JSON序列化和反序列化

```java
ObjectMapper objectMapper = new ObjectMapper();

// 序列化为JSON
String json = objectMapper.writeValueAsString(question);

// 从JSON反序列化
BaseQuestion question = objectMapper.readValue(json, BaseQuestion.class);
```

## 特性

1. **类型安全**: 使用枚举定义问题类型，避免字符串错误
2. **JSON支持**: 完全支持Jackson序列化和反序列化
3. **Builder模式**: 支持链式调用，代码更简洁
4. **工厂方法**: 提供便捷的创建方法
5. **多态支持**: 基于抽象基类，支持多态操作
6. **扩展性**: 易于添加新的问题类型

## 注意事项

1. 表单问题中的字段类型必须是 `input`、`single` 或 `multi`
2. 选择类型的字段必须提供 `options` 列表
3. 所有可选字段在JSON序列化时会自动忽略null值
4. 权重字段使用String类型，可以存储数字或其他格式的权重值

## 运行示例

运行 `QuestionExample.main()` 方法可以查看完整的使用示例和JSON输出。