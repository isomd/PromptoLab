<template>
  <div class="log-detail-page">
    <!-- 顶部导航栏 -->
    <div class="top-nav">
      <button @click="handleGoBack" class="nav-back">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        返回日志
      </button>
      <div class="nav-title">
        <h1>调用详情</h1>
        <div class="status-badge" :class="getStatusClass(fullLog?.status)">
          <div class="status-icon"></div>
          {{ getStatusText(fullLog?.status) }}
        </div>
      </div>
    </div>

    <!-- 主内容 -->
    <div class="content-wrapper">
      <div v-if="loading" class="loading-container">
        <div class="spinner-container">
          <div class="spinner"></div>
          <div class="spinner-glow"></div>
        </div>
        <span class="loading-text">加载详情中...</span>
      </div>

      <div v-else-if="fullLog" class="detail-layout">
        <!-- 左侧信息面板 -->
        <div class="info-sidebar">
          <!-- 基本信息 -->
          <div class="info-card">
            <div class="card-title">
              <div class="title-icon">📊</div>
              基本信息
            </div>
            <div class="info-list">
              <div class="info-item">
                <span class="label">调用ID</span>
                <code class="call-id">{{ fullLog.callId }}</code>
              </div>
              <div class="info-item">
                <span class="label">操作类型</span>
                <span class="operation-badge">{{ fullLog.operationType }}</span>
              </div>
              <div class="info-item">
                <span class="label">模型</span>
                <div class="model-info">
                  <div class="provider-icon-wrapper">
                    <img :src="getProviderIcon(getProviderFromModel(fullLog.modelName))"
                         class="provider-icon"
                         :alt="getProviderName(getProviderFromModel(fullLog.modelName))" />
                  </div>
                  <div class="model-details">
                    <div class="model-name">{{ fullLog.modelName }}</div>
                    <div class="provider-name">{{ getProviderName(getProviderFromModel(fullLog.modelName)) }}</div>
                  </div>
                </div>
              </div>
              <div class="info-item">
                <span class="label">调用时间</span>
                <span class="time-value">{{ formatTime(fullLog.callTime) }}</span>
              </div>
              <div class="info-item">
                <span class="label">耗时</span>
                <span class="duration-badge" :class="getDurationClass(fullLog.duration)">
                  <div class="duration-icon"></div>
                  {{ formatDuration(fullLog.duration) }}
                </span>
              </div>
            </div>
          </div>

          <!-- 请求参数 -->
          <div v-if="fullLog.requestParams" class="info-card">
            <div class="card-title">
              <div class="title-icon">⚙️</div>
              请求参数
            </div>
            <div class="params-grid">
              <div class="param-item">
                <div class="param-label">MaxTokens</div>
                <div class="param-value">{{ fullLog.requestParams.maxTokens || '-' }}</div>
              </div>
              <div class="param-item">
                <div class="param-label">Temperature</div>
                <div class="param-value">{{ fullLog.requestParams.temperature || '-' }}</div>
              </div>
              <div class="param-item">
                <div class="param-label">JSON输出</div>
                <div class="param-toggle" :class="fullLog.requestParams.jsonOutput ? 'enabled' : 'disabled'">
                  <div class="toggle-indicator"></div>
                  {{ fullLog.requestParams.jsonOutput ? '启用' : '禁用' }}
                </div>
              </div>
              <div class="param-item">
                <div class="param-label">思考模式</div>
                <div class="param-toggle" :class="fullLog.requestParams.thinking ? 'enabled' : 'disabled'">
                  <div class="toggle-indicator"></div>
                  {{ fullLog.requestParams.thinking ? '启用' : '禁用' }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧内容区域 -->
        <div class="content-area">
          <!-- 内容标签页 -->
          <div class="content-tabs">
            <button
              v-for="tab in availableTabs"
              :key="tab.key"
              @click="activeTab = tab.key"
              :class="['tab-btn', { active: activeTab === tab.key }]"
            >
              <div class="tab-content">
                <span class="tab-label">{{ tab.label }}</span>
                <div v-if="tab.hasContent" class="tab-indicator">
                  <div class="indicator-pulse"></div>
                </div>
              </div>
            </button>
          </div>

          <!-- 内容显示区域 -->
          <div class="content-display">
            <!-- 输入内容 -->
            <div v-if="activeTab === 'input' && fullLog.input" class="content-block">
              <div class="content-header">
                <div class="header-left">
                  <div class="content-icon">📥</div>
                  <h3>输入内容</h3>
                </div>
                <button @click="copyContent(JSON.stringify(fullLog.input, null, 2))" class="copy-btn">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                    <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                  </svg>
                  <span>复制</span>
                </button>
              </div>
              <div class="content-body">
                <JsonViewer
                  v-if="isJsonContent(fullLog.input)"
                  :data="parseJsonContent(fullLog.input)"
                  max-height="500px"
                />
                <pre v-else class="text-content">{{ fullLog.input }}</pre>
              </div>
            </div>

            <!-- 提示词 -->
            <div v-if="activeTab === 'prompt' && fullLog.prompt" class="content-block">
              <div class="content-header">
                <div class="header-left">
                  <div class="content-icon">💭</div>
                  <h3>提示词</h3>
                </div>
                <button @click="copyContent(fullLog.prompt)" class="copy-btn">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                    <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                  </svg>
                  <span>复制</span>
                </button>
              </div>
              <div class="content-body">
                <JsonViewer
                  v-if="isJsonContent(fullLog.prompt)"
                  :data="parseJsonContent(fullLog.prompt)"
                  max-height="500px"
                />
                <pre v-else class="text-content">{{ fullLog.prompt }}</pre>
              </div>
            </div>

            <!-- 原始响应 -->
            <div v-if="activeTab === 'rawResponse' && fullLog.rawResponse" class="content-block">
              <div class="content-header">
                <div class="header-left">
                  <div class="content-icon">🔄</div>
                  <h3>原始响应</h3>
                </div>
                <button @click="copyContent(fullLog.rawResponse)" class="copy-btn">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                    <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                  </svg>
                  <span>复制</span>
                </button>
              </div>
              <div class="content-body">
                <JsonViewer
                  v-if="isJsonContent(fullLog.rawResponse)"
                  :data="parseJsonContent(fullLog.rawResponse)"
                  max-height="500px"
                />
                <pre v-else class="text-content">{{ fullLog.rawResponse }}</pre>
              </div>
            </div>

            <!-- 输出内容 -->
            <div v-if="activeTab === 'output' && fullLog.output" class="content-block">
              <div class="content-header">
                <div class="header-left">
                  <div class="content-icon">📤</div>
                  <h3>输出内容</h3>
                </div>
                <button @click="copyContent(JSON.stringify(fullLog.output, null, 2))" class="copy-btn">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                    <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                  </svg>
                  <span>复制</span>
                </button>
              </div>
              <div class="content-body">
                <JsonViewer
                  v-if="isJsonContent(fullLog.output)"
                  :data="parseJsonContent(fullLog.output)"
                  max-height="500px"
                />
                <pre v-else class="text-content">{{ fullLog.output }}</pre>
              </div>
            </div>

            <!-- 错误信息 -->
            <div v-if="activeTab === 'error' && fullLog.errorMessage" class="content-block error">
              <div class="content-header">
                <div class="header-left">
                  <div class="content-icon error-icon">⚠️</div>
                  <h3>错误信息</h3>
                </div>
                <button @click="copyContent(fullLog.errorMessage)" class="copy-btn">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                    <path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/>
                  </svg>
                  <span>复制</span>
                </button>
              </div>
              <div class="content-body">
                <JsonViewer
                  v-if="isJsonContent(fullLog.errorMessage)"
                  :data="parseJsonContent(fullLog.errorMessage)"
                  max-height="500px"
                />
                <div v-else class="error-content">{{ fullLog.errorMessage }}</div>
              </div>
            </div>

            <!-- 空状态 -->
            <div v-if="!hasActiveContent" class="empty-state">
              <div class="empty-illustration">
                <div class="empty-circle"></div>
                <div class="empty-icon">📄</div>
              </div>
              <p class="empty-text">该标签页暂无内容</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { aiCallLogApi } from '@/services/aiCallLogApi'
import type { AICallLogSummary, AICallLog } from '@/types/system'
import JsonViewer from './JsonViewer.vue'

interface Props {
  log: AICallLogSummary | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  goBack: []
}>()

const fullLog = ref<AICallLog | null>(null)
const loading = ref(false)
const activeTab = ref('input')

// 判断内容是否为JSON格式
const isJsonContent = (content: any): boolean => {
  if (typeof content === 'object') return true
  if (typeof content === 'string') {
    try {
      JSON.parse(content)
      return true
    } catch {
      return false
    }
  }
  return false
}

// 解析JSON内容
const parseJsonContent = (content: any): any => {
  if (typeof content === 'object') return content
  if (typeof content === 'string') {
    try {
      return JSON.parse(content)
    } catch {
      return content
    }
  }
  return content
}

// 可用的标签页
const availableTabs = computed(() => {
  if (!fullLog.value) return []

  const tabs = [
    { key: 'input', label: '输入', hasContent: !!fullLog.value.input },
    { key: 'prompt', label: '提示词', hasContent: !!fullLog.value.prompt },
    { key: 'rawResponse', label: '原始响应', hasContent: !!fullLog.value.rawResponse },
    { key: 'output', label: '输出', hasContent: !!fullLog.value.output },
    { key: 'error', label: '错误', hasContent: !!fullLog.value.errorMessage }
  ]

  return tabs
})

// 监听可用标签页变化，自动切换到第一个有内容的标签
watch(availableTabs, (newTabs) => {
  const firstAvailable = newTabs.find(tab => tab.hasContent)
  if (firstAvailable && !newTabs.find(tab => tab.key === activeTab.value && tab.hasContent)) {
    activeTab.value = firstAvailable.key
  }
}, { immediate: true })

// 当前标签是否有内容
const hasActiveContent = computed(() => {
  const currentTab = availableTabs.value.find(tab => tab.key === activeTab.value)
  return currentTab?.hasContent || false
})

// 加载完整日志详情
const loadFullLog = async () => {
  if (!props.log?.callId) return

  try {
    loading.value = true
    fullLog.value = await aiCallLogApi.getFullLog(props.log.callId)
  } catch (error) {
    console.error('获取日志详情失败:', error)
    alert('获取日志详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 返回按钮处理
const handleGoBack = () => {
  emit('goBack')
}

// 复制内容
const copyContent = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    // 这里可以添加一个更优雅的提示
    alert('已复制到剪贴板')
  } catch (error) {
    console.error('复制失败:', error)
    alert('复制失败，请手动复制')
  }
}

// 辅助函数
const getProviderFromModel = (modelName: string): string => {
  const model = modelName.toLowerCase()
  if (model.includes('gpt') || model.includes('openai')) return 'openai'
  if (model.includes('claude') || model.includes('anthropic')) return 'anthropic'
  if (model.includes('gemini') || model.includes('google')) return 'google'
  if (model.includes('deepseek')) return 'deepseek'
  if (model.includes('doubao') || model.includes('豆包')) return 'doubao'
  if (model.includes('qwen') || model.includes('千问') || model.includes('qianwen')) return 'qianwen'
  return 'other'
}

const getProviderName = (provider: string) => {
  const names: Record<string, string> = {
    'openai': 'OpenAI',
    'anthropic': 'Anthropic',
    'google': 'Google',
    'deepseek': 'DeepSeek',
    'doubao': '豆包',
    'qianwen': '千问',
    'other': '其他'
  }
  return names[provider] || provider
}

const getProviderIcon = (provider: string) => {
  const icons: Record<string, string> = {
    'openai': '/src/assets/icons/openai.svg',
    'anthropic': '/src/assets/icons/anthropic.svg',
    'google': '/src/assets/icons/google.svg',
    'deepseek': '/src/assets/icons/deepseek.svg',
    'doubao': '/src/assets/icons/doubao.svg',
    'qianwen': '/src/assets/icons/qianwen.svg',
    'other': '/src/assets/icons/default.svg'
  }
  return icons[provider] || icons['other']
}

const formatTime = (timeStr: string) => {
  return new Date(timeStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatDuration = (duration: number) => {
  if (duration < 1000) return `${duration}ms`
  return `${(duration / 1000).toFixed(2)}s`
}

const getDurationClass = (duration: number) => {
  if (duration < 2000) return 'fast'
  if (duration < 10000) return 'normal'
  return 'slow'
}

const getStatusClass = (status?: string) => {
  return status === 'SUCCESS' ? 'success' : 'failed'
}

const getStatusText = (status?: string) => {
  return status === 'SUCCESS' ? '成功' : '失败'
}

// 组件挂载时加载完整日志
onMounted(() => {
  loadFullLog()
})
</script>

<style scoped>
.log-detail-page {
  height: 100vh;
  background: radial-gradient(ellipse at top, #0a0a0a 0%, #1a1a1a 50%, #000000 100%);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.log-detail-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.03) 0%, rgba(255, 193, 7, 0.02) 100%);
  pointer-events: none;
}

/* 顶部导航 */
.top-nav {
  background: rgba(26, 26, 26, 0.98);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(255, 215, 0, 0.2);
  padding: 1.5rem 2rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(255, 215, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 10;
}

.nav-back {
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  border: 1px solid rgba(255, 215, 0, 0.3);
  display: flex;
  align-items: center;
  gap: 0.625rem;
  color: #e5e5e5;
  font-weight: 600;
  cursor: pointer;
  padding: 0.75rem 1.25rem;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.4), 0 1px 2px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.nav-back::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 215, 0, 0.2), transparent);
  transition: left 0.5s;
}

.nav-back:hover::before {
  left: 100%;
}

.nav-back:hover {
  background: linear-gradient(135deg, #2a2a2a 0%, #3a3a3a 100%);
  color: #ffd700;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.2), 0 2px 4px rgba(0, 0, 0, 0.4);
}

.nav-back svg {
  width: 18px;
  height: 18px;
  stroke-width: 2.5;
  transition: transform 0.3s ease;
}

.nav-back:hover svg {
  transform: translateX(-2px);
}

.nav-title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.nav-title h1 {
  font-size: 1.5rem;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 50%, #fbbf24 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: -0.025em;
}

.status-badge {
  padding: 0.625rem 1.25rem;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3), 0 1px 3px rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.status-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  opacity: 0.1;
  background: radial-gradient(circle at center, currentColor 0%, transparent 70%);
}

.status-badge.success {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.2) 0%, rgba(22, 163, 74, 0.3) 50%, rgba(21, 128, 61, 0.2) 100%);
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.4);
}

.status-badge.failed {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2) 0%, rgba(220, 38, 38, 0.3) 50%, rgba(185, 28, 28, 0.2) 100%);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.4);
}

.status-icon {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 8px currentColor;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* 内容包装器 */
.content-wrapper {
  flex: 1;
  padding: 2rem;
  overflow: hidden;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1.5rem;
  height: 100%;
  color: #a3a3a3;
  font-weight: 600;
}

.spinner-container {
  position: relative;
  width: 48px;
  height: 48px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 215, 0, 0.1);
  border-top: 4px solid #ffd700;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  position: relative;
  z-index: 2;
}

.spinner-glow {
  position: absolute;
  top: -4px;
  left: -4px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 215, 0, 0.2) 0%, transparent 70%);
  animation: glow 2s ease-in-out infinite alternate;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes glow {
  0% { transform: scale(1); opacity: 0.5; }
  100% { transform: scale(1.1); opacity: 0.8; }
}

.loading-text {
  font-size: 1rem;
  letter-spacing: 0.025em;
}

/* 详情布局 */
.detail-layout {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 2rem;
  height: 100%;
  overflow: hidden;
}

/* 左侧信息栏 */
.info-sidebar {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 215, 0, 0.3) transparent;
}

.info-sidebar::-webkit-scrollbar {
  width: 6px;
}

.info-sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.info-sidebar::-webkit-scrollbar-thumb {
  background: rgba(255, 215, 0, 0.3);
  border-radius: 3px;
}

.info-card {
  background: rgba(26, 26, 26, 0.98);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 16px;
  border: 1px solid rgba(255, 215, 0, 0.2);
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 4px 16px rgba(255, 215, 0, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.info-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 215, 0, 0.4), transparent);
}

.info-card:hover {
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.6), 0 6px 24px rgba(255, 215, 0, 0.2);
  transform: translateY(-2px);
}

/* 信息卡片优化 - 减少内边距 */
.card-title {
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  padding: 0.875rem 1.25rem;
  font-size: 0.8125rem;
  font-weight: 800;
  color: #ffd700;
  border-bottom: 1px solid rgba(255, 215, 0, 0.2);
  letter-spacing: 0.05em;
  display: flex;
  align-items: center;
  gap: 0.625rem;
}

.title-icon {
  font-size: 1rem;
  filter: grayscale(0.2);
}

.info-list {
  padding: 0;
}

.info-item {
  padding: 0.875rem 1.25rem;
  border-bottom: 1px solid rgba(255, 215, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  transition: all 0.3s ease;
  position: relative;
}

.info-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  transition: width 0.3s ease;
}

.info-item:hover::before {
  width: 3px;
}

.info-item:hover {
  background: rgba(255, 215, 0, 0.05);
}

.info-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 0.6875rem;
  color: #a3a3a3;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  line-height: 1.2;
}

.call-id {
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  font-size: 0.75rem;
  background: linear-gradient(135deg, #2a2a2a 0%, #3a3a3a 100%);
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  color: #e5e5e5;
  word-break: break-all;
  border: 1px solid rgba(255, 215, 0, 0.3);
  font-weight: 600;
  position: relative;
  overflow: hidden;
}

.call-id::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, transparent 30%, rgba(255, 215, 0, 0.2) 50%, transparent 70%);
  transform: translateX(-100%);
  transition: transform 0.6s;
}

.call-id:hover::before {
  transform: translateX(100%);
}

.operation-badge {
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.2) 0%, rgba(255, 193, 7, 0.3) 50%, rgba(251, 191, 36, 0.2) 100%);
  color: #ffd700;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 800;
  align-self: flex-start;
  border: 1px solid rgba(255, 215, 0, 0.4);
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.2);
  position: relative;
  overflow: hidden;
}

.operation-badge::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.2), transparent);
  opacity: 0;
  transition: opacity 0.3s;
}

.operation-badge:hover::before {
  opacity: 1;
}

.model-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.provider-icon-wrapper {
  position: relative;
  padding: 4px;
  background: linear-gradient(135deg, #1a1a1a, #2a2a2a);
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.provider-icon {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  object-fit: contain;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.provider-icon-wrapper:hover .provider-icon {
  transform: scale(1.05);
}

.model-details {
  flex: 1;
  min-width: 0;
}

.model-name {
  font-size: 0.875rem;
  font-weight: 800;
  color: #ffffff;
  line-height: 1.4;
  word-break: break-all;
  margin-bottom: 2px;
}

.provider-name {
  font-size: 0.75rem;
  color: #a3a3a3;
  line-height: 1.4;
  font-weight: 600;
}

.time-value {
  font-size: 0.875rem;
  color: #ffffff;
  font-weight: 700;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  letter-spacing: -0.025em;
}

.duration-badge {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.75rem;
  font-weight: 800;
  align-self: flex-start;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
  border: 1px solid;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  gap: 0.5rem;
  position: relative;
  overflow: hidden;
}

.duration-icon {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 6px currentColor;
}

.duration-badge.fast {
  background: rgba(34, 197, 94, 0.2);
  color: #22c55e;
  border-color: rgba(34, 197, 94, 0.4);
}

.duration-badge.normal {
  background: rgba(251, 191, 36, 0.2);
  color: #fbbf24;
  border-color: rgba(251, 191, 36, 0.4);
}

.duration-badge.slow {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.4);
}

/* 参数网格 */
.params-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  background: rgba(255, 215, 0, 0.1);
}

.param-item {
  background: rgba(26, 26, 26, 0.8);
  padding: 1rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  transition: all 0.3s ease;
  position: relative;
}

.param-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 0;
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  transition: width 0.3s ease;
}

.param-item:hover::before {
  width: 2px;
}

.param-item:hover {
  background: rgba(255, 215, 0, 0.05);
}

.param-label {
  font-size: 0.75rem;
  color: #a3a3a3;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.param-value {
  font-size: 0.875rem;
  font-weight: 800;
  color: #ffffff;
  font-family: 'SF Mono', 'Monaco', 'Menlo', monospace;
}

.param-toggle {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  align-self: flex-start;
}

.toggle-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.param-toggle.enabled {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.1);
}

.param-toggle.enabled .toggle-indicator {
  background: #22c55e;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.4);
}

.param-toggle.disabled {
  color: #a3a3a3;
  background: rgba(163, 163, 163, 0.1);
}

.param-toggle.disabled .toggle-indicator {
  background: #737373;
}

/* 右侧内容区域 */
.content-area {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 内容标签页 */
.content-tabs {
  display: flex;
  background: rgba(26, 26, 26, 0.98);
  backdrop-filter: blur(20px) saturate(180%);
  border-radius: 16px 16px 0 0;
  border: 1px solid rgba(255, 215, 0, 0.2);
  border-bottom: none;
  overflow-x: auto;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
  position: relative;
}

.content-tabs::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 215, 0, 0.4), transparent);
}

.tab-btn {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border-bottom: 3px solid transparent;
  position: relative;
  overflow: hidden;
}

.tab-content {
  padding: 1.25rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  white-space: nowrap;
}

.tab-label {
  font-size: 0.875rem;
  font-weight: 700;
  color: #a3a3a3;
  transition: color 0.3s ease;
}

.tab-btn:hover .tab-label {
  color: #e5e5e5;
}

.tab-btn.active .tab-label {
  color: #ffd700;
  font-weight: 800;
}

.tab-btn.active {
  border-bottom-color: #ffd700;
  background: rgba(255, 215, 0, 0.05);
}

.tab-indicator {
  position: relative;
  width: 8px;
  height: 8px;
}

.indicator-pulse {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  border-radius: 50%;
  animation: pulse-glow 2s infinite;
  position: relative;
}

.indicator-pulse::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: radial-gradient(circle, rgba(34, 197, 94, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse-glow 2s infinite;
}

@keyframes pulse-glow {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.7; }
}

/* 内容显示区域 */
.content-display {
  flex: 1;
  background: rgba(26, 26, 26, 0.98);
  backdrop-filter: blur(20px) saturate(180%);
  border: 1px solid rgba(255, 215, 0, 0.2);
  border-radius: 0 0 16px 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.content-block {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.content-block.error {
  border-color: rgba(239, 68, 68, 0.4);
}

.content-header {
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid rgba(255, 215, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.content-icon {
  font-size: 1.125rem;
  filter: grayscale(0.2);
}

.content-icon.error-icon {
  filter: none;
}

.content-header h3 {
  margin: 0;
  font-size: 0.875rem;
  font-weight: 800;
  color: #ffd700;
  letter-spacing: 0.025em;
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  background: linear-gradient(135deg, #1a1a1a 0%, #2a2a2a 100%);
  border: 1px solid rgba(255, 215, 0, 0.3);
  border-radius: 8px;
  padding: 0.625rem 1rem;
  font-size: 0.75rem;
  color: #e5e5e5;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 700;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.4);
  position: relative;
  overflow: hidden;
}

.copy-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 215, 0, 0.2), transparent);
  transition: left 0.5s;
}

.copy-btn:hover::before {
  left: 100%;
}

.copy-btn:hover {
  background: linear-gradient(135deg, #2a2a2a 0%, #3a3a3a 100%);
  border-color: #ffd700;
  color: #ffd700;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.2);
}

.copy-btn svg {
  width: 16px;
  height: 16px;
  stroke-width: 2.5;
  transition: transform 0.3s ease;
}

.copy-btn:hover svg {
  transform: scale(1.1);
}

.content-body {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.code-content,
.text-content {
  width: 100%;
  height: 100%;
  padding: 1.5rem;
  font-family: 'SF Mono', 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.8rem;
  line-height: 1.7;
  color: #e5e5e5;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  white-space: pre-wrap;
  word-break: break-word;
  overflow-y: auto;
  margin: 0;
  border: none;
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 215, 0, 0.3) transparent;
  resize: none;
}

.code-content::-webkit-scrollbar,
.text-content::-webkit-scrollbar {
  width: 8px;
}

.code-content::-webkit-scrollbar-track,
.text-content::-webkit-scrollbar-track {
  background: transparent;
}

.code-content::-webkit-scrollbar-thumb,
.text-content::-webkit-scrollbar-thumb {
  background: rgba(255, 215, 0, 0.3);
  border-radius: 4px;
}

.error-content {
  width: 100%;
  height: 100%;
  padding: 1.5rem;
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(220, 38, 38, 0.1) 100%);
  color: #ef4444;
  font-family: 'SF Mono', 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 0.8rem;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  overflow-y: auto;
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  margin: 0.75rem;
  font-weight: 600;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #a3a3a3;
  gap: 1.5rem;
  padding: 3rem;
}

.empty-illustration {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-circle {
  position: absolute;
  width: 80px;
  height: 80px;
  border: 2px solid rgba(255, 215, 0, 0.2);
  border-radius: 50%;
  animation: rotate 8s linear infinite;
}

.empty-icon {
  font-size: 2.5rem;
  opacity: 0.7;
  filter: grayscale(0.3);
  z-index: 1;
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-text {
  font-size: 0.875rem;
  font-weight: 600;
  margin: 0;
  letter-spacing: 0.025em;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .detail-layout {
    grid-template-columns: 320px 1fr;
  }
}

@media (max-width: 1024px) {
  .detail-layout {
    grid-template-columns: 1fr;
    grid-template-rows: auto 1fr;
  }

  .info-sidebar {
    max-height: 350px;
  }

  .params-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    padding: 1rem;
  }

  .top-nav {
    padding: 1rem 1.5rem;
  }

  .nav-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .detail-layout {
    gap: 1.5rem;
  }

  .info-card,
  .content-tabs,
  .content-display {
    border-radius: 12px;
  }

  .content-tabs {
    border-radius: 12px 12px 0 0;
  }

  .content-display {
    border-radius: 0 0 12px 12px;
  }
}
</style>
