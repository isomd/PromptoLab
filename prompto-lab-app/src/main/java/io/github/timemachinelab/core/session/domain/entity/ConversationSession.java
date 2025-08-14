package io.github.timemachinelab.core.session.domain.entity;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Getter
public class ConversationSession {
    
    private final String sessionId;
    private final String userId;
    private final LocalDateTime createTime;
    private LocalDateTime updateTime;
    private final List<ConversationMessage> messages;
    
    public ConversationSession(String userId) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = userId;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.messages = new ArrayList<>();
    }
    
    public void addMessage(String content, String role) {
        ConversationMessage message = new ConversationMessage(content, role);
        this.messages.add(message);
        this.updateTime = LocalDateTime.now();
    }
    
    @Getter
    public static class ConversationMessage {
        private final String content;
        private final String role; // "user" or "assistant"
        private final LocalDateTime timestamp;
        
        public ConversationMessage(String content, String role) {
            this.content = content;
            this.role = role;
            this.timestamp = LocalDateTime.now();
        }
    }
}