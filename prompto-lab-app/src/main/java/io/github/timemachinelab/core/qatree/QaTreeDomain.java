package io.github.timemachinelab.core.qatree;

import io.github.timemachinelab.core.question.BaseQuestion;
import io.github.timemachinelab.core.question.InputQuestion;
import io.github.timemachinelab.core.question.SingleChoiceQuestion;
import io.github.timemachinelab.core.question.MultipleChoiceQuestion;
import io.github.timemachinelab.core.question.FormQuestion;
import io.github.timemachinelab.core.session.domain.entity.ConversationSession;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QaTreeDomain {

    public QaTree createTree(String userStartQuestion) {
        InputQuestion startQA = new InputQuestion();
        startQA.setQuestion(userStartQuestion);
        startQA.setAnswer(userStartQuestion);
        QaTreeNode startNode = new QaTreeNode(startQA);

        return new QaTree(startNode);
    }
    
    /**
     * 使用ConversationSession的自增ID创建QaTree
     * @param userStartQuestion 用户开始问题
     * @param session 会话对象，用于获取自增ID
     * @return 创建的QaTree
     */
    public QaTree createTree(String userStartQuestion, ConversationSession session) {
        InputQuestion startQA = new InputQuestion();
        startQA.setQuestion(userStartQuestion);
        startQA.setAnswer(userStartQuestion);
        // 使用会话的自增ID创建根节点
        String rootNodeId = session.getNextNodeId();
        QaTreeNode startNode = new QaTreeNode(startQA, rootNodeId);

        return new QaTree(startNode);
    }

    public QaTree appendNode(QaTree tree, String parentId, BaseQuestion qa) {
        tree.addNode(parentId, new QaTreeNode(qa));
        return tree;
    }
    
    /**
     * 使用ConversationSession的自增ID向QaTree添加节点
     * @param tree QA树
     * @param parentId 父节点ID
     * @param qa 问题对象
     * @param session 会话对象，用于获取自增ID
     * @return 更新后的QaTree
     */
    public QaTree appendNode(QaTree tree, String parentId, BaseQuestion qa, ConversationSession session) {
        String nodeId = session.getNextNodeId();
        tree.addNode(parentId, new QaTreeNode(qa, nodeId));
        return tree;
    }

    /**
     * 更新指定节点的答案
     * @param tree QA树
     * @param nodeId 节点ID
     * @param answer 新的答案内容
     * @return 是否更新成功
     */
    public boolean updateNodeAnswer(QaTree tree, String nodeId, Object answer) {
        QaTreeNode node = tree.getNodeById(nodeId);
        if (node == null) {
            return false;
        }
        
        BaseQuestion qa = node.getQa();
        if (qa == null) {
            return false;
        }
        
        // 根据问题类型设置答案
        if (qa instanceof InputQuestion) {
            ((InputQuestion) qa).setAnswer((String) answer);
        } else if (qa instanceof SingleChoiceQuestion) {
            ((SingleChoiceQuestion) qa).setAnswer((List<String>) answer);
        } else if (qa instanceof MultipleChoiceQuestion) {
            ((MultipleChoiceQuestion) qa).setAnswer((List<String>) answer);
        } else if (qa instanceof FormQuestion) {
            ((FormQuestion) qa).setAnswer((List<FormQuestion.AnswerItem>) answer);
        }
        
        return true;
    }
}
