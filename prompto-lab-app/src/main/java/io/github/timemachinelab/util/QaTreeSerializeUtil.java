package io.github.timemachinelab.util;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.timemachinelab.core.qatree.QaTree;
import io.github.timemachinelab.core.qatree.QaTreeNode;
import io.github.timemachinelab.core.serializable.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class QaTreeSerializeUtil {

    public static String serialize(QaTree t) throws JsonProcessingException {
        if (t == null || t.getRoot() == null) {
            return "[]";
        }

        List<JsonNode> result = new ArrayList<>();

        firstOrderTraversal(t.getRoot(), null, result);

        return JSONObject.toJSONString(result);
    }
    
    private static void firstOrderTraversal(QaTreeNode node, String parentId, List<JsonNode> result) throws JsonProcessingException {
        if (node == null) {
            return;
        }
        
        // 获取子节点列表
        List<QaTreeNode> children = new ArrayList<>();

        if (node.getChildren() != null) {
            children.addAll(node.getChildren().values());
        }
        
        // 访问当前节点
        JsonNode jsonNode = JsonNode.Convert2JsonNode(node, parentId);

        result.add(jsonNode);

        // 先序遍历
        for (QaTreeNode child : children) {
            firstOrderTraversal(child, node.getId(), result);
        }
    }
}
