<template>
  <Teleport to="body">
    <Transition
      name="guide"
      enter-active-class="guide-enter-active"
      leave-active-class="guide-leave-active"
      enter-from-class="guide-enter-from"
      leave-to-class="guide-leave-to"
    >
      <div v-if="visible" class="login-guide-overlay" @click="handleOverlayClick">
        <div class="login-guide-container" @click.stop>
          <!-- 背景装饰 -->
          <div class="guide-bg-decoration">
            <div class="floating-circle circle-1"></div>
            <div class="floating-circle circle-2"></div>
            <div class="floating-circle circle-3"></div>
          </div>
          
          <!-- 主要内容 -->
          <div class="guide-content">
            <!-- 图标 -->
            <div class="guide-icon">
              <div class="icon-wrapper">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 7.5V9M21 17V15L15 15.5V17M11 14V16H13V20H11V22H7V20H9V16H7V14H11ZM3 12C3 10.9 3.9 10 5 10S7 10.9 7 12 6.1 14 5 14 3 13.1 3 12Z" fill="currentColor"/>
                </svg>
              </div>
            </div>
            
            <!-- 标题和描述 -->
            <div class="guide-header">
              <h2 class="guide-title">{{ title }}</h2>
              <p class="guide-description">{{ description }}</p>
            </div>
            
            <!-- 进度条 -->
            <div v-if="showProgress" class="guide-progress">
              <div class="progress-track">
                <div 
                  class="progress-fill" 
                  :style="{ width: progressWidth + '%' }"
                ></div>
              </div>
              <div class="progress-text">{{ Math.round(progressWidth) }}%</div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="guide-actions">
              <button 
                v-if="showCancel" 
                @click="handleCancel" 
                class="guide-btn guide-btn-secondary"
              >
                {{ cancelText }}
              </button>
              <button 
                @click="handleConfirm" 
                class="guide-btn guide-btn-primary"
                :disabled="loading"
              >
                <span v-if="loading" class="btn-loading"></span>
                <span>{{ confirmText }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface Props {
  visible?: boolean
  title?: string
  description?: string
  confirmText?: string
  cancelText?: string
  showCancel?: boolean
  showProgress?: boolean
  autoRedirect?: boolean
  redirectDelay?: number
  loading?: boolean
}

interface Emits {
  confirm: []
  cancel: []
  close: []
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  title: '登录已过期',
  description: '您的登录状态已过期，需要重新登录以继续使用',
  confirmText: '立即登录',
  cancelText: '稍后再说',
  showCancel: true,
  showProgress: true,
  autoRedirect: true,
  redirectDelay: 3000,
  loading: false
})

const emit = defineEmits<Emits>()

const progressWidth = ref(0)
let progressTimer: number | null = null
let redirectTimer: number | null = null

const handleOverlayClick = () => {
  if (props.showCancel) {
    handleCancel()
  }
}

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  emit('cancel')
}

const startProgress = () => {
  if (!props.showProgress || !props.autoRedirect) return
  
  progressWidth.value = 0
  const duration = props.redirectDelay
  const interval = 50
  const increment = (interval / duration) * 100
  
  progressTimer = setInterval(() => {
    progressWidth.value += increment
    if (progressWidth.value >= 100) {
      progressWidth.value = 100
      clearInterval(progressTimer!)
      // 自动确认
      setTimeout(() => {
        handleConfirm()
      }, 200)
    }
  }, interval)
}

const stopProgress = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

onMounted(() => {
  if (props.visible && props.autoRedirect) {
    setTimeout(() => {
      startProgress()
    }, 500)
  }
})

onUnmounted(() => {
  stopProgress()
  if (redirectTimer) {
    clearTimeout(redirectTimer)
  }
})
</script>

<style scoped>
.login-guide-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.login-guide-container {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 24px;
  padding: 40px;
  max-width: 480px;
  width: 100%;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  overflow: hidden;
}

.guide-bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
}

.floating-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: -60px;
  right: -60px;
  animation-delay: 0s;
}

.circle-2 {
  width: 80px;
  height: 80px;
  bottom: -40px;
  left: -40px;
  animation-delay: 2s;
}

.circle-3 {
  width: 60px;
  height: 60px;
  top: 50%;
  left: -30px;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.7;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 1;
  }
}

.guide-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
}

.guide-icon {
  margin-bottom: 24px;
}

.icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(255, 255, 255, 0.4);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 20px rgba(255, 255, 255, 0);
  }
}

.guide-header {
  margin-bottom: 32px;
}

.guide-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
  background: linear-gradient(135deg, #ffffff 0%, #f0f0f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.guide-description {
  font-size: 16px;
  line-height: 1.6;
  margin: 0;
  opacity: 0.9;
}

.guide-progress {
  margin-bottom: 32px;
}

.progress-track {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffffff 0%, #f0f0f0 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.progress-text {
  font-size: 14px;
  opacity: 0.8;
}

.guide-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.guide-btn {
  padding: 14px 28px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 120px;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.guide-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.guide-btn:hover::before {
  left: 100%;
}

.guide-btn-primary {
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.guide-btn-primary:hover {
  background: white;
  transform: translateY(-2px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.2);
}

.guide-btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.guide-btn-secondary {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.guide-btn-secondary:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.btn-loading {
  width: 16px;
  height: 16px;
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

/* 过渡动画 */
.guide-enter-active {
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
}

.guide-leave-active {
  transition: all 0.3s cubic-bezier(0.755, 0.05, 0.855, 0.06);
}

.guide-enter-from {
  opacity: 0;
  transform: scale(0.8) translateY(20px);
}

.guide-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(-10px);
}

/* 响应式设计 */
@media (max-width: 640px) {
  .login-guide-container {
    padding: 32px 24px;
    margin: 20px;
  }
  
  .guide-title {
    font-size: 24px;
  }
  
  .guide-description {
    font-size: 15px;
  }
  
  .guide-actions {
    flex-direction: column;
  }
  
  .guide-btn {
    width: 100%;
  }
}
</style>