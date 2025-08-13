package io.github.timemachinelab.core.session.application;

import io.github.timemachinelab.core.qatree.*;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConversationService {
    
    private final QaTreeDomain qaTreeDomain;
    private final Map<String, ConversationSession> sessions = new ConcurrentHashMap<>();
    
    public ConversationSession createSession(String userId, String initialQuestion) {
        ConversationSession session = new ConversationSession(userId, initialQuestion);
        sessions.put(session.getSessionId(), session);
        return session;
    }
    
    public ConversationSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public void addUserAnswer(String sessionId, String answer) {
        ConversationSession session = sessions.get(sessionId);
        if (session != null) {
            session.addUserAnswer(answer);
        }
    }
    
    public String addAIQuestion(String sessionId, String question) {
        ConversationSession session = sessions.get(sessionId);
        if (session != null) {
            CommonQA newQA = new CommonQA();
            newQA.setQuestion(question);
            
            qaTreeDomain.appendNode(session.getQaTree(), session.getCurrentNodeId(), newQA);
            
            QaTreeNode newNode = findNewlyAddedNode(session, newQA);
            if (newNode != null) {
                session.updateCurrentNode(newNode.getId());
                return newNode.getId();
            }
        }
        return null;
    }
    
    public String addAISelectionQuestion(String sessionId, String question, String[] options) {
        ConversationSession session = sessions.get(sessionId);
        if (session != null) {
            SelectionQA selectionQA = new SelectionQA();
            // selectionQA.setQuestion(question);
            // selectionQA.setOptions(options);
            
            qaTreeDomain.appendNode(session.getQaTree(), session.getCurrentNodeId(), selectionQA);
            
            QaTreeNode newNode = findNewlyAddedNode(session, selectionQA);
            if (newNode != null) {
                session.updateCurrentNode(newNode.getId());
                return newNode.getId();
            }
        }
        return null;
    }
    
    public void handleUserSelection(String sessionId, String questionId, Integer selectedOption) {
        ConversationSession session = sessions.get(sessionId);
        if (session != null) {
            QaTreeNode node = session.getQaTree().getNodeById(questionId);
            if (node != null && node.getQa() instanceof SelectionQA) {
                SelectionQA selectionQA = (SelectionQA) node.getQa();
                // selectionQA.setSelectedOption(selectedOption);
                selectionQA.updateTimestamp();
                session.updateCurrentNode(questionId);
            }
        }
    }
    
    private QaTreeNode findNewlyAddedNode(ConversationSession session, QA targetQA) {
        for (QaTreeNode child : session.getCurrentNode().getChildren().values()) {
            if (child.getQa() == targetQA) {
                return child;
            }
        }
        return null;
    }
}