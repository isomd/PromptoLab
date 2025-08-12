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
      :style="{ marginLeft: level * 24 + 'px' }"
      @click="selectNode"
    >
      <div class="node-content">
        <div class="node-info">
          <div class="node-type-wrapper">
            <span class="node-type">{{ node.type === 'user' ? '👤' : '🤖' }}</span>
          </div>
          <span class="node-text">{{ truncatedText }}</span>
        </div>
        <div class="node-actions" v-if="node.id !== 'root'">
          <button 
            @click.stop="deleteNode" 
            class="btn-delete"
            :title="`删除此分支`"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>
      </div>
      
      <!-- 分支指示器 -->
      <div v-if="node.children.length > 1" class="branch-indicator">
        <div class="branch-badge">
          <span class="branch-count">{{ node.children.length }}</span>
          <span class="branch-text">个分支</span>
        </div>
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
  const maxLength = 35
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
  margin-bottom: 8px;
}

.tree-node {
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(20, 20, 20, 0.9), rgba(30, 30, 30, 0.8));
  border-radius: 12px;
  border: 1px solid rgba(212, 175, 55, 0.2);
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  backdrop-filter: blur(10px);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  overflow: hidden;
}

.tree-node::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.05), transparent);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.tree-node:hover {
  border-color: rgba(212, 175, 55, 0.4);
  transform: translateY(-2px);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(212, 175, 55, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.tree-node:hover::before {
  opacity: 1;
}

.tree-node.active {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2), rgba(244, 208, 63, 0.1));
  border-color: #d4af37;
  color: #ffffff;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 24px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.tree-node.active::before {
  opacity: 1;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(255, 215, 0, 0.05));
}

.tree-node.inactive {
  opacity: 0.5;
  background: linear-gradient(135deg, rgba(15, 15, 15, 0.8), rgba(25, 25, 25, 0.7));
  border-color: rgba(212, 175, 55, 0.1);
}

.tree-node.user {
  border-left: 4px solid #28a745;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.3),
    -4px 0 12px rgba(40, 167, 69, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.tree-node.assistant {
  border-left: 4px solid #d4af37;
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.3),
    -4px 0 12px rgba(212, 175, 55, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.node-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 2;
}

.node-info {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 16px;
}

.node-type-wrapper {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
  transition: all 0.3s ease;
}

.tree-node:hover .node-type-wrapper {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(212, 175, 55, 0.4);
}

.node-type {
  font-size: 16px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.node-text {
  font-size: 14px;
  line-height: 1.5;
  flex: 1;
  color: #e8e8e8;
  font-weight: 500;
  font-family: 'Inter', 'SF Pro Display', sans-serif;
  transition: all 0.3s ease;
}

.tree-node:hover .node-text {
  color: #ffffff;
  font-weight: 600;
}

.tree-node.active .node-text {
  color: #ffffff;
  font-weight: 600;
  text-shadow: 0 0 8px rgba(255, 255, 255, 0.3);
}

.node-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  transition: all 0.3s ease;
  transform: translateX(10px);
}

.tree-node:hover .node-actions {
  opacity: 1;
  transform: translateX(0);
}

.btn-delete {
  padding: 8px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  background: linear-gradient(135deg, rgba(220, 53, 69, 0.8), rgba(200, 35, 51, 0.9));
  color: #ffffff;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
  backdrop-filter: blur(10px);
}

.btn-delete:hover {
  background: linear-gradient(135deg, rgba(220, 53, 69, 1), rgba(200, 35, 51, 1));
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(220, 53, 69, 0.5);
}

.branch-indicator {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.branch-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.2), rgba(244, 208, 63, 0.1));
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 20px;
  padding: 6px 12px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.2);
}

.branch-count {
  font-size: 12px;
  font-weight: 700;
  color: #d4af37;
  text-shadow: 0 0 4px rgba(212, 175, 55, 0.5);
}

.branch-text {
  font-size: 11px;
  color: #b8b8b8;
  font-weight: 500;
}

.children {
  margin-left: 20px;
  border-left: 2px solid rgba(212, 175, 55, 0.2);
  padding-left: 16px;
  position: relative;
}

.children::before {
  content: '';
  position: absolute;
  left: -1px;
  top: 0;
  bottom: 0;
  width: 2px;
  background: linear-gradient(180deg, rgba(212, 175, 55, 0.4), transparent);
  border-radius: 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tree-node {
    padding: 12px 16px;
  }
  
  .node-info {
    gap: 12px;
  }
  
  .node-type-wrapper {
    width: 28px;
    height: 28px;
  }
  
  .node-type {
    font-size: 14px;
  }
  
  .node-text {
    font-size: 13px;
  }
  
  .children {
    margin-left: 16px;
    padding-left: 12px;
  }
}
</style>