package io.github.timemachinelab.core.session.infrastructure.web.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class MessageResponse {
    private String nodeId;
    private String content;
    private MessageType type;
    private String[] options;
    private Long timestamp;
    
    public enum MessageType {
        USER_ANSWER,
        AI_QUESTION,
        AI_ANSWER,
        SYSTEM_INFO
    }
    
    public static MessageResponse userAnswer(String nodeId, String content) {
        return new MessageResponse(nodeId, content, MessageType.USER_ANSWER, null, System.currentTimeMillis());
    }
    
    public static MessageResponse aiQuestion(String nodeId, String content) {
        return new MessageResponse(nodeId, content, MessageType.AI_QUESTION, null, System.currentTimeMillis());
    }

    public static MessageResponse aiAnswer(String nodeId, String content) {
        return new MessageResponse(nodeId, content, MessageType.AI_ANSWER, null, System.currentTimeMillis());
    }
}