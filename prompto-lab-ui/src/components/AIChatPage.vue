<template>
  <div class="ai-chat-page">
    <!-- 动态背景效果 -->
    <div class="dynamic-background">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="floating-particles">
        <div class="particle" v-for="i in 15" :key="i"></div>
      </div>
    </div>
    
    <!-- 左侧栏 -->
    <div class="left-sidebar">
      <div class="sidebar-content">
        <div class="sidebar-header">
          <div class="header-icon">
            <div class="icon-wrapper">
              <span class="icon">◈</span>
            </div>
          </div>
          <div class="header-text">
            <h3>AI助手</h3>
            <p>智能对话引导</p>
          </div>
        </div>
        <div class="sidebar-placeholder">
          <div class="placeholder-content">
            <div class="placeholder-icon">⚡</div>
            <p>功能面板</p>
            <span>即将推出</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 中间对话主页面 -->
    <div class="main-content" :style="{ width: mainContentWidth + 'px' }">
      <ChatMain 
        :messages="currentBranchMessages"
        :is-loading="isLoading"
        @send-message="handleSendMessage"
      />
    </div>
    
    <!-- 可拖拽的分隔条 -->
    <div 
      class="resizer"
      @mousedown="startResize"
      @touchstart="startResize"
    >
      <div class="resizer-line"></div>
      <div class="resizer-handle">
        <div class="resizer-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </div>
    </div>
    
    <!-- 右侧思维导图 -->
    <div class="right-sidebar" :style="{ width: rightSidebarWidth + 'px' }">
      <MindMapTree 
        :conversation-tree="conversationTree"
        :current-node-id="currentNodeId"
        @node-selected="handleNodeSelected"
        @branch-deleted="handleBranchDeleted"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import ChatMain from './ChatMain.vue'
import MindMapTree from './MindMapTree.vue'

interface Message {
  id: string
  content: string
  type: 'user' | 'assistant'
  timestamp: Date
}

interface ConversationNode {
  id: string
  content: string
  type: 'user' | 'assistant'
  timestamp: Date
  parentId?: string
  children: string[]
  isActive: boolean
}

// 布局相关的响应式变量
const containerWidth = ref(1500) // 容器总宽度
const leftSidebarWidth = 250 // 左侧栏恢复到原始宽度 250px
const rightSidebarWidth = ref(600) // 右侧栏进一步增加到600px
const minRightSidebarWidth = 500 // 右侧栏最小宽度增加到500px
const maxRightSidebarWidth = 1000 // 右侧栏最大宽度增加到1000px
const resizerWidth = 6 // 分隔条宽度
// 计算中间内容区域宽度
const mainContentWidth = computed(() => {
  return containerWidth.value - leftSidebarWidth - rightSidebarWidth.value - resizerWidth
})

// 拖拽相关变量
const isResizing = ref(false)
const startX = ref(0)
const startRightWidth = ref(0)

// 对话树存储所有节点
const conversationTree = ref<Map<string, ConversationNode>>(new Map())
const currentNodeId = ref<string>('')
const isLoading = ref(false)

// 初始化根节点
const initializeTree = () => {
  const rootNode: ConversationNode = {
    id: 'root',
    content: '您好！我是AI助手，有什么可以帮助您的吗？',
    type: 'assistant',
    timestamp: new Date(),
    children: [],
    isActive: true
  }
  
  conversationTree.value.set('root', rootNode)
  currentNodeId.value = 'root'
}

// 计算当前分支的消息列表
const currentBranchMessages = computed(() => {
  const messages: Message[] = []
  const visited = new Set<string>()
  
  const buildPath = (nodeId: string): string[] => {
    const path: string[] = []
    let current = nodeId
    
    while (current && !visited.has(current)) {
      visited.add(current)
      path.unshift(current)
      const node = conversationTree.value.get(current)
      current = node?.parentId || ''
    }
    
    return path
  }
  
  const path = buildPath(currentNodeId.value)
  
  path.forEach(nodeId => {
    const node = conversationTree.value.get(nodeId)
    if (node) {
      messages.push({
        id: node.id,
        content: node.content,
        type: node.type,
        timestamp: node.timestamp
      })
    }
  })
  
  return messages
})

// 拖拽开始
const startResize = (event: MouseEvent | TouchEvent) => {
  isResizing.value = true
  
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX
  startX.value = clientX
  startRightWidth.value = rightSidebarWidth.value
  
  document.addEventListener('mousemove', handleResize)
  document.addEventListener('mouseup', stopResize)
  document.addEventListener('touchmove', handleResize)
  document.addEventListener('touchend', stopResize)
  
  // 防止文本选择
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'
  
  event.preventDefault()
}

// 拖拽过程中
const handleResize = (event: MouseEvent | TouchEvent) => {
  if (!isResizing.value) return
  
  const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX
  const deltaX = startX.value - clientX // 注意方向：向左拖拽为正值
  const newWidth = startRightWidth.value + deltaX
  
  // 限制在最小和最大宽度之间
  rightSidebarWidth.value = Math.max(
    minRightSidebarWidth,
    Math.min(maxRightSidebarWidth, newWidth)
  )
  
  event.preventDefault()
}

// 拖拽结束
const stopResize = () => {
  isResizing.value = false
  
  document.removeEventListener('mousemove', handleResize)
  document.removeEventListener('mouseup', stopResize)
  document.removeEventListener('touchmove', handleResize)
  document.removeEventListener('touchend', stopResize)
  
  document.body.style.userSelect = ''
  document.body.style.cursor = ''
}

// 更新容器宽度
const updateContainerWidth = () => {
  containerWidth.value = window.innerWidth
}

// 生命周期
onMounted(() => {
  initializeTree()
  updateContainerWidth()
  window.addEventListener('resize', updateContainerWidth)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateContainerWidth)
  stopResize() // 确保清理事件监听器
})

// 其他现有方法保持不变
const handleSendMessage = async (content: string) => {
  const userNodeId = `user_${Date.now()}`
  const userNode: ConversationNode = {
    id: userNodeId,
    content,
    type: 'user',
    timestamp: new Date(),
    parentId: currentNodeId.value,
    children: [],
    isActive: true
  }
  
  const currentNode = conversationTree.value.get(currentNodeId.value)
  if (currentNode) {
    currentNode.children.forEach(childId => {
      const childNode = conversationTree.value.get(childId)
      if (childNode) {
        setNodeAndDescendantsInactive(childId)
      }
    })
    
    currentNode.children.push(userNodeId)
  }
  
  conversationTree.value.set(userNodeId, userNode)
  currentNodeId.value = userNodeId
  
  isLoading.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 2000))
    
    const aiNodeId = `ai_${Date.now()}`
    const aiNode: ConversationNode = {
      id: aiNodeId,
      content: generateAIResponse(content),
      type: 'assistant',
      timestamp: new Date(),
      parentId: userNodeId,
      children: [],
      isActive: true
    }
    
    userNode.children.push(aiNodeId)
    conversationTree.value.set(aiNodeId, aiNode)
    currentNodeId.value = aiNodeId
  } catch (error) {
    console.error('发送消息失败:', error)
  } finally {
    isLoading.value = false
  }
}

const setNodeAndDescendantsInactive = (nodeId: string) => {
  const node = conversationTree.value.get(nodeId)
  if (node) {
    node.isActive = false
    node.children.forEach(childId => {
      setNodeAndDescendantsInactive(childId)
    })
  }
}

const generateAIResponse = (userInput: string): string => {
  const responses = [
    `关于"${userInput}"，这是一个很有趣的问题。让我为您详细解答...`,
    `我理解您询问的是"${userInput}"。根据我的知识，我可以告诉您...`,
    `"${userInput}"确实是一个值得探讨的话题。从多个角度来看...`,
    `感谢您的问题"${userInput}"。我建议我们可以从以下几个方面来分析...`
  ]
  
  return responses[Math.floor(Math.random() * responses.length)]
}

const handleNodeSelected = (nodeId: string) => {
  const targetNode = conversationTree.value.get(nodeId)
  if (targetNode) {
    conversationTree.value.forEach(node => {
      node.isActive = false
    })
    
    const activatePath = (currentNodeId: string) => {
      const node = conversationTree.value.get(currentNodeId)
      if (node) {
        node.isActive = true
        if (node.parentId) {
          activatePath(node.parentId)
        }
      }
    }
    
    activatePath(nodeId)
    currentNodeId.value = nodeId
  }
}

const handleBranchDeleted = (nodeId: string) => {
  const deleteNodeAndDescendants = (id: string) => {
    const node = conversationTree.value.get(id)
    if (node) {
      node.children.forEach(childId => {
        deleteNodeAndDescendants(childId)
      })
      
      if (node.parentId) {
        const parentNode = conversationTree.value.get(node.parentId)
        if (parentNode) {
          parentNode.children = parentNode.children.filter(childId => childId !== id)
        }
      }
      
      conversationTree.value.delete(id)
    }
  }
  
  deleteNodeAndDescendants(nodeId)
  
  if (!conversationTree.value.has(currentNodeId.value)) {
    let newCurrentId = 'root'
    conversationTree.value.forEach((node, id) => {
      if (node.isActive && id !== 'root') {
        newCurrentId = id
      }
    })
    currentNodeId.value = newCurrentId
  }
}
</script>

<style scoped>
/* 主容器 - 黑金风格 */
.ai-chat-page {
  display: flex;
  height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  color: #e8e8e8;
  font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
  overflow: hidden;
  position: relative;
}

/* 动态背景效果 */
.dynamic-background {
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
  filter: blur(60px);
  opacity: 0.2;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: linear-gradient(45deg, #d4af37, #ffd700);
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  background: linear-gradient(45deg, #c0c0c0, #ffffff);
  top: 60%;
  right: 15%;
  animation-delay: -10s;
}

.orb-3 {
  width: 150px;
  height: 150px;
  background: linear-gradient(45deg, #d4af37, #b8860b);
  bottom: 30%;
  left: 60%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
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
  width: 2px;
  height: 2px;
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
    transform: translateY(-100px) rotate(180deg);
    opacity: 0.8;
  }
}

/* 左侧栏 - 恢复原始宽度 */
.left-sidebar {
  width: 250px; /* 恢复到原始的250px宽度 */
  background: rgba(8, 8, 8, 0.9);
  backdrop-filter: blur(24px) saturate(180%);
  border-right: 1px solid rgba(212, 175, 55, 0.15);
  flex-shrink: 0;
  position: relative;
  z-index: 2;
  box-shadow: 
    inset -1px 0 0 rgba(212, 175, 55, 0.1),
    0 0 32px rgba(0, 0, 0, 0.3);
}

.sidebar-content {
  padding: 24px 20px; /* 恢复原始内边距 */
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px; /* 恢复原始间距 */
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 16px; /* 恢复原始间距 */
  padding: 16px; /* 恢复原始内边距 */
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.08), rgba(244, 208, 63, 0.04));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.icon-wrapper {
  width: 48px; /* 恢复到原始的48px */
  height: 48px;
  background: linear-gradient(135deg, #d4af37 0%, #f4d03f 50%, #d4af37 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 8px 24px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.icon {
  font-size: 22px; /* 恢复到原始的22px */
  color: #1a1a1a;
  font-weight: 700;
}

.header-text h3 {
  margin: 0;
  font-size: 18px; /* 恢复到原始的18px */
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #d4af37 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.2;
}

.header-text p {
  margin: 4px 0 0 0;
  font-size: 12px; /* 恢复到原始的12px */
  color: #888;
  font-weight: 500;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.placeholder-content {
  text-align: center;
  padding: 32px 20px; /* 恢复原始内边距 */
  background: rgba(15, 15, 15, 0.6);
  border: 1px solid rgba(212, 175, 55, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.placeholder-icon {
  font-size: 32px; /* 恢复到原始的32px */
  margin-bottom: 16px;
  opacity: 0.6;
}

.placeholder-content p {
  margin: 0 0 8px 0;
  font-size: 16px; /* 恢复到原始的16px */
  font-weight: 600;
  color: #e8e8e8;
}

.placeholder-content span {
  font-size: 12px; /* 恢复到原始的12px */
  color: #888;
  font-weight: 500;
}

/* 右侧栏 - 进一步增加宽度 */
.right-sidebar {
  background: rgba(8, 8, 8, 0.9);
  backdrop-filter: blur(24px) saturate(180%);
  min-width: 600px; /* 从500px进一步增加到600px */
  max-width: 1200px; /* 从900px增加到1200px */
  flex-shrink: 0;
  border-left: 1px solid rgba(212, 175, 55, 0.15);
  position: relative;
  z-index: 2;
  box-shadow: 
    inset 1px 0 0 rgba(212, 175, 55, 0.1),
    0 0 32px rgba(0, 0, 0, 0.3);
}

/* 主内容区域 - 恢复合适的最小宽度 */
.main-content {
  display: flex;
  flex-direction: column;
  min-width: 400px; /* 恢复到400px */
  background: rgba(10, 10, 10, 0.8);
  backdrop-filter: blur(20px);
  position: relative;
  z-index: 2;
}

/* 响应式设计调整 */
@media (max-width: 1200px) {
  .left-sidebar {
    width: 220px; /* 在较小屏幕上适当减少 */
  }
  
  .right-sidebar {
    min-width: 450px;
  }
  
  .main-content {
    min-width: 350px;
  }
}

@media (max-width: 1024px) {
  .left-sidebar {
    width: 200px; /* 更小的屏幕进一步调整 */
  }
  
  .right-sidebar {
    min-width: 400px;
  }
}

.sidebar-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-content {
  text-align: center;
  padding: 32px 20px;
  background: rgba(15, 15, 15, 0.6);
  border: 1px solid rgba(212, 175, 55, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.placeholder-content:hover {
  border-color: rgba(212, 175, 55, 0.3);
  background: rgba(15, 15, 15, 0.8);
}

.placeholder-icon {
  font-size: 32px;
  margin-bottom: 16px;
  opacity: 0.6;
}

.placeholder-content p {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #e8e8e8;
}

.placeholder-content span {
  font-size: 12px;
  color: #888;
  font-weight: 500;
}

/* 主内容区域 */
.main-content {
  display: flex;
  flex-direction: column;
  min-width: 400px;
  background: rgba(10, 10, 10, 0.8);
  backdrop-filter: blur(20px);
  position: relative;
  z-index: 2;
}

/* 分隔条 - 精致设计 */
.resizer {
  width: 6px;
  background: linear-gradient(180deg, rgba(212, 175, 55, 0.1), rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.1));
  cursor: col-resize;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  z-index: 3;
  border-left: 1px solid rgba(212, 175, 55, 0.2);
  border-right: 1px solid rgba(212, 175, 55, 0.2);
}

.resizer:hover {
  background: linear-gradient(180deg, rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.6), rgba(212, 175, 55, 0.3));
  box-shadow: 
    0 0 20px rgba(212, 175, 55, 0.4),
    inset 0 0 20px rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.5);
}

.resizer-line {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(212, 175, 55, 0.6), transparent);
  transform: translateX(-50%);
}

.resizer:hover .resizer-line {
  background: linear-gradient(180deg, transparent, rgba(255, 255, 255, 0.8), transparent);
  box-shadow: 0 0 10px rgba(212, 175, 55, 0.6);
}

.resizer-handle {
  width: 24px;
  height: 48px;
  background: linear-gradient(135deg, rgba(15, 15, 15, 0.9), rgba(25, 25, 25, 0.9));
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  box-shadow: 
    0 4px 16px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.resizer:hover .resizer-handle {
  opacity: 1;
  transform: scale(1.05);
  border-color: rgba(212, 175, 55, 0.6);
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(212, 175, 55, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.resizer-dots {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.resizer-dots span {
  width: 4px;
  height: 4px;
  background: linear-gradient(45deg, #d4af37, #f4d03f);
  border-radius: 50%;
  box-shadow: 0 0 4px rgba(212, 175, 55, 0.6);
  transition: all 0.3s ease;
}

.resizer:hover .resizer-dots span {
  background: linear-gradient(45deg, #ffffff, #d4af37);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.8);
  transform: scale(1.2);
}

/* 右侧栏 */
.right-sidebar {
  background: rgba(8, 8, 8, 0.9);
  backdrop-filter: blur(24px) saturate(180%);
  min-width: 300px;
  max-width: 600px;
  flex-shrink: 0;
  border-left: 1px solid rgba(212, 175, 55, 0.15);
  position: relative;
  z-index: 2;
  box-shadow: 
    inset 1px 0 0 rgba(212, 175, 55, 0.1),
    0 0 32px rgba(0, 0, 0, 0.3);
}

/* 拖拽时的全局样式 */
body.resizing {
  cursor: col-resize !important;
  user-select: none !important;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .left-sidebar {
    width: 200px;
  }
  
  .sidebar-content {
    padding: 24px 16px;
  }
}

@media (max-width: 768px) {
  .ai-chat-page {
    flex-direction: column;
  }
  
  .left-sidebar,
  .right-sidebar {
    width: 100% !important;
    height: 200px;
  }
  
  .main-content {
    width: 100% !important;
    flex: 1;
    min-height: 400px;
  }
  
  .resizer {
    display: none;
  }
  
  .dynamic-background {
    display: none;
  }
}

@media (max-width: 480px) {
  .sidebar-content {
    padding: 16px 12px;
    gap: 20px;
  }
  
  .sidebar-header {
    padding: 16px;
    gap: 12px;
  }
  
  .icon-wrapper {
    width: 40px;
    height: 40px;
  }
  
  .icon {
    font-size: 20px;
  }
}
</style>
