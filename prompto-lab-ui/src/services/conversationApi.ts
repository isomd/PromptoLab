import { apiJsonRequest, apiRequest } from './apiUtils'
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
  type: 'USER_ANSWER' | 'AI_QUESTION' | 'AI_SELECTION_QUESTION' | 'SYSTEM_INFO' | 'AI_STREAM_START' | 'AI_STREAM_CHUNK' | 'AI_STREAM_END' | 'AI_ANSWER'
  options?: string[]
  timestamp: number
  isComplete?: boolean  // 标识消息是否完整
}

export interface ConversationSession {
  sessionId: string
  userId: string
  qaTree: any
  currentNodeId: string
  createdAt: string
}

// API基础URL - 使用ConversationController接口
const API_BASE = `${API_CONFIG.BASE_URL}/api/conversation`

/**
 * 创建新的会话 - 对应ConversationController.startConversation
 */
export const startConversation = async (userId: string): Promise<ConversationSession> => {
  const url = `${API_BASE}/start?userId=${encodeURIComponent(userId)}`
  return await apiJsonRequest<ConversationSession>(url, {
    method: 'POST',
    requireAuth: false
  })
}

/**
 * 发送消息 - 对应ConversationController.addMessage
 */
export const sendMessage = async (request: MessageRequest): Promise<void> => {
  const url = `${API_BASE}/message`
  await apiRequest(url, {
    method: 'POST',
    body: JSON.stringify(request),
    requireAuth: false
  })
}

/**
 * 获取会话信息
 */
export const getSession = async (sessionId: string): Promise<ConversationSession> => {
  const url = `${API_BASE}/session/${sessionId}`
  return await apiJsonRequest<ConversationSession>(url, {
    method: 'GET',
    requireAuth: false
  })
}

/**
 * 建立SSE连接 - 对应ConversationController.streamConversation
 */
/**
 * 建立SSE连接 - 支持流式响应
 */
export const connectSSE = (sessionId: string, onMessage: (response: MessageResponse) => void, onError?: (error: Event) => void): EventSource => {
  const url = `${API_BASE}/sse/${sessionId}`
  const eventSource = new EventSource(url)
  
  // 监听默认message事件
  eventSource.addEventListener('message', (event: MessageEvent) => {
    try {
      const response: MessageResponse = JSON.parse(event.data)
      onMessage(response)
    } catch (error) {
      console.error('解析SSE消息失败:', error)
    }
  })
  
  // 监听流式开始事件
  eventSource.addEventListener('stream_start', (event: MessageEvent) => {
    try {
      const response: MessageResponse = JSON.parse(event.data)
      response.type = 'AI_STREAM_START'
      onMessage(response)
    } catch (error) {
      console.error('解析流式开始消息失败:', error)
    }
  })
  
  // 监听流式数据块事件
  eventSource.addEventListener('stream_chunk', (event: MessageEvent) => {
    try {
      const response: MessageResponse = JSON.parse(event.data)
      response.type = 'AI_STREAM_CHUNK'
      onMessage(response)
    } catch (error) {
      console.error('解析流式数据块失败:', error)
    }
  })
  
  // 监听流式结束事件
  eventSource.addEventListener('stream_end', (event: MessageEvent) => {
    try {
      const response: MessageResponse = JSON.parse(event.data)
      response.type = 'AI_STREAM_END'
      onMessage(response)
    } catch (error) {
      console.error('解析流式结束消息失败:', error)
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