package io.github.timemachinelab.core.session.infrastructure.web.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String sessionId;
    private String nodeId;
    private String content;

    public enum ContentType {
        FORM,
        TEXT,
        OPTIONS
    }
}