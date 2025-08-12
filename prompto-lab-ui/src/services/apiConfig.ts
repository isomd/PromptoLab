const API_CONFIG = {
  BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  N8N_URL: import.meta.env.VITE_N8N_BASE_URL || 'http://124.70.70.127:5678',

  ENDPOINTS: {
  }
}

// 获取完整的API URL
export const getApiUrl = (endpoint: keyof typeof API_CONFIG.ENDPOINTS, path: string = '') => {
  return `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS[endpoint]}${path}`
}

// 导出配置供其他地方使用
export { API_CONFIG }
