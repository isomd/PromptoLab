package io.github.timemachinelab.core.model;

import com.suifeng.sfchain.annotation.AIOp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@AIOp(value = "p",
        description = "实现一个简单的AI对话功能示例",
        defaultModel = "gpt-5-chat")
@Component
@Slf4j
public class PromptAI {
}
