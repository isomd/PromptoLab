package io.github.timemachinelab.core.session.infrastructure.ai;

import com.alibaba.fastjson2.JSONObject;

import com.suifeng.sfchain.annotation.AIOp;
import com.suifeng.sfchain.core.BaseAIOperation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@AIOp(value = "PromptGenMaster",
        description = "提示词生成大师"
)
@Component
@Slf4j
public class GenPromptOperation  extends BaseAIOperation<GenPromptOperation.GpRequest, GenPromptOperation.GpResponse> {


    @Override
    protected String buildPrompt(GpRequest gpRequest) {

        return JSONObject.toJSONString(gpRequest);
    }

    @Override
    protected GpResponse parseResult(String jsonContent, GpRequest gpRequest) {
        GpResponse gpResponse = new GpResponse();
        gpResponse.setGenPrompt(jsonContent);
        return gpResponse;
    }

    @Data
    public static class GpRequest {

        private String prompt;

        private String user;

        private String aiModel;

        private String userTarget;

        private String userConversation;
    }

    @Data
    public static class GpResponse {
        private String genPrompt;
    }
}
