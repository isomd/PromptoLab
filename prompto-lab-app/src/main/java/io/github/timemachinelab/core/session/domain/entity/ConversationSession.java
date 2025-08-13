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
    private String currentNodeId;
    private final LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public ConversationSession(String userId, String initialQuestion) {
        this.sessionId = UUID.randomUUID().toString();
        this.userId = userId;
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        
        CommonQA startQA = new CommonQA();
        startQA.setQuestion(initialQuestion);
        QaTreeNode rootNode = new QaTreeNode(startQA);
        this.qaTree = new QaTree(rootNode);
        this.currentNodeId = rootNode.getId();
    }
    
    public void updateCurrentNode(String nodeId) {
        this.currentNodeId = nodeId;
        this.updateTime = LocalDateTime.now();
    }
    
    public void addUserAnswer(String answer) {
        QaTreeNode currentNode = qaTree.getNodeById(currentNodeId);
        if (currentNode != null && currentNode.getQa() instanceof CommonQA) {
            CommonQA qa = (CommonQA) currentNode.getQa();
            qa.setAnswer(answer);
            qa.updateTimestamp();
            this.updateTime = LocalDateTime.now();
        }
    }
    
    public QaTreeNode getCurrentNode() {
        return qaTree.getNodeById(currentNodeId);
    }
}