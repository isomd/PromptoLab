<template>
  <div class="poet-auth-container">
    <!-- 诗意背景 -->
    <div class="poet-background">
      <div class="floating-words">
        <span class="word word-1">智能</span>
        <span class="word word-2">实验</span>
        <span class="word word-3">优化</span>
        <span class="word word-4">精准</span>
        <span class="word word-5">创新</span>
      </div>
      <div class="ink-drops">
        <div class="ink-drop drop-1"></div>
        <div class="ink-drop drop-2"></div>
        <div class="ink-drop drop-3"></div>
      </div>
      <div class="aurora-effect"></div>
    </div>

    <div class="auth-layout">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <div class="brand-content">
          <!-- 品牌标识 -->
          <div class="brand-header">
            <div class="brand-logo-container">
              <div class="brand-icon">
                <svg width="72" height="72" viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <!-- 实验室烧杯图标 -->
                  <path d="M30 20 L70 20 L65 80 Q65 85 60 85 L40 85 Q35 85 35 80 Z" fill="url(#gradient1)" opacity="0.9"/>
                  <path d="M25 30 L75 30 L70 75 Q70 80 65 80 L35 80 Q30 80 30 75 Z" fill="url(#gradient2)" opacity="0.7"/>
                  <circle cx="50" cy="50" r="15" fill="url(#gradient3)" opacity="0.8"/>
                  <defs>
                    <linearGradient id="gradient1" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" style="stop-color:#d4af37;stop-opacity:1" />
                      <stop offset="100%" style="stop-color:#f7e98e;stop-opacity:1" />
                    </linearGradient>
                    <linearGradient id="gradient2" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
                      <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
                    </linearGradient>
                    <linearGradient id="gradient3" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" style="stop-color:#4facfe;stop-opacity:1" />
                      <stop offset="100%" style="stop-color:#00f2fe;stop-opacity:1" />
                    </linearGradient>
                  </defs>
                </svg>
              </div>
              <div class="brand-text">
                <h1 class="brand-title">PromptoLab</h1>
                <p class="brand-tagline">智能提示词实验室</p>
              </div>
            </div>
          </div>

          <!-- 诗意描述 -->
          <div class="poet-description">
            <div class="description-item">
              <div class="item-icon">🧠</div>
              <div class="item-content">
                <h3>AI提示词优化</h3>
                <p>精准调校每一个提示词，释放AI的无限潜能</p>
              </div>
            </div>
            <div class="description-item">
              <div class="item-icon">⚡</div>
              <div class="item-content">
                <h3>智能实验平台</h3>
                <p>专业的测试环境，让每次尝试都有价值</p>
              </div>
            </div>
            <div class="description-item">
              <div class="item-icon">🎯</div>
              <div class="item-content">
                <h3>精准结果输出</h3>
                <p>从模糊想法到精确指令，一站式解决方案</p>
              </div>
            </div>
          </div>

          <!-- 诗句装饰 -->
          <div class="poetry-quote">
            <blockquote>
              "精准的提示词是通往AI智慧的钥匙"
              <cite>— PromptoLab</cite>
            </blockquote>
          </div>
        </div>
      </div>

<!-- 右侧表单区域 -->
<div class="form-section">
  <div class="form-container">
    <!-- 表单头部 - 简化为单一登录 -->
    <div class="form-header">
      <h2 class="form-title">开启智能实验之旅</h2>
      <p class="form-subtitle">登录即可开始，新用户将自动注册</p>
    </div>

    <!-- 登录表单 - 移除tab切换 -->
    <form @submit.prevent="handleLogin" class="auth-form">
      <div class="input-group">
        <label class="input-label">邮箱地址</label>
        <div class="input-wrapper">
          <input
            v-model="loginForm.email"
            type="email"
            class="form-input"
            placeholder="请输入您的邮箱地址"
            required
          />
          <div class="input-icon">📧</div>
        </div>
      </div>

      <!-- 图片验证码 - 只在发送邮箱验证码时需要 -->
      <div class="input-group" v-if="!emailCodeSent">
        <label class="input-label">图片验证码</label>
        <div class="captcha-wrapper">
          <div class="captcha-input-section">
            <div class="input-wrapper">
              <input
                v-model="loginForm.code"
                type="text"
                class="form-input captcha-input"
                placeholder="请输入验证码"
                maxlength="6"
                required
              />
              <div class="input-icon">🔐</div>
            </div>
          </div>
          <div class="captcha-image-section">
            <div class="captcha-display" @click="refreshCaptcha">
              <img
                v-if="captchaData.base64"
                :src="`data:image/png;base64,${captchaData.base64}`"
                alt="验证码"
                class="captcha-image"
              />
              <div v-else class="captcha-placeholder">
                <div class="loading-spinner"></div>
                <span>加载中...</span>
              </div>
              <div class="captcha-refresh-hint">点击刷新</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 邮箱验证码 -->
      <div class="input-group">
        <label class="input-label">邮箱验证码</label>
        <div class="email-code-wrapper">
          <div class="email-input-section">
            <div class="input-wrapper">
              <input
                v-model="loginForm.emailCode"
                type="text"
                class="form-input"
                placeholder="请输入邮箱验证码"
                maxlength="6"
                :required="emailCodeSent"
              />
              <div class="input-icon">✉️</div>
            </div>
          </div>
          <div class="email-button-section">
            <button
              type="button"
              @click="sendEmailCode"
              :disabled="emailCodeLoading || emailCodeCountdown > 0 || (!emailCodeSent && !canSendEmailCode)"
              class="send-code-button"
              v-if="!emailCodeSent || emailCodeCountdown > 0"
            >
              <span v-if="emailCodeCountdown > 0" class="countdown">{{ emailCodeCountdown }}s</span>
              <span v-else-if="emailCodeLoading" class="loading">发送中...</span>
              <span v-else>发送验证码</span>
            </button>
            <div v-else class="code-sent-indicator">
              <span class="success-icon">✓</span>
              验证码已发送
            </div>
          </div>
        </div>
      </div>

      <button type="submit" :disabled="!canLogin" class="submit-button">
        <span v-if="loginLoading" class="button-loading">
          <div class="loading-spinner"></div>
          登录中...
        </span>
        <span v-else>开始创作</span>
      </button>
    </form>

    <!-- 底部信息 - 标准的同意条款复选框 -->
    <div class="form-footer">
      <div class="terms-agreement">
        <div class="checkbox-wrapper" @click="toggleAgreement">
          <div class="checkbox" :class="{ checked: agreedToTerms }">
            <div class="checkmark" v-if="agreedToTerms">✓</div>
          </div>
          <label class="agreement-text">
            我已阅读并同意
            <a href="#" class="terms-link" @click.stop="showTerms">《服务条款》</a>
            和
            <a href="#" class="terms-link" @click.stop="showPrivacy">《隐私政策》</a>
          </label>
        </div>
      </div>
    </div>
  </div>
</div>
    </div>

    <!-- 成功提示 -->
    <div v-if="showSuccessMessage" class="success-overlay">
      <div class="success-message">
        <div class="success-icon">✨</div>
        <h3>{{ successTitle }}</h3>
        <p>{{ successMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/services/userApi'
import { toast } from '@/utils/toast'

const router = useRouter()

// 响应式数据
const activeTab = ref('login')
const loginLoading = ref(false)
const registerLoading = ref(false)
const emailCodeLoading = ref(false)
const emailCodeCountdown = ref(0)
const showSuccessMessage = ref(false)
const successTitle = ref('')
const successMessage = ref('')
const agreedToTerms = ref(false)
const emailCodeSent = ref(false)

// 验证码数据
const captchaData = ref({
  base64: '',
  pid: ''
})

// 表单数据
const loginForm = ref({
  email: '',
  code: '',
  emailCode: ''
})

const registerForm = ref({
  email: '',
  code: '',
  emailCode: ''
})

// 计算属性
const canSendEmailCode = computed(() => {
  const currentForm = activeTab.value === 'login' ? loginForm.value : registerForm.value
  return currentForm.email && currentForm.code && captchaData.value.pid
})

const canLogin = computed(() => {
  return loginForm.value.email && loginForm.value.emailCode && agreedToTerms.value
})

// 切换同意条款
const toggleAgreement = () => {
  agreedToTerms.value = !agreedToTerms.value
}

// 显示条款
const showTerms = () => {
  // 这里可以打开条款弹窗或跳转到条款页面
  console.log('显示服务条款')
}

// 显示隐私政策
const showPrivacy = () => {
  // 这里可以打开隐私政策弹窗或跳转到隐私政策页面
  console.log('显示隐私政策')
}

// 显示帮助
const showHelp = () => {
  // 这里可以打开帮助弹窗
  console.log('显示帮助')
}

// 体验演示
const tryDemo = () => {
  // 这里可以跳转到演示页面
  console.log('体验演示')
}

// 切换标签页
const switchTab = (tab: string) => {
  activeTab.value = tab
  // 清空表单
  loginForm.value = { email: '', code: '', emailCode: '' }
  registerForm.value = { email: '', code: '', emailCode: '' }
  // 重新获取验证码
  getCaptcha()
}

// 获取图片验证码
const getCaptcha = async () => {
  try {
    const response = await userApi.getCaptcha()
    if (response.code === 200) {
      captchaData.value = response.data
    } else {
      toast.error({
        title: '获取验证码失败',
        message: response.message || '请稍后重试'
      })
    }
  } catch (error: any) {
    toast.error({
      title: '获取验证码失败',
      message: '网络连接异常，请检查网络后重试'
    })
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  getCaptcha()
}

// 发送邮箱验证码
const sendEmailCode = async () => {
  const currentForm = activeTab.value === 'login' ? loginForm.value : registerForm.value

  if (!currentForm.email) {
    toast.warning({
      title: '请输入邮箱',
      message: '请先输入您的邮箱地址'
    })
    return
  }

  if (!emailCodeSent.value && !currentForm.code) {
    toast.warning({
      title: '请输入验证码',
      message: '请先输入图片验证码'
    })
    return
  }

  emailCodeLoading.value = true

  try {
    const response = await userApi.sendEmailCode({
      email: currentForm.email,
      pid: captchaData.value.pid,
      code: currentForm.code
    })

    if (response.code === 200) {
      toast.success({
        title: '验证码发送成功',
        message: '请查收您的邮箱，验证码5分钟内有效'
      })
      emailCodeSent.value = true
      startCountdown()
      // 发送成功后刷新图片验证码
      refreshCaptcha()
      // 清空图片验证码输入
      currentForm.code = ''
    } else {
      toast.error({
        title: '发送失败',
        message: response.message || '请检查输入信息后重试'
      })
      // 刷新验证码
      refreshCaptcha()
    }
  } catch (error: any) {
    toast.error({
      title: '发送失败',
      message: '网络连接异常，请稍后重试'
    })
    refreshCaptcha()
  } finally {
    emailCodeLoading.value = false
  }
}

// 开始倒计时
const startCountdown = () => {
  emailCodeCountdown.value = 60
  const timer = setInterval(() => {
    emailCodeCountdown.value--
    if (emailCodeCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 显示成功消息
const showSuccess = (title: string, message: string) => {
  successTitle.value = title
  successMessage.value = message
  showSuccessMessage.value = true

  setTimeout(() => {
    showSuccessMessage.value = false
  }, 2000)
}

// 处理登录
const handleLogin = async () => {
  if (!loginForm.value.email || !loginForm.value.emailCode) {
    toast.warning({
      title: '请完善信息',
      message: '请填写邮箱和邮箱验证码'
    })
    return
  }

  if (!agreedToTerms.value) {
    toast.warning({
      title: '请同意条款',
      message: '请先同意服务条款和隐私政策'
    })
    return
  }

  loginLoading.value = true

  try {
    const response = await userApi.login({
      email: loginForm.value.email,
      code: loginForm.value.emailCode // 只需要邮箱验证码
    })

    if (response.code === 200) {
      // 确保正确保存token和用户信息
      const { token, userVO } = response.data

      if (token) {
        localStorage.setItem('token', token)
        console.log('Token saved:', token) // 调试日志
      } else {
        console.error('No token received from server')
      }

      if (userVO) {
        localStorage.setItem('userInfo', JSON.stringify(userVO))
        console.log('User info saved:', userVO) // 调试日志
      }

      showSuccess('登录成功', '欢迎回来，开始您的创作之旅吧！')

      setTimeout(() => {
        router.push('/copywriting')
      }, 2000)
    } else {
      toast.error({
        title: '登录失败',
        message: response.message || '请检查您的登录信息'
      })
    }
  } catch (error: any) {
    console.error('Login error:', error)
    toast.error({
      title: '登录失败',
      message: '网络连接异常，请稍后重试'
    })
  } finally {
    loginLoading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  if (!registerForm.value.email || !registerForm.value.emailCode) {
    toast.warning({
      title: '请完善信息',
      message: '请填写完整的注册信息'
    })
    return
  }

  registerLoading.value = true

  try {
    const response = await userApi.register({
      email: registerForm.value.email,
      code: registerForm.value.emailCode
    })

    if (response.code === 200) {
      // 保存token和用户信息
      localStorage.setItem('token', response.data.token)
      localStorage.setItem('userInfo', JSON.stringify(response.data.userVO))

      showSuccess('注册成功', '欢迎加入Poet，开始您的创作之旅！')

      setTimeout(() => {
        router.push('/copywriting')
      }, 2000)
    } else {
      toast.error({
        title: '注册失败',
        message: response.message || '请检查您的注册信息'
      })
    }
  } catch (error: any) {
    toast.error({
      title: '注册失败',
      message: '网络连接异常，请稍后重试'
    })
  } finally {
    registerLoading.value = false
  }
}

// 组件挂载时获取验证码
onMounted(() => {
  getCaptcha()
})
</script>

<style scoped>
/* 引入高级字体 */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&family=Playfair+Display:wght@400;500;600;700&family=Noto+Serif+SC:wght@300;400;500;600;700&display=swap');

/* 主容器 */
.poet-auth-container {
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', 'Noto Serif SC', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  font-feature-settings: 'liga' 1, 'kern' 1;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 主背景改为奢华深色主题 */
.poet-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
    #0f0f23 0%,      /* 深蓝黑 - 专业神秘 */
    #1a1a2e 15%,     /* 深紫蓝 - 科技感 */
    #16213e 30%,     /* 深蓝 - 稳重专业 */
    #0f3460 45%,     /* 中蓝 - 智慧深度 */
    #533483 60%,     /* 深紫 - 创新思维 */
    #7209b7 75%,     /* 紫色 - 前沿科技 */
    #2d1b69 90%,     /* 深紫蓝 - 回归专业 */
    #0f0f23 100%     /* 回到起点 */
  );
  background-size: 400% 400%;
  animation: gradientShift 25s ease infinite;
}

.aurora-effect {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    /* 诗意光晕效果 */
    radial-gradient(ellipse 80% 50% at 20% 0%, rgba(139, 92, 246, 0.15) 0%, transparent 50%),
    radial-gradient(ellipse 60% 40% at 80% 100%, rgba(59, 130, 246, 0.12) 0%, transparent 50%),
    radial-gradient(ellipse 100% 80% at 50% 50%, rgba(16, 185, 129, 0.08) 0%, transparent 70%);
  animation: auroraShift 25s ease-in-out infinite;
  /* 添加诗意粒子效果 */
  background-image:
    radial-gradient(2px 2px at 20px 30px, rgba(255, 255, 255, 0.4), transparent),
    radial-gradient(2px 2px at 40px 70px, rgba(255, 255, 255, 0.3), transparent),
    radial-gradient(1px 1px at 90px 40px, rgba(255, 255, 255, 0.5), transparent),
    radial-gradient(1px 1px at 130px 80px, rgba(255, 255, 255, 0.3), transparent);
  background-repeat: repeat;
  background-size: 150px 100px;
}

/* 增强动画效果 */
@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
    filter: hue-rotate(0deg) brightness(1);
  }
  25% {
    background-position: 100% 50%;
    filter: hue-rotate(10deg) brightness(1.1);
  }
  50% {
    background-position: 100% 100%;
    filter: hue-rotate(20deg) brightness(1.05);
  }
  75% {
    background-position: 0% 100%;
    filter: hue-rotate(10deg) brightness(1.1);
  }
  100% {
    background-position: 0% 50%;
    filter: hue-rotate(0deg) brightness(1);
  }
}

@keyframes auroraShift {
  0%, 100% {
    opacity: 0.7;
    transform: scale(1) rotate(0deg);
  }
  33% {
    opacity: 0.9;
    transform: scale(1.05) rotate(1deg);
  }
  66% {
    opacity: 0.8;
    transform: scale(0.98) rotate(-1deg);
  }
}

/* 浮动文字 - 优化诗意效果 */
.floating-words {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.word {
  position: absolute;
  color: rgba(255, 255, 255, 0.12);
  font-size: 3rem;
  font-weight: 300;
  font-family: 'Noto Serif SC', 'Playfair Display', serif;
  animation: float 30s linear infinite;
  letter-spacing: 0.2em;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
}

.word-1 {
  top: 15%;
  left: 8%;
  animation-delay: 0s;
}

.word-2 {
  top: 35%;
  right: 12%;
  animation-delay: 6s;
}

.word-3 {
  bottom: 45%;
  left: 15%;
  animation-delay: 12s;
}

.word-4 {
  top: 65%;
  right: 20%;
  animation-delay: 18s;
}

.word-5 {
  bottom: 25%;
  right: 8%;
  animation-delay: 24s;
}

@keyframes float {
  0% {
    transform: translateY(100vh) rotate(0deg) scale(0.8);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg) scale(1.2);
    opacity: 0;
  }
}

/* 墨滴效果 - 增强诗意 */
.ink-drops {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.ink-drop {
  position: absolute;
  width: 6px;
  height: 6px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.6) 0%, rgba(255, 255, 255, 0.2) 100%);
  border-radius: 50%;
  animation: inkDrop 12s ease-in-out infinite;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.4);
}

.drop-1 {
  top: 25%;
  left: 25%;
  animation-delay: 0s;
}

.drop-2 {
  top: 55%;
  right: 35%;
  animation-delay: 4s;
}

.drop-3 {
  bottom: 35%;
  left: 55%;
  animation-delay: 8s;
}

@keyframes inkDrop {
  0%, 100% {
    transform: scale(1) translateY(0);
    opacity: 0.4;
  }
  50% {
    transform: scale(4) translateY(-30px);
    opacity: 0.9;
  }
}

/* 布局 - 优化比例 */
.auth-layout {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  max-width: 1800px;
  margin: 0 auto;
}

/* 品牌区域 - 增强诗意感 */
.brand-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100px 80px;
  color: white;
  position: relative;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(ellipse at center,
    rgba(255, 255, 255, 0.05) 0%,
    transparent 70%
  );
  pointer-events: none;
}

.brand-content {
  max-width: 600px;
  width: 100%;
  position: relative;
  z-index: 1;
}

/* 品牌标识 - 优化布局 */
.brand-header {
  margin-bottom: 100px;
}

.brand-logo-container {
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 40px;
}

.brand-icon {
  animation: brandPulse 6s ease-in-out infinite;
  filter: drop-shadow(0 12px 40px rgba(255, 255, 255, 0.15));
}

@keyframes brandPulse {
  0%, 100% {
    transform: scale(1) rotate(0deg);
  }
  50% {
    transform: scale(1.1) rotate(5deg);
  }
}

.brand-text {
  flex: 1;
}

/* 金色强调色 */
.brand-title {
  background: linear-gradient(135deg, #d4af37 0%, #f7e98e 50%, #d4af37 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 3.5rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.02em;
  font-family: 'Playfair Display', 'Noto Serif SC', serif;
  text-shadow: 0 8px 32px rgba(255, 255, 255, 0.2);
  animation: titleGlow 4s ease-in-out infinite;
}

@keyframes titleGlow {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.2); }
}

.brand-tagline {
  font-size: 1.3rem;
  margin: 0;
  opacity: 0.95;
  font-weight: 400;
  letter-spacing: 0.1em;
  font-family: 'Noto Serif SC', 'Inter', sans-serif;
  line-height: 1.6;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 描述区域 - 增强卡片效果 */
.poet-description {
  margin-bottom: 100px;
}

.description-item {
  display: flex;
  align-items: flex-start;
  gap: 28px;
  margin-bottom: 32px;
  padding: 32px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.description-item:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.12);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.item-icon {
  font-size: 2rem;
  opacity: 0.9;
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.item-content h3 {
  font-size: 1.3rem;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: rgba(255, 255, 255, 0.95);
  font-family: 'Noto Serif SC', 'Inter', sans-serif;
  letter-spacing: 0.05em;
}

.item-content p {
  font-size: 1rem;
  margin: 0;
  opacity: 0.85;
  line-height: 1.7;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.02em;
}

/* 诗句装饰 - 增强文学感 */
.poetry-quote {
  text-align: center;
  padding: 40px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 24px;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.08);
  position: relative;
  overflow: hidden;
}

.poetry-quote::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center,
    rgba(255, 255, 255, 0.05) 0%,
    transparent 70%
  );
  animation: quoteGlow 8s ease-in-out infinite;
}

@keyframes quoteGlow {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.poetry-quote blockquote {
  margin: 0;
  font-size: 1.4rem;
  font-style: italic;
  color: rgba(255, 255, 255, 0.95);
  font-family: 'Noto Serif SC', 'Playfair Display', serif;
  line-height: 1.8;
  letter-spacing: 0.1em;
  position: relative;
  z-index: 1;
}

.poetry-quote cite {
  display: block;
  margin-top: 20px;
  font-size: 1rem;
  opacity: 0.8;
  font-style: normal;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.05em;
}

/* 表单区域 - 增强玻璃质感 */
.form-section {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  background: rgba(255, 255, 255, 0.02);
  backdrop-filter: blur(40px);
  position: relative;
}

.form-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg,
    rgba(255, 255, 255, 0.1) 0%,
    rgba(255, 255, 255, 0.05) 50%,
    rgba(255, 255, 255, 0.1) 100%
  );
  pointer-events: none;
}

/* 玻璃态效果 */
.form-container {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
  padding: 2.5rem;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.5);
  max-width: 480px;
  width: 100%;
  position: relative;
  z-index: 1;
}

/* 表单头部 - 优化标题 */
.form-header {
  text-align: center;
  margin-bottom: 48px;
}

.form-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin: 0;
  font-family: 'Inter', sans-serif;
  opacity: 0.8;
}

.form-title {
  font-size: 2.2rem;
  font-weight: 600;
  margin: 0 0 32px 0;
  background: linear-gradient(135deg, #2d3748 0%, #4a5568 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-family: 'Noto Serif SC', 'Playfair Display', serif;
  letter-spacing: 0.05em;
}

/* 移除tab切换相关样式 */
.tab-switcher {
  display: none;
}

.tab-button {
  flex: 1;
  padding: 14px 24px;
  border: none;
  background: transparent;
  color: #718096;
  font-size: 15px;
  font-weight: 500;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.02em;
  position: relative;
  z-index: 1;
}

.tab-button.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow:
    0 8px 24px rgba(102, 126, 234, 0.25),
    0 4px 12px rgba(102, 126, 234, 0.15);
  transform: translateY(-1px);
}

.tab-button:not(.active):hover {
  color: #4a5568;
  background: rgba(102, 126, 234, 0.08);
}

/* 表单样式 - 优化间距 */
.auth-form {
  margin-top: 32px;
}

.input-group {
  margin-bottom: 28px;
}

.input-label {
  display: block;
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 500;
  color: #2d3748;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.02em;
}

.input-wrapper {
  position: relative;
}

/* 输入框样式 */
.form-input {
  width: 100%;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(212, 175, 55, 0.3);
  color: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  padding: 1rem 1.25rem;
  font-size: 0.95rem;
  transition: all 0.3s ease;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.01em;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.form-input:focus {
  outline: none;
  border-color: #d4af37;
  box-shadow: 0 0 0 2px rgba(212, 175, 55, 0.2);
  background: rgba(255, 255, 255, 0.12);
  transform: translateY(-1px);
}

.form-input::placeholder {
  color: #a0aec0;
  font-weight: 400;
}

.input-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  opacity: 0.6;
  pointer-events: none;
}

/* 验证码样式 - 重新设计布局 */
.captcha-wrapper {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.captcha-input-container {
  flex: 1;
  min-width: 0;
}

.captcha-input {
  width: 100%;
}

.captcha-display {
  width: 170px;
  height: 60px;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.captcha-display:hover {
  border-color: #667eea;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.15);
  transform: translateY(-2px);
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 14px;
  /* 使用object-position将图片内容向右移动 */
  object-position: 25% center;
}

.captcha-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #718096;
  font-family: 'Inter', sans-serif;
}

.captcha-refresh-hint {
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #a0aec0;
  background: rgba(255, 255, 255, 0.95);
  padding: 2px 8px;
  border-radius: 6px;
  opacity: 0;
  transition: opacity 0.3s ease;
  font-family: 'Inter', sans-serif;
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.captcha-display:hover .captcha-refresh-hint {
  opacity: 1;
}

/* 邮箱验证码样式 - 优化布局 */
.email-code-wrapper {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.email-input-container {
  flex: 1;
  min-width: 0;
}

.send-code-button {
  padding: 18px 28px;
  border: none;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  min-width: 140px;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.02em;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
  flex-shrink: 0;
}

.send-code-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.35);
}

.send-code-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.countdown {
  color: #f56565;
  font-weight: 600;
}

/* 验证码已发送指示器 */
.code-sent-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 18px 28px;
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  font-family: 'Inter', sans-serif;
  border: 1px solid rgba(16, 185, 129, 0.2);
  min-width: 140px;
  justify-content: center;
  flex-shrink: 0;
}

.success-icon {
  font-size: 16px;
  font-weight: bold;
}

/* 按钮样式 */
.submit-button {
  width: 100%;
  background: linear-gradient(135deg, #d4af37 0%, #f7e98e 50%, #d4af37 100%);
  color: #1a1a2e;
  border: none;
  border-radius: 12px;
  padding: 1rem 2rem;
  font-weight: 600;
  font-size: 1rem;
  transition: all 0.3s ease;
  margin-top: 24px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  letter-spacing: 0.02em;
  box-shadow:
    0 12px 32px rgba(102, 126, 234, 0.3),
    0 6px 16px rgba(102, 126, 234, 0.2);
}

.submit-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s;
}

.submit-button:hover:not(:disabled)::before {
  left: 100%;
}

.submit-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(212, 175, 55, 0.4);
}

.submit-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.button-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

/* 加载动画 - 优化效果 */
.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid currentColor;
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 底部区域 - 条款同意复选框 */
.form-footer {
  margin-top: 32px;
}

.terms-agreement {
  display: flex;
  justify-content: center;
}

.checkbox-wrapper {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  cursor: pointer;
  user-select: none;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.checkbox-wrapper:hover {
  background-color: rgba(102, 126, 234, 0.05);
}

.checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid #D1D5DB;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
  flex-shrink: 0;
  margin-top: 2px;
}

.checkbox.checked {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.checkmark {
  color: white;
  font-size: 12px;
  font-weight: bold;
  animation: checkmarkAppear 0.3s ease;
}

@keyframes checkmarkAppear {
  0% {
    opacity: 0;
    transform: scale(0.5);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}

.agreement-text {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.6;
  font-family: 'Inter', sans-serif;
  cursor: pointer;
  margin: 0;
}

.terms-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
  cursor: pointer;
}

.terms-link:hover {
  color: #5a6fd8;
  text-decoration: underline;
}

/* 成功提示 - 增强动画 */
.success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(12px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.4s ease;
}

.success-message {
  background: white;
  border-radius: 24px;
  padding: 60px;
  text-align: center;
  max-width: 420px;
  box-shadow: 0 32px 80px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.5s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.success-icon {
  font-size: 4.5rem;
  margin-bottom: 28px;
  animation: bounce 0.8s ease;
}

.success-message h3 {
  font-size: 1.6rem;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #2d3748;
  font-family: 'Noto Serif SC', 'Inter', sans-serif;
}

.success-message p {
  font-size: 1rem;
  color: #718096;
  margin: 0;
  line-height: 1.6;
  font-family: 'Inter', sans-serif;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-12px);
  }
  60% {
    transform: translateY(-6px);
  }
}

/* 响应式设计 - 优化断点 */
@media (max-width: 1600px) {
  .brand-title {
    font-size: 4.5rem;
  }

  .form-container {
    padding: 50px 40px;
  }
}

@media (max-width: 1400px) {
  .auth-layout {
    grid-template-columns: 1fr 1fr;
  }

  .brand-title {
    font-size: 4rem;
  }

  .brand-section {
    padding: 80px 60px;
  }
}

@media (max-width: 1200px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }

  .brand-section {
    display: none;
  }

  .form-section {
    background: rgba(255, 255, 255, 0.08);
  }
}

@media (max-width: 768px) {
  .form-container {
    padding: 40px 32px;
    margin: 20px;
    border-radius: 24px;
  }

  .form-title {
    font-size: 1.8rem;
  }

  .captcha-wrapper {
    flex-direction: column;
    gap: 16px;
  }

  .captcha-display {
    width: 100%;
    height: 60px;
  }

  .email-code-wrapper {
    flex-direction: column;
    gap: 16px;
  }

  .send-code-button {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .poet-auth-container {
    padding: 0;
  }

  .form-section {
    padding: 20px;
  }

  .form-container {
    padding: 32px 24px;
    margin: 12px;
    border-radius: 20px;
  }

  .form-title {
    font-size: 1.6rem;
  }

  .word {
    font-size: 2.5rem;
  }
}
</style>
