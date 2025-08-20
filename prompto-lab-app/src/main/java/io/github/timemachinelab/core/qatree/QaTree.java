package io.github.timemachinelab.core.qatree;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class QaTree {

    @Getter
    private QaTreeNode root;

    private Map<String, QaTreeNode> nodeMap = new HashMap<>();

    public QaTree(QaTreeNode root) {
        this.root = root;
        nodeMap.put(root.getId(), root);
    }

    public void addNode(String parentId, QaTreeNode node) {
        QaTreeNode parent = nodeMap.get(parentId);
        if (parent == null) {
            return;
        }
        parent.append(node);
        nodeMap.put(node.getId(), node);
    }

    public QaTreeNode getNodeById(String id) {
        return nodeMap.get(id);
    }

}
