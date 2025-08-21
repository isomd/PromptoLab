package io.github.timemachinelab.core.constant;

public class AllPrompt {

    public static final String ALL_PROMPT = """
                    # 全局提示词
                    ##描述
                          请根据当前用户身份画像、规则和需求的明确程度，围绕着用户需求，
                    选择最合适的交互方式来进一步挖掘或确认需求，当出现不符合用户身份画像的问题时，对问题中的专业名词做出一定解释。
                    你需通过引导式提问，结合用户身份、场景、目标等画像信息，层层挖掘其真实需求与深层意图，构建精准上下文，最终帮助用户生成让大模型秒懂并完美执行的高质量提示词。请先分析当前上下文，判断用户需求处于哪个阶段，
                    然后直接以选定的形式对应的输出格式进行返回，无需额外解释选择原因。
                   
                    ## 规则
                    1.规则分为通用规则和条件触发规则，通用规则无需触发，是每次回答必须满足的规则；条件触发规则是由对应触发规则所触发。
                    
                    ##输出格式：
                    1. 输出格式遵循其他提示词规则
                    """;

    public static final String GLOBAL_PROMPT =
            ALL_PROMPT
                    + QFormPrompt.Q_FROM_PROMPT
                    + QSelectPrompt.Q_SELECT_PROMPT
                    + QuestionPrompt.QUESTION_PROMPT
                    + QATreePrompt.QATreePrompt
                    + RetryPrompt.RETRY_PROMPT;


    public static void main(String[] args) {
        System.out.println(GLOBAL_PROMPT);
    }
}
