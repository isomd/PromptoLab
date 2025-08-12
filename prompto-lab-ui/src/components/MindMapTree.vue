<template>
  <div class="mind-map-container">
    <h3>对话思维导图</h3>
    <div class="mind-map" ref="mindMapRef">
      <svg 
        :width="svgWidth" 
        :height="svgHeight"
        class="mind-map-svg"
      >
        <!-- 连接线 -->
        <g class="connections">
          <path 
            v-for="connection in connections"
            :key="connection.id"
            :d="connection.path"
            class="connection-line"
            :class="{ active: connection.isActive }"
          />
        </g>
        
        <!-- 节点 -->
        <g class="nodes">
          <g 
            v-for="node in layoutNodes"
            :key="node.id"
            :transform="`translate(${node.x}, ${node.y})`"
            class="node-group"
            :class="{ 
              active: node.id === currentNodeId,
              inactive: !node.isActive,
              'has-branches': node.children.length > 1
            }"
            @click="selectNode(node.id)"
          >
            <!-- 节点背景 -->
            <rect 
              :width="node.width"
              :height="node.height"
              :rx="8"
              :ry="8"
              class="node-bg"
              :class="node.type"
            />
            
            <!-- 悬停效果背景 -->
            <rect 
              :width="node.width + 4"
              :height="node.height + 4"
              :x="-2"
              :y="-2"
              :rx="10"
              :ry="10"
              class="hover-bg"
            />
            
            <!-- 节点图标 -->
            <text 
              :x="12"
              :y="20"
              class="node-icon"
            >
              {{ node.type === 'user' ? '👤' : '🤖' }}
            </text>
            
            <!-- 节点文本 -->
            <text 
              :x="35"
              :y="16"
              class="node-text"
            >
              {{ node.truncatedText }}
            </text>
            
            <!-- 分支数量指示器 -->
            <circle 
              v-if="node.children.length > 1"
              :cx="node.width - 15"
              :cy="15"
              r="10"
              class="branch-indicator"
            />
            <text 
              v-if="node.children.length > 1"
              :x="node.width - 15"
              :y="19"
              class="branch-count"
              text-anchor="middle"
            >
              {{ node.children.length }}
            </text>
            
            <!-- 删除按钮 -->
            <g 
              v-if="node.id !== 'root'"
              class="delete-btn"
              :transform="`translate(${node.width - 25}, 5)`"
              @click.stop="deleteNode(node.id)"
            >
              <circle r="8" class="delete-bg" />
              <text x="0" y="3" text-anchor="middle" class="delete-icon">×</text>
            </g>
          </g>
        </g>
      </svg>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

interface ConversationNode {
  id: string
  content: string
  type: 'user' | 'assistant'
  timestamp: Date
  parentId?: string
  children: string[]
  isActive: boolean
}

interface LayoutNode extends ConversationNode {
  x: number
  y: number
  width: number
  height: number
  level: number
  truncatedText: string
}

interface Connection {
  id: string
  path: string
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

const mindMapRef = ref<HTMLElement>()
const svgWidth = ref(800)
const svgHeight = ref(600)
const containerWidth = ref(0) // 新增：容器宽度

// 监听容器尺寸变化
const updateContainerSize = () => {
  if (mindMapRef.value) {
    const rect = mindMapRef.value.getBoundingClientRect()
    const newWidth = rect.width
    const newHeight = rect.height
    
    // 只有当尺寸真正发生变化时才更新
    if (Math.abs(containerWidth.value - newWidth) > 5) {
      containerWidth.value = newWidth
      // 立即更新SVG宽度，确保响应性
      svgWidth.value = Math.max(newWidth - 48, 600)
    }
    
    if (Math.abs(svgHeight.value - (newHeight - 100)) > 5) {
      svgHeight.value = Math.max(newHeight - 100, 400)
    }
  }
}

// 使用ResizeObserver监听容器尺寸变化
let resizeObserver: ResizeObserver | null = null

// 节点布局计算 - 优化布局以适应更宽的容器
const layoutNodes = computed(() => {
  const nodes: LayoutNode[] = []
  const nodeWidth = 200 // 增加节点宽度
  const nodeHeight = 45 // 稍微增加节点高度
  const levelGap = Math.max(250, containerWidth.value / 6) // 动态调整层级间距
  const siblingGap = 70 // 增加兄弟节点间距
  
  // 计算每个节点的位置
  const calculateLayout = (nodeId: string, level: number, siblingIndex: number, parentX?: number, parentY?: number) => {
    const node = props.conversationTree.get(nodeId)
    if (!node) return
    
    const truncatedText = node.content.length > 25 
      ? node.content.substring(0, 25) + '...'
      : node.content
    
    let x: number
    let y: number
    
    if (level === 0) {
      // 根节点居中
      x = svgWidth.value / 2 - nodeWidth / 2
      y = 60
    } else {
      // 子节点布局
      const totalSiblings = node.parentId ? 
        (props.conversationTree.get(node.parentId)?.children.length || 1) : 1
      
      x = (parentX || 0) + levelGap
      
      // 垂直分布子节点
      const startY = (parentY || 0) - ((totalSiblings - 1) * siblingGap) / 2
      y = startY + siblingIndex * siblingGap
    }
    
    const layoutNode: LayoutNode = {
      ...node,
      x,
      y,
      width: nodeWidth,
      height: nodeHeight,
      level,
      truncatedText
    }
    
    nodes.push(layoutNode)
    
    // 递归处理子节点
    node.children.forEach((childId, index) => {
      calculateLayout(childId, level + 1, index, x, y + nodeHeight / 2)
    })
  }
  
  const rootNode = props.conversationTree.get('root')
  if (rootNode) {
    calculateLayout('root', 0, 0)
  }
  
  return nodes
})

// 连接线计算
const connections = computed(() => {
  const conns: Connection[] = []
  
  layoutNodes.value.forEach(node => {
    if (node.parentId) {
      const parent = layoutNodes.value.find(n => n.id === node.parentId)
      if (parent) {
        const startX = parent.x + parent.width
        const startY = parent.y + parent.height / 2
        const endX = node.x
        const endY = node.y + node.height / 2
        
        // 创建贝塞尔曲线路径
        const controlX1 = startX + (endX - startX) / 3
        const controlY1 = startY
        const controlX2 = startX + (endX - startX) * 2 / 3
        const controlY2 = endY
        
        const path = `M ${startX} ${startY} C ${controlX1} ${controlY1}, ${controlX2} ${controlY2}, ${endX} ${endY}`
        
        conns.push({
          id: `${parent.id}-${node.id}`,
          path,
          isActive: node.isActive && parent.isActive
        })
      }
    }
  })
  
  return conns
})

// 自动调整SVG尺寸 - 响应布局变化
watch([layoutNodes, containerWidth], () => {
  if (layoutNodes.value.length > 0) {
    const maxX = Math.max(...layoutNodes.value.map(n => n.x + n.width))
    const maxY = Math.max(...layoutNodes.value.map(n => n.y + n.height))
    
    svgWidth.value = Math.max(containerWidth.value - 48, maxX + 100)
    svgHeight.value = Math.max(600, maxY + 100)
  }
}, { immediate: true })

const selectNode = (nodeId: string) => {
  emit('nodeSelected', nodeId)
}

const deleteNode = (nodeId: string) => {
  if (confirm('确定要删除这个分支吗？这将删除该节点及其所有后续对话。')) {
    emit('branchDeleted', nodeId)
  }
}

onMounted(() => {
  // 初始化尺寸
  nextTick(() => {
    updateContainerSize()
  })
  
  // 使用ResizeObserver监听容器尺寸变化
  if (mindMapRef.value) {
    resizeObserver = new ResizeObserver((entries) => {
      // 使用 requestAnimationFrame 确保在下一帧更新
      requestAnimationFrame(() => {
        updateContainerSize()
      })
    })
    resizeObserver.observe(mindMapRef.value)
  }
  
  // 监听窗口尺寸变化作为备用
  window.addEventListener('resize', () => {
    requestAnimationFrame(updateContainerSize)
  })
})

// 添加强制更新方法
const forceUpdateSize = () => {
  nextTick(() => {
    updateContainerSize()
  })
}

// 监听父组件传入的数据变化，触发尺寸更新
watch(() => props.conversationTree.size, () => {
  nextTick(() => {
    forceUpdateSize()
  })
}, { flush: 'post' })

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
  window.removeEventListener('resize', updateContainerSize)
})
</script>

<style scoped>
/* 主容器 - 优化以支持更大宽度 */
.mind-map-container {
  height: 100%;
  padding: 20px;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  border-left: 1px solid rgba(212, 175, 55, 0.2);
  overflow: hidden;
  position: relative;
  min-width: 1200px;
  width: 100%; /* 确保占满父容器 */
}

.mind-map {
  width: 100%;
  height: calc(100% - 60px);
  overflow: auto;
  background: rgba(15, 15, 15, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(212, 175, 55, 0.2);
  backdrop-filter: blur(20px);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  position: relative;
  z-index: 2;
}

.mind-map-svg {
  display: block;
  min-width: 100%; /* 确保SVG至少占满容器宽度 */
  transition: width 0.3s ease, height 0.3s ease; /* 添加平滑过渡 */
  background: 
    radial-gradient(circle, rgba(212, 175, 55, 0.1) 1px, transparent 1px),
    linear-gradient(135deg, rgba(10, 10, 10, 0.9) 0%, rgba(20, 20, 20, 0.9) 100%);
  background-size: 30px 30px, 100% 100%;
}

/* 添加动态背景效果 */
.mind-map-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(212, 175, 55, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(244, 208, 63, 0.03) 0%, transparent 50%);
  pointer-events: none;
  z-index: 1;
}

.mind-map-container h3 {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #d4af37 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
  z-index: 2;
  text-shadow: 0 0 20px rgba(212, 175, 55, 0.3);
}

.mind-map {
  width: 100%;
  height: calc(100% - 70px);
  overflow: auto;
  background: rgba(15, 15, 15, 0.8);
  border-radius: 16px;
  border: 1px solid rgba(212, 175, 55, 0.2);
  backdrop-filter: blur(20px);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  position: relative;
  z-index: 2;
}

.mind-map-svg {
  display: block;
  background: 
    radial-gradient(circle, rgba(212, 175, 55, 0.1) 1px, transparent 1px),
    linear-gradient(135deg, rgba(10, 10, 10, 0.9) 0%, rgba(20, 20, 20, 0.9) 100%);
  background-size: 30px 30px, 100% 100%;
}
/* 连接线样式 - 黄金主题 */
.connection-line {
  fill: none;
  stroke: rgba(212, 175, 55, 0.4);
  stroke-width: 2;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 0 4px rgba(212, 175, 55, 0.3));
}

.connection-line.active {
  stroke: #d4af37;
  stroke-width: 3;
  filter: drop-shadow(0 0 8px rgba(212, 175, 55, 0.6));
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    filter: drop-shadow(0 0 8px rgba(212, 175, 55, 0.6));
  }
  50% {
    filter: drop-shadow(0 0 16px rgba(212, 175, 55, 0.9));
  }
}

/* 节点样式 - 奢华设计 */
.node-group {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.node-group.inactive {
  opacity: 0.4;
}

/* 悬停效果背景 */
.hover-bg {
  fill: rgba(212, 175, 55, 0.08);
  stroke: rgba(212, 175, 55, 0.3);
  stroke-width: 2;
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 0 12px rgba(212, 175, 55, 0.4));
}

.node-group:hover .hover-bg {
  opacity: 1;
  stroke-width: 3;
}

.node-bg {
  fill: rgba(20, 20, 20, 0.9);
  stroke: rgba(212, 175, 55, 0.3);
  stroke-width: 2;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  filter: drop-shadow(0 4px 16px rgba(0, 0, 0, 0.3));
}

.node-bg.user {
  fill: linear-gradient(135deg, rgba(40, 167, 69, 0.1), rgba(40, 167, 69, 0.05));
  stroke: rgba(40, 167, 69, 0.6);
  filter: drop-shadow(0 0 8px rgba(40, 167, 69, 0.3));
}

.node-bg.assistant {
  fill: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(212, 175, 55, 0.05));
  stroke: rgba(212, 175, 55, 0.6);
  filter: drop-shadow(0 0 8px rgba(212, 175, 55, 0.3));
}

.node-group.active .node-bg {
  fill: linear-gradient(135deg, rgba(212, 175, 55, 0.2), rgba(244, 208, 63, 0.1));
  stroke: #d4af37;
  stroke-width: 3;
  filter: drop-shadow(0 0 20px rgba(212, 175, 55, 0.6));
}

.node-group.active .node-text {
  fill: #ffffff;
  font-weight: 700;
  filter: drop-shadow(0 0 8px rgba(255, 255, 255, 0.5));
}

.node-group.has-branches .node-bg {
  stroke-width: 3;
  stroke-dasharray: 8,4;
  animation: dash-flow 3s linear infinite;
}

@keyframes dash-flow {
  0% {
    stroke-dashoffset: 0;
  }
  100% {
    stroke-dashoffset: 24;
  }
}

/* 悬停时增强节点效果 */
.node-group:hover .node-bg {
  stroke-width: 3;
  stroke: #d4af37;
  filter: drop-shadow(0 0 16px rgba(212, 175, 55, 0.5));
  transform: scale(1.02);
}

.node-icon {
  font-size: 16px;
  fill: #d4af37;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 0 4px rgba(212, 175, 55, 0.4));
}

.node-group:hover .node-icon {
  font-size: 18px;
  filter: drop-shadow(0 0 8px rgba(212, 175, 55, 0.8));
}

.node-text {
  font-size: 13px;
  fill: #e8e8e8;
  font-family: 'Inter', 'SF Pro Display', sans-serif;
  font-weight: 500;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.node-group:hover .node-text {
  font-weight: 600;
  fill: #ffffff;
  filter: drop-shadow(0 0 4px rgba(255, 255, 255, 0.3));
}

/* 分支指示器 - 黄金主题 */
.branch-indicator {
  fill: linear-gradient(135deg, #d4af37, #f4d03f);
  stroke: #b8860b;
  stroke-width: 2;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  filter: drop-shadow(0 0 8px rgba(212, 175, 55, 0.5));
}

.node-group:hover .branch-indicator {
  fill: #ffd700;
  stroke-width: 3;
  filter: drop-shadow(0 0 12px rgba(255, 215, 0, 0.8));
  transform: scale(1.1);
}

.branch-count {
  font-size: 11px;
  fill: #1a1a1a;
  font-weight: 700;
  text-shadow: 0 0 4px rgba(0, 0, 0, 0.5);
}

/* 删除按钮 - 精致设计 */
.delete-btn {
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
}

.node-group:hover .delete-btn {
  opacity: 1;
}

.delete-bg {
  fill: linear-gradient(135deg, #dc3545, #c82333);
  stroke: #a71e2a;
  stroke-width: 2;
  transition: all 0.3s ease;
  filter: drop-shadow(0 0 8px rgba(220, 53, 69, 0.4));
}

.delete-icon {
  font-size: 14px;
  fill: #ffffff;
  font-weight: 700;
  text-shadow: 0 0 4px rgba(0, 0, 0, 0.5);
}

.delete-btn:hover .delete-bg {
  fill: #c82333;
  transform: scale(1.15);
  filter: drop-shadow(0 0 12px rgba(220, 53, 69, 0.7));
}

/* 滚动条样式 - 黄金主题 */
.mind-map::-webkit-scrollbar {
  width: 12px;
  height: 12px;
}

.mind-map::-webkit-scrollbar-track {
  background: rgba(15, 15, 15, 0.5);
  border-radius: 8px;
  border: 1px solid rgba(212, 175, 55, 0.1);
}

.mind-map::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.6), rgba(244, 208, 63, 0.4));
  border-radius: 8px;
  border: 1px solid rgba(212, 175, 55, 0.3);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.mind-map::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.8), rgba(244, 208, 63, 0.6));
  box-shadow: 
    inset 0 1px 0 rgba(255, 255, 255, 0.2),
    0 0 8px rgba(212, 175, 55, 0.4);
}

/* 滚动条角落 */
.mind-map::-webkit-scrollbar-corner {
  background: rgba(15, 15, 15, 0.5);
}
</style>
