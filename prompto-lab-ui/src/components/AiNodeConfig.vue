<template>
  <div class="operations-content">
    <div class="content-header">
      <h2>AI节点</h2>
      <p>管理AI节点与模型的映射关系</p>
    </div>

    <div class="operations-grid">
      <!-- 已配置模型列表 -->
      <div class="panel-section">
        <div class="section-header">
          <h3>已配置模型</h3>
          <button @click="refreshModels" class="btn btn-sm btn-secondary">
            <span>🔄</span>
            <span>刷新</span>
          </button>
        </div>
        <div class="model-list">
          <div v-if="models.length === 0" class="empty-state">
            <span class="empty-icon">📝</span>
            <p>暂无配置的模型</p>
          </div>
          <div v-for="model in models" :key="model.modelName" class="model-item">
            <div class="model-info">
              <div class="model-name">{{ model.modelName }}</div>
              <div class="model-provider">{{ model.provider }}</div>
              <div class="model-status">
                <span :class="['status-dot', model.enabled ? 'enabled' : 'disabled']"></span>
                <span>{{ model.enabled ? '已启用' : '已禁用' }}</span>
              </div>
            </div>
            <div class="model-actions">
              <button @click="testModel(model.modelName)" class="btn btn-xs btn-secondary">
                测试
              </button>
              <button @click="deleteModel(model.modelName)" class="btn btn-xs btn-danger">
                删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 节点映射配置 -->
      <div class="panel-section">
        <div class="section-header">
          <h3>节点映射</h3>
          <button @click="refreshOperations" class="btn btn-sm btn-secondary">
            <span>🔄</span>
            <span>刷新</span>
          </button>
        </div>
        <div class="operation-mappings">
          <div v-if="operations.length === 0" class="empty-state">
            <span class="empty-icon">🤖</span>
            <p>暂无节点映射</p>
          </div>
          <div v-for="operation in operations" :key="operation.name" class="operation-item">
            <div class="operation-info">
              <div class="operation-name">{{ operation.name }}</div>
              <div class="operation-description">{{ operation.description || '无描述' }}</div>
            </div>
            <div class="operation-mapping">
              <select
                v-model="operation.mappedModel"
                @change="updateOperationMapping(operation.name, operation.mappedModel)"
                class="form-select-sm"
              >
                <option value="">选择模型</option>
                <option v-for="model in enabledModels" :key="model.modelName" :value="model.modelName">
                  {{ model.modelName }}
                </option>
              </select>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import type { SystemOverview, ApiResponse } from '@/types/system'

// 定义类型接口
interface ModelData {
  modelName: string
  provider: string
  enabled: boolean
}

interface OperationData {
  name: string
  mappedModel: string
  description?: string
}

// Props
interface Props {
  systemOverview?: SystemOverview
}

defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update-overview': [overview: SystemOverview]
}>()

// 模型和操作数据
const models = ref<ModelData[]>([])
const operations = ref<OperationData[]>([])

// 计算属性
const enabledModels = computed(() => models.value.filter(model => model.enabled))

// API基础URL
const API_BASE = '/sf/api'

// 刷新模型列表
const refreshModels = async () => {
  try {
    const response = await fetch(`${API_BASE}/models`)
    const result = await response.json()
    if (result.success) {
      models.value = result.data
    }
  } catch (error) {
    console.error('刷新模型列表失败:', error)
  }
}

// 刷新操作列表
const refreshOperations = async () => {
  try {
    const response = await fetch(`${API_BASE}/operations`)
    const result = await response.json()
    if (result.success) {
      operations.value = result.data
    }
  } catch (error) {
    console.error('刷新操作列表失败:', error)
  }
}

// 测试模型
const testModel = async (modelName: string) => {
  try {
    const response = await fetch(`${API_BASE}/models/${modelName}/test`, {
      method: 'POST'
    })
    const result: ApiResponse = await response.json()
    if (result.success) {
      alert('模型测试成功')
    } else {
      throw new Error(result.message || '测试失败')
    }
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '模型测试失败'
    alert('测试失败: ' + errorMessage)
  }
}

// 删除模型
const deleteModel = async (modelName: string) => {
  if (!confirm(`确定要删除模型 ${modelName} 吗？`)) {
    return
  }
  
  try {
    const response = await fetch(`${API_BASE}/models/${modelName}`, {
      method: 'DELETE'
    })
    const result: ApiResponse<SystemOverview> = await response.json()
    if (result.success) {
      alert('模型删除成功')
      await refreshModels()
      if (result.data) {
        emit('update-overview', result.data)
      }
    } else {
      throw new Error(result.message || '删除失败')
    }
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '删除模型时出错'
    alert('删除失败: ' + errorMessage)
  }
}

// 更新操作映射
const updateOperationMapping = async (operationName: string, modelName: string) => {
  try {
    const response = await fetch(`${API_BASE}/operations/${operationName}/mapping`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ modelName })
    })
    const result: ApiResponse<SystemOverview> = await response.json()
    if (result.success) {
      alert('操作映射更新成功')
      if (result.data) {
        emit('update-overview', result.data)
      }
    } else {
      throw new Error(result.message || '更新失败')
    }
  } catch (error: unknown) {
    const errorMessage = error instanceof Error ? error.message : '更新操作映射时出错'
    alert('映射更新失败: ' + errorMessage)
  }
}
</script>

<style scoped>
.content-header {
  margin-bottom: 2rem;
}

.content-header h2 {
  font-size: 2rem;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 0.5rem 0;
}

.content-header p {
  font-size: 1.1rem;
  color: #718096;
  margin: 0;
}

/* 操作配置内容 */
.operations-content .operations-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.panel-section {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 2rem;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h3 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0;
}

.model-list,
.operation-mappings {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #718096;
}

.empty-icon {
  font-size: 2rem;
  display: block;
  margin-bottom: 0.5rem;
}

.model-item,
.operation-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.model-info,
.operation-info {
  flex: 1;
}

.model-name,
.operation-name {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 0.25rem;
}

.model-provider,
.operation-description {
  font-size: 0.875rem;
  color: #718096;
  margin-bottom: 0.25rem;
}

.model-status {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.enabled {
  background: #38a169;
}

.status-dot.disabled {
  background: #e53e3e;
}

.model-actions {
  display: flex;
  gap: 0.5rem;
}

.operation-mapping {
  min-width: 200px;
}

.form-select-sm {
  padding: 0.5rem;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.875rem;
  transition: border-color 0.3s ease;
}

.form-select-sm:focus {
  outline: none;
  border-color: #667eea;
}

/* 按钮样式 */
.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  font-size: 1rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-secondary:hover:not(:disabled) {
  background: #cbd5e0;
}

.btn-danger {
  background: #fed7d7;
  color: #742a2a;
}

.btn-danger:hover:not(:disabled) {
  background: #feb2b2;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

.btn-xs {
  padding: 0.375rem 0.75rem;
  font-size: 0.75rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .operations-grid {
    grid-template-columns: 1fr;
  }
}
</style>