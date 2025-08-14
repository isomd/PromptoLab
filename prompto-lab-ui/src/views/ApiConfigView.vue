<template>
  <div class="api-config">
    <!-- 简化的页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <button @click="goBack" class="back-btn">
          <span class="back-icon">←</span>
          <span>返回设置</span>
        </button>
        <div class="header-text">
          <h1>AI配置管理</h1>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧Tab导航栏 (15%) -->
      <div class="sidebar">
        <div class="tab-navigation">
          <div class="tab-header">
            <h3>配置导航</h3>
          </div>
          <div class="tab-list">
            <div
              v-for="tab in tabs"
              :key="tab.key"
              :class="['tab-item', { active: activeTab === tab.key }]"
              @click="switchTab(tab.key)"
            >
              <div class="tab-icon">
                <div class="icon-svg" v-html="tab.icon"></div>
              </div>
              <div class="tab-content">
                <div class="tab-title">{{ tab.title }}</div>
                <div class="tab-description">{{ tab.description }}</div>
              </div>
              <div class="tab-indicator"></div>
            </div>
          </div>
        </div>

        <!-- 系统状态卡片 -->
        <div class="status-card">
          <div class="status-header">
            <span class="status-icon">📊</span>
            <span class="status-title">系统状态</span>
          </div>
          <div class="status-content">
            <div class="status-item">
              <span class="status-label">运行状态</span>
              <span class="status-value running">正常运行</span>
            </div>
            <div class="status-item">
              <span class="status-label">最后更新</span>
              <span class="status-value">{{ formatTime(systemOverview.lastUpdate) }}</span>
            </div>
          </div>
        </div>

        <!-- 紧凑的统计信息卡片 -->
        <div class="stats-card">
          <div class="stats-header">
            <span class="stats-icon">📈</span>
            <span class="stats-title">统计信息</span>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-value">{{ systemOverview.totalModels || 0 }}</span>
              <span class="stat-label">总模型</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ systemOverview.enabledModels || 0 }}</span>
              <span class="stat-label">已启用</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ systemOverview.configuredOperations || 0 }}</span>
              <span class="stat-label">已配置</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分隔线 -->
      <div class="divider"></div>

      <!-- 右侧内容区域 (85%) -->
      <div class="content-area">
        <!-- API信息配置组件 -->
        <ApiInfoConfig
          v-if="activeTab === 'api'"
          :system-overview="systemOverview"
          @update-overview="handleOverviewUpdate"
        />

        <!-- AI节点配置组件 -->
        <AiNodeConfig
          v-if="activeTab === 'operations'"
          :system-overview="systemOverview"
          @update-overview="handleOverviewUpdate"
        />

        <!-- AI调用日志组件 -->
        <AICallLogViewer
          v-if="activeTab === 'logs'"
          :system-overview="systemOverview"
          @update-overview="handleOverviewUpdate"
        />

        <!-- 系统管理组件 -->
        <SystemManagement
          v-if="activeTab === 'system'"
          :system-overview="systemOverview"
          @update-overview="handleOverviewUpdate"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ApiInfoConfig from '@/components/ApiInfoConfig.vue'
import AiNodeConfig from '@/components/AiNodeConfig.vue'
import SystemManagement from '@/components/SystemManagement.vue'
import AICallLogViewer from '@/components/AICallLogViewer.vue'
import { systemApi } from '@/services/systemApi' // 导入系统API服务
import type { SystemOverview } from '@/types/system'

const router = useRouter()

// Tab配置 - 优化后的图标
const tabs = [
  {
    key: 'api',
    title: 'AI模型',
    description: '配置AI模型API',
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
      <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
      <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
      <circle cx="12" cy="7" r="2" fill="currentColor" opacity="0.3"/>
      <circle cx="12" cy="12" r="1.5" fill="currentColor" opacity="0.5"/>
      <circle cx="12" cy="17" r="1" fill="currentColor" opacity="0.7"/>
    </svg>`
  },
  {
    key: 'operations',
    title: 'AI节点',
    description: '管理AI节点映射',
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
      <circle cx="12" cy="12" r="1" fill="currentColor"/>
      <path d="M12 1V6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M12 18V23" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M4.22 4.22L7.76 7.76" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M16.24 16.24L19.78 19.78" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M1 12H6" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M18 12H23" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M4.22 19.78L7.76 16.24" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M16.24 7.76L19.78 4.22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <circle cx="6" cy="6" r="1" fill="currentColor" opacity="0.6"/>
      <circle cx="18" cy="6" r="1" fill="currentColor" opacity="0.6"/>
      <circle cx="6" cy="18" r="1" fill="currentColor" opacity="0.6"/>
      <circle cx="18" cy="18" r="1" fill="currentColor" opacity="0.6"/>
    </svg>`
  },
  {
    key: 'logs',
    title: 'AI日志',
    description: '查看AI调用日志',
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
      <polyline points="14,2 14,8 20,8" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/>
      <line x1="16" y1="13" x2="8" y2="13" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <line x1="16" y1="17" x2="8" y2="17" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <polyline points="10,9 9,9 8,9" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <circle cx="12" cy="13" r="1" fill="currentColor" opacity="0.6"/>
      <circle cx="12" cy="17" r="1" fill="currentColor" opacity="0.6"/>
    </svg>`
  }
]

// 当前激活的Tab
const activeTab = ref('api')

// 系统概览数据
const systemOverview = ref<SystemOverview>({
  totalModels: 0,
  enabledModels: 0,
  configuredOperations: 0,
  totalOperations: 0,
  enabledOperations: 0,  // 添加这个字段
  lastUpdate: Date.now()
})

// API基础URL
const API_BASE = '/sf/api'

// Tab切换
const switchTab = (tabKey: string) => {
  activeTab.value = tabKey
}

// 返回设置页面
const goBack = () => {
  router.push('/settings')
}

// 时间格式化
const formatTime = (timestamp: number) => {
  if (!timestamp) return '未知'
  return new Date(timestamp).toLocaleString()
}

// 获取系统概览 - 使用API服务
const fetchSystemOverview = async () => {
  try {
    const data = await systemApi.getSystemOverview()
    systemOverview.value = data
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '获取系统概览失败'
    console.error('Failed to fetch system overview:', errorMessage)
  }
}

// 处理概览数据更新
const handleOverviewUpdate = (overview: SystemOverview) => {
  systemOverview.value = { ...systemOverview.value, ...overview }
}

// 组件挂载时加载数据
onMounted(() => {
  fetchSystemOverview()
})
</script>

<style scoped>
/* 基础样式 - 黑金主题 */
.api-config {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  color: #ffffff;
  padding: 1rem;
  position: relative;
  overflow-x: hidden;
}

/* 动态背景效果 */
.api-config::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 20% 20%, rgba(212, 175, 55, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 215, 0, 0.08) 0%, transparent 50%),
    radial-gradient(circle at 40% 60%, rgba(184, 134, 11, 0.06) 0%, transparent 50%);
  pointer-events: none;
  z-index: 1;
}

/* 页面头部样式 */
.page-header {
  background: rgba(26, 26, 26, 0.9);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 16px;
  padding: 1rem 2rem;
  margin-bottom: 1.5rem;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(212, 175, 55, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  position: relative;
  z-index: 2;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 2rem;
}

.back-btn {
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #d4af37;
  font-weight: 600;
  cursor: pointer;
  padding: 0.75rem 1.5rem;
  border-radius: 12px;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.back-btn:hover {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.5);
  transform: translateX(-2px);
  box-shadow: 0 4px 16px rgba(212, 175, 55, 0.2);
}

.back-icon {
  font-size: 1.2rem;
  color: #d4af37;
}

.header-text {
  flex: 1;
}

.header-text h1 {
  font-size: 1.8rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff, #d4af37);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  letter-spacing: -0.5px;
}

/* 主要内容区域 */
.main-content {
  display: flex;
  gap: 0;
  height: calc(100vh - 140px);
  position: relative;
  z-index: 2;
}

/* 左侧导航栏 */
.sidebar {
  width: 15%;
  background: rgba(26, 26, 26, 0.9);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 16px 0 0 16px;
  padding: 2rem 1.5rem;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-right: none;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.tab-navigation {
  flex: 1;
}

.tab-header {
  margin-bottom: 1.5rem;
}

.tab-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  background: linear-gradient(135deg, #ffffff, #d4af37);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.tab-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.tab-item {
  position: relative;
  padding: 1rem;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  display: flex;
  align-items: center;
  gap: 1rem;
  background: rgba(255, 255, 255, 0.02);
}

.tab-item:hover {
  background: rgba(212, 175, 55, 0.1);
  border-color: rgba(212, 175, 55, 0.3);
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(212, 175, 55, 0.1);
}

.tab-item.active {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15), rgba(255, 215, 0, 0.1));
  border-color: #d4af37;
  box-shadow:
    0 8px 24px rgba(212, 175, 55, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.tab-icon {
  min-width: 2rem;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg {
  width: 24px;
  height: 24px;
  color: #a0a0a0;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-svg svg {
  width: 100%;
  height: 100%;
  transition: all 0.3s ease;
}

.tab-item:hover .icon-svg {
  color: #d4af37;
  transform: scale(1.1);
}

.tab-item.active .icon-svg {
  color: #d4af37;
  transform: scale(1.15);
}

.tab-item.active .icon-svg svg {
  filter: drop-shadow(0 2px 8px rgba(212, 175, 55, 0.4));
}

.tab-content {
  flex: 1;
}

.tab-title {
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.tab-description {
  font-size: 0.75rem;
  color: #a0a0a0;
  line-height: 1.3;
}

.tab-item.active .tab-title {
  color: #d4af37;
}

.tab-indicator {
  position: absolute;
  right: 0.5rem;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(135deg, #d4af37, #ffd700);
  border-radius: 2px;
  opacity: 0;
  transition: opacity 0.3s ease;
  box-shadow: 0 0 8px rgba(212, 175, 55, 0.5);
}

.tab-item.active .tab-indicator {
  opacity: 1;
}

/* 系统状态卡片 */
.status-card {
  background: rgba(26, 26, 26, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 0.75rem;
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.status-card:hover {
  border-color: rgba(212, 175, 55, 0.4);
  box-shadow:
    0 8px 24px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(212, 175, 55, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.status-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.status-icon {
  font-size: 1rem;
}

.status-title {
  font-weight: 600;
  color: #d4af37;
  font-size: 0.85rem;
}

.status-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-label {
  font-size: 0.75rem;
  color: #a0a0a0;
}

.status-value {
  font-size: 0.75rem;
  font-weight: 600;
  color: #ffffff;
}

.status-value.running {
  color: #d4af37;
  text-shadow: 0 0 8px rgba(212, 175, 55, 0.3);
}

/* 统计信息卡片 */
.stats-card {
  background: rgba(26, 26, 26, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  padding: 1rem;
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.stats-card:hover {
  border-color: rgba(212, 175, 55, 0.4);
  box-shadow:
    0 8px 24px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(212, 175, 55, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.stats-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.stats-icon {
  font-size: 1rem;
}

.stats-title {
  font-weight: 600;
  color: #d4af37;
  font-size: 0.85rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.5rem;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0.75rem;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(212, 175, 55, 0.15);
  border-color: rgba(212, 175, 55, 0.4);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.1);
}

.stat-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #d4af37;
  line-height: 1;
  text-shadow: 0 0 8px rgba(212, 175, 55, 0.3);
}

.stat-label {
  font-size: 0.7rem;
  color: #a0a0a0;
  font-weight: 500;
}

/* 分隔线 */
.divider {
  width: 4px;
  background: linear-gradient(
    45deg,
    rgba(212, 175, 55, 0.3) 0%,
    rgba(255, 215, 0, 0.5) 25%,
    rgba(212, 175, 55, 0.7) 50%,
    rgba(255, 215, 0, 0.5) 75%,
    rgba(212, 175, 55, 0.3) 100%
  );
  position: relative;
  overflow: hidden;
  box-shadow:
    0 0 20px rgba(212, 175, 55, 0.3),
    inset 0 0 20px rgba(255, 215, 0, 0.2);
}

.divider::before {
  content: '';
  position: absolute;
  top: 0;
  left: -2px;
  right: -2px;
  bottom: 0;
  background: linear-gradient(
    45deg,
    transparent 0%,
    rgba(255, 255, 255, 0.8) 25%,
    rgba(255, 255, 255, 0.9) 50%,
    rgba(255, 255, 255, 0.8) 75%,
    transparent 100%
  );
  filter: blur(2px);
  animation: golden-shimmer 3s ease-in-out infinite;
}

.divider::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    rgba(212, 175, 55, 0.1) 0%,
    rgba(255, 215, 0, 0.3) 50%,
    rgba(212, 175, 55, 0.1) 100%
  );
  box-shadow:
    0 0 10px rgba(212, 175, 55, 0.4),
    0 0 20px rgba(255, 215, 0, 0.3),
    0 0 30px rgba(212, 175, 55, 0.2);
}

@keyframes golden-shimmer {
  0%, 100% {
    opacity: 0.5;
    transform: translateY(-100%);
  }
  50% {
    opacity: 1;
    transform: translateY(100%);
  }
}

/* 右侧内容区域 */
.content-area {
  flex: 1;
  width: 85%;
  background: rgba(26, 26, 26, 0.9);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 0 16px 16px 0;
  padding: 2rem;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border-left: none;
  overflow-y: auto;
  position: relative;
}

/* 内容区域背景效果 */
.content-area::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 10% 10%, rgba(212, 175, 55, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 90% 90%, rgba(255, 215, 0, 0.03) 0%, transparent 50%);
  pointer-events: none;
  border-radius: 0 16px 16px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .sidebar {
    width: 20%;
  }

  .content-area {
    width: 80%;
  }

  .tab-title {
    font-size: 0.85rem;
  }

  .tab-description {
    font-size: 0.7rem;
  }
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    height: auto;
  }

  .sidebar {
    width: 100%;
    border-radius: 16px;
    border: 1px solid rgba(212, 175, 55, 0.2);
    margin-bottom: 1rem;
  }

  .divider {
    display: none;
  }

  .content-area {
    width: 100%;
    border-radius: 16px;
    border: 1px solid rgba(212, 175, 55, 0.2);
  }

  .header-content {
    flex-direction: column;
    gap: 1rem;
  }

  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 0.25rem;
  }

  .stat-item {
    flex-direction: column;
    text-align: center;
    padding: 0.5rem;
  }

  .stat-value {
    font-size: 1rem;
    margin-bottom: 0.25rem;
  }

  .stat-label {
    font-size: 0.65rem;
  }
}
</style>
