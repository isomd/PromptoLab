package io.github.timemachinelab.core.session.infrastructure.web.dto;

import lombok.Data;

@Data
public class MessageRequest {
    private String sessionId;
    private String content;
    
    public enum MessageType {
        USER_TEXT,
        USER_SELECTION,
        AI_QUESTION,
        AI_SELECTION_QUESTION
    }
}