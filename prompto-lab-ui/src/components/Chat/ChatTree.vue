<template>
  <div class="chat-tree">
    <!-- 动态背景效果 -->
    <div class="tree-background">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="floating-particles">
        <div class="particle" v-for="i in 8" :key="i"></div>
      </div>
    </div>
    
    <div class="tree-header">
      <div class="header-icon">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <polyline points="3.27,6.96 12,12.01 20.73,6.96" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <line x1="12" y1="22.08" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
      <h3 class="tree-title">对话树</h3>
      <div class="tree-status">
        <span class="status-indicator"></span>
        <span class="status-text">活跃</span>
      </div>
    </div>
    
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
      
      <!-- 空状态 -->
      <div v-if="!rootNode" class="empty-state">
        <div class="empty-icon">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
            <path d="M8 14s1.5 2 4 2 4-2 4-2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="9" y1="9" x2="9.01" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <line x1="15" y1="9" x2="15.01" y2="9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </div>
        <p class="empty-text">暂无对话记录</p>
        <p class="empty-hint">开始新的对话来构建对话树</p>
      </div>
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
  // 动态查找根节点（没有parentId的节点）
  for (const [id, node] of props.conversationTree) {
    if (!node.parentId) {
      return node
    }
  }
  return null
})
</script>

<style scoped>
.chat-tree {
  height: 100%;
  padding: 24px;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  border-left: 1px solid rgba(212, 175, 55, 0.2);
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.tree-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.2;
  animation: float 15s ease-in-out infinite;
}

.orb-1 {
  width: 200px;
  height: 200px;
  background: linear-gradient(45deg, #d4af37, #ffd700);
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 150px;
  height: 150px;
  background: linear-gradient(45deg, #c0c0c0, #ffffff);
  bottom: 30%;
  right: 15%;
  animation-delay: -7s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(20px, -20px) rotate(120deg);
  }
  66% {
    transform: translate(-15px, 15px) rotate(240deg);
  }
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  background: linear-gradient(45deg, #d4af37, #ffd700);
  border-radius: 50%;
  opacity: 0.4;
  animation: particle-float 8s ease-in-out infinite;
  width: 3px;
  height: 3px;
}

.particle:nth-child(odd) {
  animation-duration: 6s;
}

.particle:nth-child(3n) {
  animation-duration: 10s;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10%, 90% {
    opacity: 0.4;
  }
  50% {
    transform: translateY(-60px) rotate(180deg);
    opacity: 0.8;
  }
}

/* 头部样式 */
.tree-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding: 20px 24px;
  background: rgba(15, 15, 15, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(212, 175, 55, 0.2);
  backdrop-filter: blur(20px);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  position: relative;
  z-index: 2;
}

.header-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1a1a1a;
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.3);
  transition: all 0.3s ease;
}

.header-icon:hover {
  transform: scale(1.05);
  box-shadow: 0 12px 32px rgba(212, 175, 55, 0.4);
}

.tree-title {
  flex: 1;
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #d4af37 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.3);
}

.tree-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.status-indicator {
  width: 8px;
  height: 8px;
  background: #28a745;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
  box-shadow: 0 0 8px rgba(40, 167, 69, 0.5);
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.2);
  }
}

.status-text {
  font-size: 12px;
  color: #d4af37;
  font-weight: 600;
}

/* 容器样式 */
.tree-container {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
  padding: 20px;
  background: rgba(15, 15, 15, 0.6);
  border-radius: 16px;
  border: 1px solid rgba(212, 175, 55, 0.1);
  backdrop-filter: blur(20px);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  position: relative;
  z-index: 2;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(244, 208, 63, 0.05));
  border: 2px solid rgba(212, 175, 55, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #d4af37;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(212, 175, 55, 0.1);
}

.empty-text {
  font-size: 18px;
  font-weight: 600;
  color: #e8e8e8;
  margin: 0 0 8px 0;
}

.empty-hint {
  font-size: 14px;
  color: #888;
  margin: 0;
  font-weight: 400;
}

/* 滚动条样式 */
.tree-container::-webkit-scrollbar {
  width: 8px;
}

.tree-container::-webkit-scrollbar-track {
  background: rgba(15, 15, 15, 0.3);
  border-radius: 4px;
}

.tree-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.6), rgba(244, 208, 63, 0.4));
  border-radius: 4px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.tree-container::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.8), rgba(244, 208, 63, 0.6));
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-tree {
    padding: 16px;
  }
  
  .tree-header {
    padding: 16px 20px;
    margin-bottom: 24px;
  }
  
  .header-icon {
    width: 40px;
    height: 40px;
  }
  
  .tree-title {
    font-size: 18px;
  }
  
  .tree-container {
    padding: 16px;
    max-height: calc(100vh - 180px);
  }
  
  .empty-state {
    padding: 40px 20px;
  }
  
  .empty-icon {
    width: 60px;
    height: 60px;
  }
}
</style>