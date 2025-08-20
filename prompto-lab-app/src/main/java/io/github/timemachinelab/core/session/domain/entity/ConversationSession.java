package io.github.timemachinelab.core.session.domain.entity;

import io.github.timemachinelab.core.qatree.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ConversationSession {
    
    private final String sessionId;
    private final String userId;
    private final QaTree qaTree;
    private final LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public ConversationSession(String userId, String sessionId, QaTree qaTree) {
        this.qaTree = qaTree;
        this.sessionId = sessionId;
        this.userId = userId;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}