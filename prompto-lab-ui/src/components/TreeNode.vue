<template>
  <div class="tree-node-container">
    <div 
      class="tree-node"
      :class="{ 
        active: node.id === currentNodeId,
        inactive: !node.isActive,
        user: node.type === 'user',
        assistant: node.type === 'assistant'
      }"
      :style="{ marginLeft: level * 20 + 'px' }"
      @click="selectNode"
    >
      <div class="node-content">
        <div class="node-info">
          <span class="node-type">{{ node.type === 'user' ? '👤' : '🤖' }}</span>
          <span class="node-text">{{ truncatedText }}</span>
        </div>
        <div class="node-actions" v-if="node.id !== 'root'">
          <button 
            @click.stop="deleteNode" 
            class="btn-delete"
            :title="`删除此分支`"
          >
            🗑️
          </button>
        </div>
      </div>
      
      <!-- 分支指示器 -->
      <div v-if="node.children.length > 1" class="branch-indicator">
        <span>{{ node.children.length }} 个分支</span>
      </div>
    </div>
    
    <!-- 递归渲染子节点 -->
    <div v-if="node.children.length > 0" class="children">
      <TreeNode 
        v-for="childId in node.children"
        :key="childId"
        :node="conversationTree.get(childId)!"
        :conversation-tree="conversationTree"
        :current-node-id="currentNodeId"
        :level="level + 1"
        @node-selected="$emit('nodeSelected', $event)"
        @branch-deleted="$emit('branchDeleted', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

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
  node: ConversationNode
  conversationTree: Map<string, ConversationNode>
  currentNodeId: string
  level: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  nodeSelected: [nodeId: string]
  branchDeleted: [nodeId: string]
}>()

const truncatedText = computed(() => {
  const maxLength = 30
  return props.node.content.length > maxLength 
    ? props.node.content.substring(0, maxLength) + '...'
    : props.node.content
})

const selectNode = () => {
  emit('nodeSelected', props.node.id)
}

const deleteNode = () => {
  if (confirm(`确定要删除这个分支吗？这将删除该节点及其所有后续对话。`)) {
    emit('branchDeleted', props.node.id)
  }
}
</script>

<style scoped>
.tree-node-container {
  margin-bottom: 4px;
}

.tree-node {
  padding: 8px 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.tree-node:hover {
  background-color: #e9ecef;
  border-color: #adb5bd;
}

.tree-node.active {
  background-color: #007bff;
  color: white;
  border-color: #0056b3;
}

.tree-node.inactive {
  opacity: 0.6;
  background-color: #f8f9fa;
}

.tree-node.user {
  border-left: 4px solid #28a745;
}

.tree-node.assistant {
  border-left: 4px solid #007bff;
}

.node-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.node-info {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 8px;
}

.node-type {
  font-size: 14px;
}

.node-text {
  font-size: 12px;
  line-height: 1.4;
  flex: 1;
}

.node-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.tree-node:hover .node-actions {
  opacity: 1;
}

.btn-delete {
  padding: 2px 4px;
  font-size: 12px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  background: transparent;
  transition: background-color 0.2s;
}

.btn-delete:hover {
  background-color: rgba(220, 53, 69, 0.1);
}

.branch-indicator {
  margin-top: 4px;
  font-size: 10px;
  color: #6c757d;
  font-style: italic;
}

.children {
  margin-left: 12px;
  border-left: 1px dashed #dee2e6;
  padding-left: 8px;
}
</style>