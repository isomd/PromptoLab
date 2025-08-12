import { API_CONFIG } from './apiConfig'
import { apiJsonRequest } from './apiUtils'

export interface LoginRequest {
  email: string
  code: string
}

export interface RegisterRequest {
  email: string
  code: string
}

export interface EmailCodeRequest {
  email: string
  pid: string
  code: string
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface UserVO {
  id: string
  uid: string
  email: string
  nickname: string
  avatar?: string
  point: number
}

export interface LoginResponse {
  userVO: UserVO
  token: string
}

export const userApi = {
  // 获取图片验证码 - 不需要认证
  async getCaptcha(): Promise<ApiResponse<{ base64: string; pid: string }>> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/user/captcha`, {
      method: 'POST',
      requireAuth: false
    })
  },

  // 发送邮箱验证码 - 不需要认证
  async sendEmailCode(data: EmailCodeRequest): Promise<ApiResponse> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/user/email/code`, {
      method: 'POST',
      body: JSON.stringify(data),
      requireAuth: false
    })
  },

  // 用户登录 - 不需要认证
  async login(data: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/user/login`, {
      method: 'POST',
      body: JSON.stringify(data),
      requireAuth: false
    })
  },

  // 用户注册 - 不需要认证
  async register(data: RegisterRequest): Promise<ApiResponse<LoginResponse>> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/user/register`, {
      method: 'POST',
      body: JSON.stringify(data),
      requireAuth: false
    })
  },

  // 获取用户信息 - 需要认证
  async getUserInfo(): Promise<ApiResponse<UserVO>> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/user/info`, {
      method: 'GET',
      requireAuth: true
    })
  },

  // 用户登出 - 需要认证
  async logout(): Promise<ApiResponse> {
    return apiJsonRequest(`${API_CONFIG.BASE_URL}/user/logout`, {
      method: 'POST',
      requireAuth: true
    })
  }
}