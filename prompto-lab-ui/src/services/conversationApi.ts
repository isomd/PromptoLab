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
  type: 'USER_ANSWER' | 'AI_QUESTION' | 'AI_SELECTION_QUESTION' | 'SYSTEM_INFO' | 'AI_ANSWER'
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

// 统一答案请求接口
export interface UnifiedAnswerRequest {
  sessionId: string
  nodeId?: string
  questionType: 'single' | 'multi' | 'input' | 'form'
  answer: any // 根据questionType不同，类型不同
  context?: Record<string, any>
  userId: string
}

// 表单答案项
export interface FormAnswerItem {
  id: string
  value: string[]
}

// API基础URL
const API_BASE = `${API_CONFIG.BASE_URL}/api/demo`
const USER_INTERACTION_BASE = `${API_CONFIG.BASE_URL}/api/user-interaction`

/**
 * 创建新的会话（真实版本 - 对接后端会话管理）
 */
export const startConversation = async (userId: string): Promise<ConversationSession> => {
  // 生成真实的会话ID
  
  // 返回会话信息
  return {
    sessionId: "",
    userId: userId,
    qaTree: null,
    currentNodeId: '', // 由SSE连接建立时动态设置
    createdAt: new Date().toISOString()
  }
}

/**
 * 创建新的会话（Demo版本 - 直接返回模拟数据）
 */
export const startConversationDemo = async (userId: string): Promise<ConversationSession> => {
  // Demo版本：直接返回模拟的会话数据
  return {
    sessionId: userId,
    userId: userId,
    qaTree: null,
    currentNodeId: '', // 由SSE连接建立时动态设置
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
    currentNodeId: '', // 由SSE连接建立时动态设置
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
 * 发送用户消息到用户交互接口
 * 对接后端的用户交互消息接口
 */
export const sendUserMessage = async (request: MessageRequest, userId: string, nodeId?: string): Promise<void> => {
  // 构建统一答案请求格式
  const unifiedRequest: UnifiedAnswerRequest = {
    sessionId: request.sessionId,
    nodeId: nodeId || undefined, // 添加nodeId参数
    questionType: 'input', // 普通文本消息作为input类型
    answer: request.content,
    userId: userId // 使用正确的userId
  }
  
  const url = `${USER_INTERACTION_BASE}/message`
  await apiRequest(url, {
    method: 'POST',
    body: JSON.stringify(unifiedRequest),
    headers: {
      'Content-Type': 'application/json'
    },
    requireAuth: false
  })
}

/**
 * 处理统一答案请求
 * 对接后端的processAnswer接口
 */
export const processAnswer = async (request: UnifiedAnswerRequest): Promise<void> => {
  const url = `${USER_INTERACTION_BASE}/message`
  await apiRequest(url, {
    method: 'POST',
    body: JSON.stringify(request),
    headers: {
      'Content-Type': 'application/json'
    },
    requireAuth: false
  })
}

/**
 * 建立用户交互SSE连接
 * 对接后端的用户交互SSE接口
 */
export const connectUserInteractionSSE = (sessionId: string | null, userId: string, onMessage: (response: MessageResponse) => void, onError?: (error: Event) => void): EventSource => {
  const params = new URLSearchParams()
  if (sessionId) {
    params.append('sessionId', sessionId)
  }
  params.append('userId', userId)
  const url = `${USER_INTERACTION_BASE}/sse?${params.toString()}`
  const eventSource = new EventSource(url)

  // 监听连接建立事件
  eventSource.addEventListener('connected', (event: MessageEvent) => {
    console.log('用户交互SSE连接已建立:', event.data)
    try {
      const response = JSON.parse(event.data)
      onMessage(response)
    } catch (error) {
      console.error('解析连接建立消息失败:', error)
    }
  })

  // 监听消息事件
  eventSource.addEventListener('message', (event: MessageEvent) => {
    try {
      const response: MessageResponse = JSON.parse(event.data)
      onMessage(response)
    } catch (error) {
      console.error('解析用户交互SSE消息失败:', error)
    }
  })

  eventSource.onerror = (error: Event) => {
    console.error('用户交互SSE连接错误:', error)
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
