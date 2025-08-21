<template>
  <div class="api-config">
    <!-- 动态背景 -->
    <div class="background-container">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="floating-particles">
        <div class="particle" v-for="i in 8" :key="i" :style="getParticleStyle(i)"></div>
      </div>
    </div>

    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <button @click="goBack" class="back-btn">
          <div class="btn-background"></div>
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
      <!-- 左侧Tab导航栏 -->
      <div class="sidebar">
        <div class="sidebar-background"></div>
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
                <div v-html="tab.icon"></div>
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
          <div class="card-background"></div>
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

        <!-- 统计信息卡片 -->
        <div class="stats-card">
          <div class="card-background"></div>
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
      <div class="divider">
        <div class="divider-glow"></div>
      </div>

      <!-- 右侧内容区域 -->
      <div class="content-area">
        <div class="content-background"></div>
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
import ApiInfoConfig from '@/components/SFChain/ApiInfoConfig.vue'
import AiNodeConfig from '@/components/Chat/AiNodeConfig.vue'
import SystemManagement from '@/components/SFChain/SystemManagement.vue'
import type { SystemOverview, ApiResponse } from '@/types/system'

const router = useRouter()

// Tab配置
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
    key: 'system',
    title: '系统管理',
    description: '系统维护操作',
    icon: `<svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <circle cx="12" cy="12" r="3" stroke="currentColor" stroke-width="2"/>
      <path d="M19.4 15A1.65 1.65 0 0 0 21 13.09A1.65 1.65 0 0 0 19.4 9" stroke="currentColor" stroke-width="2"/>
      <path d="M4.6 9A1.65 1.65 0 0 0 3 10.91A1.65 1.65 0 0 0 4.6 15" stroke="currentColor" stroke-width="2"/>
      <path d="M15 4.6A1.65 1.65 0 0 0 13.09 3A1.65 1.65 0 0 0 9 4.6" stroke="currentColor" stroke-width="2"/>
      <path d="M9 19.4A1.65 1.65 0 0 0 10.91 21A1.65 1.65 0 0 0 15 19.4" stroke="currentColor" stroke-width="2"/>
      <circle cx="12" cy="12" r="1" fill="currentColor"/>
      <path d="M12 8V7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
      <path d="M12 17V16" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
      <path d="M16 12H17" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
      <path d="M7 12H8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.7"/>
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
  lastUpdate: Date.now()
})

// API基础URL
const API_BASE = '/sf/api'

// 粒子样式生成
const getParticleStyle = (index: number) => {
  const size = Math.random() * 3 + 1
  const left = Math.random() * 100
  const animationDelay = Math.random() * 6
  const animationDuration = Math.random() * 3 + 4

  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${animationDelay}s`,
    animationDuration: `${animationDuration}s`
  }
}

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

// 获取系统概览
const fetchSystemOverview = async () => {
  try {
    const response = await fetch(`${API_BASE}/overview`)
    const result: ApiResponse<SystemOverview> = await response.json()
    if (result.success && result.data) {
      systemOverview.value = result.data
    }
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
.api-config {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  color: #e8e8e8;
  font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.background-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
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
  top: 15%;
  left: 15%;
  animation-delay: 0s;
}

.orb-2 {
  width: 250px;
  height: 250px;
  background: linear-gradient(45deg, #c0c0c0, #ffffff);
  top: 50%;
  right: 15%;
  animation-delay: -10s;
}

.orb-3 {
  width: 180px;
  height: 180px;
  background: linear-gradient(45deg, #d4af37, #b8860b);
  bottom: 25%;
  left: 60%;
  animation-delay: -5s;
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
  animation: particle-float 6s ease-in-out infinite;
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
    transform: translateY(-80px) rotate(180deg);
    opacity: 0.8;
  }
}

/* 页面头部 */
.page-header {
  padding: 40px 40px 0;
  margin-bottom: 40px;
  position: relative;
  z-index: 2;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 32px;
  background: rgba(8, 8, 8, 0.85);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
  padding: 24px 32px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.02),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
}

.header-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
    rgba(212, 175, 55, 0.03) 0%,
    transparent 30%,
    transparent 70%,
    rgba(212, 175, 55, 0.03) 100%
  );
  animation: header-shimmer 8s ease-in-out infinite;
}

@keyframes header-shimmer {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.back-btn {
  background: none;
  border: none;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #d4af37;
  font-weight: 600;
  cursor: pointer;
  padding: 12px 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(212, 175, 55, 0.2);
  z-index: 2;
}

.btn-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.back-btn:hover {
  border-color: rgba(212, 175, 55, 0.4);
  transform: translateX(-4px);
  box-shadow: 0 4px 16px rgba(212, 175, 55, 0.2);
}

.back-btn:hover .btn-background {
  opacity: 1;
}

.back-icon {
  font-size: 1.2rem;
  transition: transform 0.3s ease;
}

.back-btn:hover .back-icon {
  transform: translateX(-2px);
}

.header-text {
  flex: 1;
  z-index: 2;
  position: relative;
}

.header-text h1 {
  font-size: 2rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #d4af37 50%, #f4d03f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 4px 0;
  letter-spacing: -0.5px;
}

.header-text p {
  font-size: 0.95rem;
  color: #a0a0a0;
  margin: 0;
  font-weight: 400;
}

/* 主要内容区域 */
.main-content {
  display: flex;
  gap: 0;
  height: calc(100vh - 200px);
  padding: 0 40px 40px;
  position: relative;
  z-index: 2;
}

/* 左侧导航栏 */
.sidebar {
  width: 320px;
  background: rgba(8, 8, 8, 0.85);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px 0 0 20px;
  padding: 32px 24px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.02),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  border-right: none;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  position: relative;
}

.sidebar-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
    rgba(212, 175, 55, 0.03) 0%,
    transparent 30%,
    transparent 70%,
    rgba(212, 175, 55, 0.03) 100%
  );
  animation: sidebar-shimmer 10s ease-in-out infinite;
}

@keyframes sidebar-shimmer {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.7; }
}

.tab-navigation {
  flex: 1;
  position: relative;
  z-index: 2;
}

.tab-header {
  margin-bottom: 24px;
}

.tab-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0;
}

.tab-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tab-item {
  position: relative;
  padding: 16px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid transparent;
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.02);
}

.tab-item:hover {
  background: rgba(212, 175, 55, 0.1);
  border-color: rgba(212, 175, 55, 0.2);
  transform: translateX(4px);
}

.tab-item.active {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15), rgba(212, 175, 55, 0.05));
  border-color: rgba(212, 175, 55, 0.4);
  box-shadow: 0 4px 16px rgba(212, 175, 55, 0.2);
}

.tab-icon {
  font-size: 1.25rem;
  min-width: 24px;
  text-align: center;
}

.tab-content {
  flex: 1;
}

.tab-title {
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 4px;
  font-size: 0.95rem;
}

.tab-description {
  font-size: 0.8rem;
  color: #a0a0a0;
  line-height: 1.3;
}

.tab-indicator {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  border-radius: 2px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.tab-item.active .tab-indicator {
  opacity: 1;
}

/* 状态和统计卡片 */
.status-card,
.stats-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  position: relative;
  z-index: 2;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.status-card:hover,
.stats-card:hover {
  background: rgba(212, 175, 55, 0.08);
  border-color: rgba(212, 175, 55, 0.2);
}

.card-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.05), transparent);
  border-radius: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.status-card:hover .card-background,
.stats-card:hover .card-background {
  opacity: 1;
}

.status-header,
.stats-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  position: relative;
  z-index: 2;
}

.status-icon,
.stats-icon {
  font-size: 1rem;
}

.status-title,
.stats-title {
  font-weight: 600;
  color: #ffffff;
  font-size: 0.9rem;
}

.status-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
  position: relative;
  z-index: 2;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-label {
  font-size: 0.8rem;
  color: #a0a0a0;
}

.status-value {
  font-size: 0.8rem;
  font-weight: 600;
  color: #ffffff;
}

.status-value.running {
  color: #22c55e;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  position: relative;
  z-index: 2;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: rgba(212, 175, 55, 0.15);
  border-color: rgba(212, 175, 55, 0.3);
  transform: scale(1.02);
}

.stat-value {
  font-size: 1.1rem;
  font-weight: 700;
  color: #d4af37;
  line-height: 1;
}

.stat-label {
  font-size: 0.75rem;
  color: #a0a0a0;
  font-weight: 500;
}

/* 分隔线 */
.divider {
  width: 2px;
  background: linear-gradient(
    45deg,
    rgba(212, 175, 55, 0.2) 0%,
    rgba(212, 175, 55, 0.4) 25%,
    rgba(212, 175, 55, 0.6) 50%,
    rgba(212, 175, 55, 0.4) 75%,
    rgba(212, 175, 55, 0.2) 100%
  );
  position: relative;
  overflow: hidden;
}

.divider-glow {
  position: absolute;
  top: 0;
  left: -1px;
  right: -1px;
  bottom: 0;
  background: linear-gradient(
    45deg,
    transparent 0%,
    rgba(212, 175, 55, 0.3) 25%,
    rgba(212, 175, 55, 0.5) 50%,
    rgba(212, 175, 55, 0.3) 75%,
    transparent 100%
  );
  filter: blur(2px);
  animation: divider-pulse 3s ease-in-out infinite;
}

@keyframes divider-pulse {
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
  background: rgba(8, 8, 8, 0.85);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 0 20px 20px 0;
  padding: 32px;
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.02),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  border-left: none;
  overflow-y: auto;
  position: relative;
}

.content-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
    rgba(212, 175, 55, 0.02) 0%,
    transparent 30%,
    transparent 70%,
    rgba(212, 175, 55, 0.02) 100%
  );
  animation: content-shimmer 12s ease-in-out infinite;
}

@keyframes content-shimmer {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.8; }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .sidebar {
    width: 280px;
  }

  .main-content {
    padding: 0 24px 40px;
  }

  .page-header {
    padding: 40px 24px 0;
  }
}

@media (max-width: 768px) {
  .main-content {
    flex-direction: column;
    height: auto;
    padding: 0 16px 40px;
  }

  .page-header {
    padding: 40px 16px 0;
  }

  .header-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .sidebar {
    width: 100%;
    border-radius: 20px;
    border: 1px solid rgba(212, 175, 55, 0.2);
    margin-bottom: 20px;
  }

  .divider {
    display: none;
  }

  .content-area {
    width: 100%;
    border-radius: 20px;
    border: 1px solid rgba(212, 175, 55, 0.2);
  }

  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .stat-item {
    flex-direction: column;
    text-align: center;
    padding: 8px;
  }

  .stat-value {
    font-size: 1rem;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 0.7rem;
  }
}
</style>
