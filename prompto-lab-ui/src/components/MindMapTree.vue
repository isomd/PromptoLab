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
import { ref, computed, onMounted, watch } from 'vue'

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

// 节点布局计算
const layoutNodes = computed(() => {
  const nodes: LayoutNode[] = []
  const nodeWidth = 180
  const nodeHeight = 40
  const levelGap = 200
  const siblingGap = 60
  
  // 计算每个节点的位置
  const calculateLayout = (nodeId: string, level: number, siblingIndex: number, parentX?: number, parentY?: number) => {
    const node = props.conversationTree.get(nodeId)
    if (!node) return
    
    const truncatedText = node.content.length > 20 
      ? node.content.substring(0, 20) + '...'
      : node.content
    
    let x: number
    let y: number
    
    if (level === 0) {
      // 根节点居中
      x = svgWidth.value / 2 - nodeWidth / 2
      y = 50
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

// 自动调整SVG尺寸
watch(layoutNodes, () => {
  if (layoutNodes.value.length > 0) {
    const maxX = Math.max(...layoutNodes.value.map(n => n.x + n.width))
    const maxY = Math.max(...layoutNodes.value.map(n => n.y + n.height))
    
    svgWidth.value = Math.max(800, maxX + 50)
    svgHeight.value = Math.max(600, maxY + 50)
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
  // 初始化时调整容器大小
  if (mindMapRef.value) {
    const rect = mindMapRef.value.getBoundingClientRect()
    svgWidth.value = Math.max(800, rect.width)
    svgHeight.value = Math.max(600, rect.height)
  }
})
</script>

<style scoped>
.mind-map-container {
  height: 100%;
  padding: 16px;
  background-color: #f8f9fa;
  border-left: 1px solid #e9ecef;
  overflow: hidden;
}

.mind-map-container h3 {
  margin: 0 0 16px 0;
  color: #495057;
  font-size: 16px;
}

.mind-map {
  width: 100%;
  height: calc(100% - 50px);
  overflow: auto;
  background: white;
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

.mind-map-svg {
  display: block;
  background: radial-gradient(circle, #f8f9fa 1px, transparent 1px);
  background-size: 20px 20px;
}

/* 连接线样式 */
.connection-line {
  fill: none;
  stroke: #dee2e6;
  stroke-width: 2;
  transition: all 0.3s ease;
}

.connection-line.active {
  stroke: #007bff;
  stroke-width: 3;
}

/* 节点样式 */
.node-group {
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 移除会导致位移的scale变换 */
.node-group:hover {
  /* transform: scale(1.05); 移除这行 */
}

.node-group.inactive {
  opacity: 0.5;
}

/* 悬停效果背景 */
.hover-bg {
  fill: rgba(0, 123, 255, 0.1);
  stroke: rgba(0, 123, 255, 0.3);
  stroke-width: 2;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.node-group:hover .hover-bg {
  opacity: 1;
}

.node-bg {
  fill: white;
  stroke: #dee2e6;
  stroke-width: 2;
  transition: all 0.3s ease;
}

.node-bg.user {
  fill: #e8f5e8;
  stroke: #28a745;
}

.node-bg.assistant {
  fill: #e3f2fd;
  stroke: #007bff;
}

.node-group.active .node-bg {
  fill: #007bff;
  stroke: #0056b3;
  stroke-width: 3;
}

.node-group.active .node-text {
  fill: white;
  font-weight: bold;
}

.node-group.has-branches .node-bg {
  stroke-width: 3;
  stroke-dasharray: 5,5;
}

/* 悬停时增强节点边框 */
.node-group:hover .node-bg {
  stroke-width: 3;
  stroke: #007bff;
  filter: drop-shadow(0 2px 4px rgba(0, 123, 255, 0.3));
}

.node-icon {
  font-size: 14px;
  fill: #495057;
  transition: all 0.3s ease;
}

.node-group:hover .node-icon {
  font-size: 16px;
}

.node-text {
  font-size: 12px;
  fill: #495057;
  font-family: 'Segoe UI', sans-serif;
  transition: all 0.3s ease;
}

.node-group:hover .node-text {
  font-weight: 600;
}

/* 分支指示器 */
.branch-indicator {
  fill: #ffc107;
  stroke: #e0a800;
  stroke-width: 1;
  transition: all 0.3s ease;
}

.node-group:hover .branch-indicator {
  fill: #ffcd39;
  stroke-width: 2;
}

.branch-count {
  font-size: 10px;
  fill: #212529;
  font-weight: bold;
}

/* 删除按钮 */
.delete-btn {
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;
}

.node-group:hover .delete-btn {
  opacity: 1;
}

.delete-bg {
  fill: #dc3545;
  stroke: #c82333;
  stroke-width: 1;
  transition: all 0.3s ease;
}

.delete-icon {
  font-size: 12px;
  fill: white;
  font-weight: bold;
}

.delete-btn:hover .delete-bg {
  fill: #c82333;
  transform: scale(1.1);
}

/* 滚动条样式 */
.mind-map::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.mind-map::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.mind-map::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.mind-map::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>