<template>
  <div class="ai-log-content">
    <!-- 显示日志详情页面 -->
    <LogDetailModal
      v-if="showLogDetail"
      :log="selectedLog"
      @go-back="closeLogDetail"
    />

    <!-- 显示日志列表页面 -->
    <div v-else>
      <!-- 精简的页面头部 -->
      <div class="content-header">
        <h2>AI调用日志</h2>
        <div class="header-actions">
          <button @click="loadLogs" class="btn btn-secondary" :disabled="loading">
            <svg v-if="loading" class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
            </svg>
            <span>{{ loading ? '加载中' : '刷新' }}</span>
          </button>
          <button @click="clearAllLogs" class="btn btn-danger" :disabled="loading || logs.length === 0">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
            </svg>
            <span>清空</span>
          </button>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 加载状态 -->
        <div v-if="loading && logs.length === 0" class="loading-state">
          <div class="loading-spinner"></div>
          <p>正在加载调用日志...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="logs.length === 0" class="empty-state">
          <div class="empty-icon">📋</div>
          <h3>暂无调用日志</h3>
          <p>当AI模型被调用时，相关日志将在这里显示</p>
        </div>

        <!-- 日志列表 -->
        <div v-else class="logs-list">
          <!-- 精简的列表头部 -->
          <div class="list-header">
            <div class="header-info">
              <span class="log-count">共 {{ filteredLogs.length }} 条记录</span>
              <div class="search-box">
                <svg class="search-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="搜索日志..."
                  class="search-input"
                />
              </div>
            </div>
            <div class="filter-controls">
              <select v-model="filterOperation" class="filter-select">
                <option value="">全部操作</option>
                <option v-for="operation in uniqueOperations" :key="operation" :value="operation">
                  {{ operation }}
                </option>
              </select>
              <select v-model="filterModel" class="filter-select">
                <option value="">全部模型</option>
                <option v-for="model in uniqueModels" :key="model" :value="model">
                  {{ model }}
                </option>
              </select>
              <select v-model="filterStatus" class="filter-select">
                <option value="">全部状态</option>
                <option value="SUCCESS">成功</option>
                <option value="FAILED">失败</option>
              </select>
            </div>
          </div>

          <!-- 优化的表格 -->
          <div class="table-container">
            <div class="table-header">
              <div class="header-cell time-cell">时间</div>
              <div class="header-cell status-cell">状态</div>
              <div class="header-cell operation-cell">操作</div>
              <div class="header-cell model-cell">模型</div>
              <div class="header-cell duration-cell">耗时</div>
              <div class="header-cell actions-cell">详情</div>
            </div>

            <div class="table-body">
              <div
                v-for="log in paginatedLogs"
                :key="log.callId"
                class="table-row"
                :class="{ 'row-failed': log.status !== 'SUCCESS' }"
              >
                <!-- 时间列 -->
                <div class="cell time-cell">
                  <div class="time-info">
                    <span class="time-relative">{{ formatRelativeTime(log.callTime) }}</span>
                    <span class="time-full">{{ formatTime(log.callTime) }}</span>
                  </div>
                </div>

                <!-- 状态列 -->
                <div class="cell status-cell">
                  <span class="status-indicator" :class="log.status.toLowerCase()">
                    <span class="status-dot"></span>
                    {{ log.status === 'SUCCESS' ? '成功' : '失败' }}
                  </span>
                </div>

                <!-- 操作列 -->
                <div class="cell operation-cell">
                  <span class="operation-tag">{{ log.operationType }}</span>
                </div>

                <!-- 模型列 -->
                <div class="cell model-cell">
                  <div class="model-info">
                    <div class="model-avatar">
                      <img :src="getProviderIcon(getProviderFromModel(log.modelName))" alt="provider" class="provider-icon" />
                    </div>
                    <div class="model-details">
                      <div class="model-name">{{ log.modelName }}</div>
                      <div class="provider-name">{{ getProviderName(getProviderFromModel(log.modelName)) }}</div>
                    </div>
                  </div>
                </div>

                <!-- 耗时列 -->
                <div class="cell duration-cell">
                  <span class="duration-value" :class="getDurationClass(log.duration)">
                    {{ formatDuration(log.duration) }}
                  </span>
                </div>

                <!-- 操作列 -->
                <div class="cell actions-cell">
                  <button
                    @click="viewLogDetail(log)"
                    class="action-btn"
                    title="查看详情"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination" v-if="totalPages > 1">
            <button
              @click="currentPage = Math.max(1, currentPage - 1)"
              :disabled="currentPage === 1"
              class="page-btn"
            >
              上一页
            </button>
            <span class="page-info">
              第 {{ currentPage }} 页，共 {{ totalPages }} 页
            </span>
            <button
              @click="currentPage = Math.min(totalPages, currentPage + 1)"
              :disabled="currentPage === totalPages"
              class="page-btn"
            >
              下一页
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { aiCallLogApi } from '@/services/aiCallLogApi'
import LogDetailModal from './LogDetailModal.vue'
import type { AICallLogSummary } from '@/types/system'

// 状态管理
const loading = ref(false)
const logs = ref<AICallLogSummary[]>([])
const searchQuery = ref('')
const filterOperation = ref('')
const filterModel = ref('')
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = 20
const showLogDetail = ref(false)
const selectedLog = ref<AICallLogSummary | null>(null)

// 计算属性
const uniqueOperations = computed(() => {
  return [...new Set(logs.value.map(log => log.operationType))]
})

const uniqueModels = computed(() => {
  return [...new Set(logs.value.map(log => log.modelName))]
})

const filteredLogs = computed(() => {
  let filtered = logs.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(log =>
      log.callId.toLowerCase().includes(query) ||
      log.operationType.toLowerCase().includes(query) ||
      log.modelName.toLowerCase().includes(query) ||
      log.errorMessage?.toLowerCase().includes(query)
    )
  }

  // 操作类型过滤
  if (filterOperation.value) {
    filtered = filtered.filter(log => log.operationType === filterOperation.value)
  }

  // 模型过滤
  if (filterModel.value) {
    filtered = filtered.filter(log => log.modelName === filterModel.value)
  }

  // 状态过滤
  if (filterStatus.value) {
    filtered = filtered.filter(log => log.status === filterStatus.value)
  }

  // 按时间倒序排列
  return filtered.sort((a, b) => new Date(b.callTime).getTime() - new Date(a.callTime).getTime())
})

const totalPages = computed(() => {
  return Math.ceil(filteredLogs.value.length / pageSize)
})

const paginatedLogs = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredLogs.value.slice(start, end)
})

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

const formatRelativeTime = (callTime: string) => {
  const now = Date.now()
  const timestamp = new Date(callTime).getTime()
  const diff = now - timestamp
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (days > 0) return `${days}天前`
  if (hours > 0) return `${hours}小时前`
  if (minutes > 0) return `${minutes}分钟前`
  return '刚刚'
}

const formatTime = (callTime: string) => {
  return new Date(callTime).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const formatDuration = (duration: number | undefined) => {
  if (!duration && duration !== 0) return '0ms'
  if (duration < 1000) return `${duration}ms`
  return `${(duration / 1000).toFixed(2)}s`
}

const getDurationClass = (duration: number | undefined) => {
  if (!duration && duration !== 0) return 'duration-unknown'
  if (duration < 2000) return 'duration-fast'
  if (duration < 10000) return 'duration-normal'
  return 'duration-slow'
}

// 主要方法
const loadLogs = async () => {
  try {
    loading.value = true
    const logsData = await aiCallLogApi.getAllLogSummaries()
    logs.value = logsData
  } catch (error) {
    console.error('加载日志失败:', error)
    alert('加载日志失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const clearAllLogs = async () => {
  if (!confirm('确定要清空所有日志吗？此操作不可恢复。')) {
    return
  }

  try {
    loading.value = true
    await aiCallLogApi.clearLogs()
    await loadLogs()
    alert('日志已清空')
  } catch (error) {
    console.error('清空日志失败:', error)
    alert('清空日志失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 页面切换逻辑
const viewLogDetail = (log: AICallLogSummary) => {
  selectedLog.value = log
  showLogDetail.value = true
}

const closeLogDetail = () => {
  showLogDetail.value = false
  selectedLog.value = null
  // 重新加载日志列表以确保数据最新
  loadLogs()
}

// 组件挂载时加载数据
onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
/* 基础样式 */
.ai-log-content {
  padding: 0;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 50%, #0f0f0f 100%);
  min-height: 100vh;
  color: #e5e7eb;
  position: relative;
  overflow: hidden;
}

.ai-log-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 20% 20%, rgba(255, 215, 0, 0.03) 0%, transparent 50%),
              radial-gradient(circle at 80% 80%, rgba(255, 215, 0, 0.02) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 精简的页面头部 */
.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding: 1.5rem 2rem 0;
  position: relative;
  z-index: 1;
}

.content-header h2 {
  font-size: 1.875rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #ffd700 50%, #ffed4e 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  text-shadow: 0 0 20px rgba(255, 215, 0, 0.3);
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.btn {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  cursor: pointer;
  font-size: 0.875rem;
  backdrop-filter: blur(10px);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.05);
  color: #e5e7eb;
  border: 1px solid rgba(255, 215, 0, 0.2);
}

.btn-secondary:hover:not(:disabled) {
  background: rgba(255, 215, 0, 0.1);
  border-color: rgba(255, 215, 0, 0.3);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.2);
}

.btn-danger {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
  border: 1px solid rgba(239, 68, 68, 0.3);
}

.btn-danger:hover:not(:disabled) {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.main-content {
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 1.5rem 2rem;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 215, 0, 0.1);
  margin: 0 2rem 2rem;
  position: relative;
  z-index: 1;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: #9ca3af;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 215, 0, 0.2);
  border-top: 3px solid #ffd700;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.6;
}

.empty-state h3 {
  color: #ffffff;
  margin-bottom: 0.5rem;
  font-size: 1.125rem;
  font-weight: 600;
}

.empty-state p {
  color: #9ca3af;
  font-size: 0.875rem;
}

/* 精简的列表头部 */
.list-header {
  margin-bottom: 1rem;
}

.header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.log-count {
  color: #9ca3af;
  font-size: 0.875rem;
  font-weight: 500;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 0.75rem;
  width: 1rem;
  height: 1rem;
  color: #ffd700;
  z-index: 1;
}

.search-input {
  padding: 0.5rem 0.75rem 0.5rem 2.25rem;
  border: 1px solid rgba(255, 215, 0, 0.2);
  border-radius: 6px;
  font-size: 0.875rem;
  width: 200px;
  transition: all 0.2s ease;
  background: rgba(255, 255, 255, 0.05);
  color: #e5e7eb;
  backdrop-filter: blur(10px);
}

.search-input::placeholder {
  color: #9ca3af;
}

.search-input:focus {
  outline: none;
  border-color: #ffd700;
  box-shadow: 0 0 0 2px rgba(255, 215, 0, 0.2);
}

.filter-controls {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.filter-select {
  padding: 0.5rem 0.75rem;
  border: 1px solid rgba(255, 215, 0, 0.2);
  border-radius: 6px;
  font-size: 0.875rem;
  background: rgba(255, 255, 255, 0.05);
  color: #e5e7eb;
  backdrop-filter: blur(10px);
  transition: all 0.2s ease;
}

.filter-select:focus {
  outline: none;
  border-color: #ffd700;
  box-shadow: 0 0 0 2px rgba(255, 215, 0, 0.2);
}

.filter-select option {
  background: #1a1a1a;
  color: #e5e7eb;
}

/* 优化的表格样式 */
.table-container {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255, 215, 0, 0.2);
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(10px);
}

.table-header {
  display: grid;
  grid-template-columns: 140px 100px 400px 2fr 100px 80px;
  gap: 0;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.08) 0%, rgba(255, 215, 0, 0.04) 100%);
  border-bottom: 2px solid rgba(255, 215, 0, 0.2);
}

.header-cell {
  padding: 1rem 0.75rem;
  font-weight: 600;
  color: #ffd700;
  font-size: 0.875rem;
  text-align: left;
  border-right: 1px solid rgba(255, 215, 0, 0.1);
}

.header-cell:last-child {
  border-right: none;
}

.table-body {
  background: rgba(255, 255, 255, 0.01);
}

.table-row {
  display: grid;
  grid-template-columns: 140px 100px 400px 2fr 100px 80px;
  gap: 0;
  border-bottom: 1px solid rgba(255, 215, 0, 0.1);
  transition: all 0.3s ease;
  position: relative;
}

.table-row:hover {
  background: rgba(255, 215, 0, 0.05);
  backdrop-filter: blur(10px);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.1);
}

.table-row:last-child {
  border-bottom: none;
}

.table-row.row-failed {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.1) 0%, rgba(220, 38, 38, 0.05) 100%);
  border-left: 4px solid #ef4444;
}

.table-row.row-failed:hover {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.15) 0%, rgba(220, 38, 38, 0.1) 100%);
}

.cell {
  padding: 1rem 0.75rem;
  display: flex;
  align-items: center;
  border-right: 1px solid rgba(255, 215, 0, 0.05);
  min-height: 60px;
}

.cell:last-child {
  border-right: none;
}

/* 时间列样式 */
.time-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.time-relative {
  font-size: 0.875rem;
  font-weight: 500;
  color: #ffffff;
}

.time-full {
  font-size: 0.75rem;
  color: #9ca3af;
}

/* 模型列样式 */
.model-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
}

.model-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: rgba(255, 215, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 215, 0, 0.2);
  flex-shrink: 0;
  box-shadow: 0 4px 8px rgba(255, 215, 0, 0.1);
}

.provider-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
  filter: brightness(1.1);
}

.model-details {
  flex: 1;
  min-width: 0;
}

.model-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 0.25rem 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.provider-name {
  font-size: 0.75rem;
  color: #9ca3af;
  font-weight: 500;
}

/* 操作列样式 */
.operation-tag {
  display: inline-flex;
  align-items: center;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 600;
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  border: 1px solid rgba(59, 130, 246, 0.2);
}

/* 耗时列样式 */
.duration-value {
  font-size: 0.875rem;
  font-weight: 600;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.duration-fast {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.duration-normal {
  color: #fbbf24;
  background: rgba(251, 191, 36, 0.1);
  border: 1px solid rgba(251, 191, 36, 0.2);
}

.duration-slow {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

.duration-unknown {
  color: #9ca3af;
  background: rgba(156, 163, 175, 0.1);
  border: 1px solid rgba(156, 163, 175, 0.2);
}

/* 状态列样式 */
.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-indicator.success .status-dot {
  background: #10b981;
  box-shadow: 0 0 0 2px rgba(16, 185, 129, 0.2);
}

.status-indicator.success {
  color: #10b981;
}

.status-indicator.failed .status-dot {
  background: #ef4444;
  box-shadow: 0 0 0 2px rgba(239, 68, 68, 0.2);
}

.status-indicator.failed {
  color: #ef4444;
}

/* 操作列样式 */
.actions-cell {
  justify-content: center;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid rgba(255, 215, 0, 0.2);
  background: rgba(255, 215, 0, 0.05);
  color: #9ca3af;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(255, 215, 0, 0.1);
  backdrop-filter: blur(10px);
}

.action-btn:hover {
  background: rgba(255, 215, 0, 0.1);
  border-color: rgba(255, 215, 0, 0.4);
  color: #ffd700;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.2);
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(255, 215, 0, 0.2);
}

.page-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(255, 215, 0, 0.2);
  background: rgba(255, 255, 255, 0.05);
  color: #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 0.875rem;
  backdrop-filter: blur(10px);
}

.page-btn:hover:not(:disabled) {
  background: rgba(255, 215, 0, 0.1);
  border-color: rgba(255, 215, 0, 0.3);
  color: #ffd700;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  color: #9ca3af;
  font-size: 0.875rem;
  font-weight: 500;
}

/* 工具类 */
.w-4 {
  width: 1rem;
}

.h-4 {
  height: 1rem;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .table-header,
  .table-row {
    grid-template-columns: 120px 90px 300px 2fr 90px 70px;
  }
}

@media (max-width: 768px) {
  .content-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .header-info {
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }

  .search-input {
    width: 100%;
  }

  .filter-controls {
    flex-wrap: wrap;
  }

  .table-header {
    display: none;
  }

  .table-row {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    padding: 1rem;
    border: 1px solid rgba(255, 215, 0, 0.2);
    border-radius: 8px;
    margin-bottom: 0.75rem;
    background: rgba(255, 255, 255, 0.02);
  }

  .table-body {
    padding: 0.75rem;
  }

  .cell {
    padding: 0;
    border: none;
    min-height: auto;
  }

  .model-info {
    width: 100%;
  }

  .main-content {
    margin: 0 1rem 1rem;
    padding: 1rem;
  }

  .content-header {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .header-actions {
    flex-direction: column;
    width: 100%;
  }

  .btn {
    justify-content: center;
  }

  .filter-controls {
    flex-direction: column;
    width: 100%;
  }

  .filter-select {
    width: 100%;
  }
}
</style>
