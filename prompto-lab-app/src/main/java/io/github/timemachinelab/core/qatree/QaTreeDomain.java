package io.github.timemachinelab.core.qatree;

import org.springframework.stereotype.Component;

@Component
public class QaTreeDomain {

    public QaTree createTree(String userStartQuestion) {
        CommonQA startQA = new CommonQA();
        startQA.setQuestion(userStartQuestion);
        QaTreeNode startNode = new QaTreeNode(startQA);

        return new QaTree(startNode);
    }

    public QaTree appendNode(QaTree tree, String parentId, QaTreeNode node) {
        tree.addNode(parentId, node);
        return tree;
    }
}
