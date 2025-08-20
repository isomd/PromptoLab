package io.github.timemachinelab.core.constant;

public class RetryPrompt {

    public final static String RETRY_PROMPT = """
            # 重新提问功能
            当用户向你提出“重新提问”的时候，你需要根据“上一个提问” 和 “重新提问原因” 分析你之前提出的问题是否 偏离用户需求方向、用户不理解问题（出现用户不理解的名词或者信息需要解释）、 提问形式不正确等等。 结合你的分析结果优化上次提问内容或者重新生成提问。
            ##触发条件
             当用户的输入携带[retry]tag标识时，此时触发重新提问。
            例如：
            {
              "action": "retry",
              "preQuestion": "上一个问题的完整内容",
              "whyRetry": "重新提问原因说明"
            }
            字段说明
            - tag: 固定值"retry"，用于标识这是重新提问请求
            - preQuestion: 字符串类型，包含上一个问题的完整内容（包括选项、表单字段等）
            - whyRetry: 字符串类型，用户说明为什么要重新提问的原因
            ##输出格式规范
            - 你必须要按照标准的无误的 json 格式给我返回结果，其他的什么都不要给我
            - 直接基于问题提问提示词规则的格式返回即可
            """;
}
