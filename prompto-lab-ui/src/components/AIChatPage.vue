<template>
  <div class="ai-chat-page">
    <!-- 左侧栏 - 暂时留空 -->
    <div class="left-sidebar">
      <div class="sidebar-placeholder">
        <p>左侧栏</p>
        <p>（预留区域）</p>
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
const containerWidth = ref(1200) // 容器总宽度
const leftSidebarWidth = 250 // 左侧栏固定宽度
const rightSidebarWidth = ref(400) // 右侧栏宽度（可调整）
const minRightSidebarWidth = 300 // 右侧栏最小宽度
const maxRightSidebarWidth = 600 // 右侧栏最大宽度
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
.ai-chat-page {
  display: flex;
  height: 100vh;
  background-color: #ffffff;
  overflow: hidden;
}

.left-sidebar {
  width: 250px;
  background-color: #f8f9fa;
  border-right: 1px solid #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.sidebar-placeholder {
  text-align: center;
  color: #6c757d;
  font-size: 14px;
}

.main-content {
  display: flex;
  flex-direction: column;
  min-width: 400px;
  background-color: white;
}

.resizer {
  width: 6px;
  background-color: #e9ecef;
  cursor: col-resize;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease;
  flex-shrink: 0;
}

.resizer:hover {
  background-color: #007bff;
}

.resizer-line {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 50%;
  width: 1px;
  background-color: #dee2e6;
  transform: translateX(-50%);
}

.resizer:hover .resizer-line {
  background-color: white;
}

.resizer-handle {
  width: 20px;
  height: 40px;
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.resizer:hover .resizer-handle {
  opacity: 1;
}

.resizer-dots {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.resizer-dots span {
  width: 3px;
  height: 3px;
  background-color: #6c757d;
  border-radius: 50%;
}

.resizer:hover .resizer-dots span {
  background-color: white;
}

.right-sidebar {
  background-color: #f8f9fa;
  min-width: 300px;
  max-width: 600px;
  flex-shrink: 0;
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
}
</style>