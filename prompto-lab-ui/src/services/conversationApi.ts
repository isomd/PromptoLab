import { apiRequest } from './apiUtils'
import { API_CONFIG } from './apiConfig'

// 类型定义
export interface MessageRequest {
  sessionId: string
  content: string
  type: 'USER_TEXT' | 'USER_SELECTION'
  questionId?: string
  selectedOption?: number
}

export interface MessageResponse {
  nodeId: string
  content: string
  type: 'USER_ANSWER' | 'AI_QUESTION' | 'AI_SELECTION_QUESTION' | 'SYSTEM_INFO'
  options?: string[]
  timestamp: number
}

export interface ConversationSession {
  sessionId: string
  userId: string
  qaTree: any
  currentNodeId: string
  createdAt: string
}

// API基础URL - 使用SSE Demo接口
const API_BASE = `${API_CONFIG.BASE_URL}/api/demo`

/**
 * 创建新的会话（Demo版本 - 直接返回模拟数据）
 */
export const startConversation = async (userId: string): Promise<ConversationSession> => {
  // Demo版本：直接返回模拟的会话数据
  return {
    sessionId: userId,
    userId: userId,
    qaTree: null,
    currentNodeId: 'root',
    createdAt: new Date().toISOString()
  }
}

/**
 * 发送消息（Demo版本 - 使用SSE Demo的发送接口）
 */
export const sendMessage = async (request: MessageRequest): Promise<void> => {
  const url = `${API_BASE}/send/${request.sessionId}?message=${encodeURIComponent(request.content)}`
  await apiRequest(url, {
    method: 'POST',
    requireAuth: false
  })
}

/**
 * 获取会话信息（Demo版本）
 */
export const getSession = async (sessionId: string): Promise<ConversationSession> => {
  return {
    sessionId: sessionId,
    userId: sessionId,
    qaTree: null,
    currentNodeId: 'root',
    createdAt: new Date().toISOString()
  }
}

/**
 * 建立SSE连接（Demo版本 - 使用SSE Demo接口）
 */
export const connectSSE = (sessionId: string, onMessage: (response: MessageResponse) => void, onError?: (error: Event) => void): EventSource => {
  const url = `${API_BASE}/sse/${sessionId}`
  const eventSource = new EventSource(url)

  // 监听连接建立事件
  eventSource.addEventListener('connected', (event: MessageEvent) => {
    console.log('SSE连接已建立:', event.data)
  })

  // 监听普通消息
  eventSource.addEventListener('message', (event: MessageEvent) => {
    try {
      // Demo版本：将SSE消息转换为MessageResponse格式
      const response: MessageResponse = {
        nodeId: `ai_${Date.now()}`,
        content: event.data,
        type: 'AI_QUESTION',
        timestamp: Date.now()
      }
      onMessage(response)
    } catch (error) {
      console.error('解析SSE消息失败:', error)
    }
  })

  // 监听流式数据
  eventSource.addEventListener('stream', (event: MessageEvent) => {
    try {
      const response: MessageResponse = {
        nodeId: `stream_${Date.now()}`,
        content: event.data,
        type: 'AI_QUESTION',
        timestamp: Date.now()
      }
      onMessage(response)
    } catch (error) {
      console.error('解析流式消息失败:', error)
    }
  })

  // 监听广播消息
  eventSource.addEventListener('broadcast', (event: MessageEvent) => {
    try {
      const response: MessageResponse = {
        nodeId: `broadcast_${Date.now()}`,
        content: `[广播] ${event.data}`,
        type: 'SYSTEM_INFO',
        timestamp: Date.now()
      }
      onMessage(response)
    } catch (error) {
      console.error('解析广播消息失败:', error)
    }
  })

  eventSource.onerror = (error: Event) => {
    console.error('SSE连接错误:', error)
    if (onError) {
      onError(error)
    }
  }

  return eventSource
}

/**
 * 关闭SSE连接
 */
export const closeSSE = (eventSource: EventSource | null) => {
  if (eventSource) {
    eventSource.close()
  }
}
