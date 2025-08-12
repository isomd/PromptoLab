package io.github.timemachinelab.core.qatree;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class QaTreeNode {

    private final String id;

    private Map<String, QaTreeNode> children;

    private QA qa;

    public QaTreeNode(QA qa) {
        this.id = UUID.randomUUID().toString();
        this.children = new HashMap<>();
        this.qa = qa;
    }

    public void append(QA qa) {
        QaTreeNode node = new QaTreeNode(qa);
        this.append(node);
    }

    public void append(QaTreeNode node) {
        children.put(node.getId(), node);
    }

    public QaTreeNode getChild(String id) {
        return children.get(id);
    }

    public boolean hasChild(String id) {
        return children.containsKey(id);
    }

    public void removeChild(String id) {
        children.remove(id);
    }

}
