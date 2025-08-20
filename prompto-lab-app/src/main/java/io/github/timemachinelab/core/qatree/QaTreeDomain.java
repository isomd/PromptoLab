package io.github.timemachinelab.core.qatree;

import io.github.timemachinelab.core.question.BaseQuestion;
import io.github.timemachinelab.core.question.InputQuestion;
import org.springframework.stereotype.Component;

@Component
public class QaTreeDomain {

    public QaTree createTree(String userStartQuestion) {
        InputQuestion startQA = new InputQuestion();
        startQA.setQuestion(userStartQuestion);
        startQA.setAnswer(userStartQuestion);
        QaTreeNode startNode = new QaTreeNode(startQA);

        return new QaTree(startNode);
    }

    public QaTree appendNode(QaTree tree, String parentId, BaseQuestion qa) {
        tree.addNode(parentId, new QaTreeNode(qa));
        return tree;
    }
}
