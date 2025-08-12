<template>
  <div class="chat-tree">
    <h3>对话树</h3>
    <div class="tree-container">
      <TreeNode 
        v-if="rootNode"
        :node="rootNode"
        :conversation-tree="conversationTree"
        :current-node-id="currentNodeId"
        :level="0"
        @node-selected="$emit('nodeSelected', $event)"
        @branch-deleted="$emit('branchDeleted', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import TreeNode from './TreeNode.vue'

interface ConversationNode {
  id: string
  content: string
  type: 'user' | 'assistant'
  timestamp: Date
  parentId?: string
  children: string[]
  isActive: boolean
}

interface Props {
  conversationTree: Map<string, ConversationNode>
  currentNodeId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  nodeSelected: [nodeId: string]
  branchDeleted: [nodeId: string]
}>()

const rootNode = computed(() => {
  return props.conversationTree.get('root')
})
</script>

<style scoped>
.chat-tree {
  height: 100%;
  padding: 16px;
  background-color: #f8f9fa;
  border-left: 1px solid #e9ecef;
}

.chat-tree h3 {
  margin: 0 0 16px 0;
  color: #495057;
  font-size: 16px;
}

.tree-container {
  max-height: calc(100vh - 120px);
  overflow-y: auto;
}
</style>