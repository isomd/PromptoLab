import { API_CONFIG } from './apiConfig'
import { apiJsonRequest } from './apiUtils'
import type { ModelConfigData, ApiResponse, ModelsResponse, TestConnectionResponse } from '@/types/system'

export const aiModelApi = {
  // 获取所有模型配置
  async getAllModels(): Promise<ModelsResponse> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/sf/api/models/list`, {
      method: 'GET',
      requireAuth: true
    })
  },

  // 获取单个模型配置
  async getModel(modelName: string): Promise<ModelConfigData> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/sf/api/models/${encodeURIComponent(modelName)}`, {
      method: 'GET',
      requireAuth: true
    })
  },

  // 保存模型配置（创建或更新）
  async saveModel(modelName: string, config: ModelConfigData): Promise<ApiResponse> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/sf/api/models/${encodeURIComponent(modelName)}`, {
      method: 'POST',
      body: JSON.stringify(config),
      requireAuth: true
    })
  },

  // 删除模型配置
  async deleteModel(modelName: string): Promise<ApiResponse> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/sf/api/models/${encodeURIComponent(modelName)}`, {
      method: 'DELETE',
      requireAuth: true
    })
  },

  // 测试模型连接
  async testModel(modelName: string): Promise<TestConnectionResponse> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/sf/api/models/${encodeURIComponent(modelName)}/test`, {
      method: 'POST',
      requireAuth: true
    })
  }
}
