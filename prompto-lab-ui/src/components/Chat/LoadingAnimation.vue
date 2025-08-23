<template>
  <div class="loading-animation">
    <div class="loading-container">
      <!-- 动态图标 -->
      <div class="loading-icon">
        <div class="icon-wrapper">
          <span class="main-icon">{{ currentStep.icon }}</span>
          <div class="icon-pulse"></div>
        </div>
      </div>
      
      <!-- 动态文字 -->
      <div class="loading-text">
        <h3 class="loading-title">{{ currentStep.title }}</h3>
        <p class="loading-subtitle">{{ currentStep.subtitle }}</p>
      </div>
      
      <!-- 进度条 -->
      <div class="progress-container">
        <div class="progress-bar">
          <div 
            class="progress-fill" 
            :style="{ width: `${progress}%` }"
          ></div>
          <div class="progress-glow"></div>
        </div>
        <div class="progress-text">{{ Math.round(progress) }}%</div>
      </div>
      
      <!-- 装饰性粒子 -->
      <div class="loading-particles">
        <div 
          v-for="i in 8" 
          :key="i"
          class="particle"
          :style="{
            '--delay': `${i * 0.2}s`,
            '--angle': `${i * 45}deg`
          }"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'

interface LoadingStep {
  icon: string
  title: string
  subtitle: string
  duration: number
}

interface Props {
  type?: 'thinking' | 'processing' | 'analyzing' | 'generating'
  duration?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'thinking',
  duration: 3000
})

// 不同类型的加载步骤
const loadingSteps: Record<string, LoadingStep[]> = {
  thinking: [
    { icon: '🤔', title: '正在思考中...', subtitle: 'AI正在理解您的问题', duration: 1000 },
    { icon: '💭', title: '分析问题', subtitle: '深度解析问题内容', duration: 1000 },
    { icon: '✨', title: '准备回答', subtitle: '整理思路，准备最佳答案', duration: 1000 }
  ],
  processing: [
    { icon: '⚡', title: '处理中...', subtitle: '正在处理您的请求', duration: 1000 },
    { icon: '🔄', title: '数据分析', subtitle: '分析和整理相关信息', duration: 1000 },
    { icon: '🎯', title: '生成结果', subtitle: '准备个性化回答', duration: 1000 }
  ],
  analyzing: [
    { icon: '🔍', title: '正在分析...', subtitle: '深度分析您的回答', duration: 1000 },
    { icon: '📊', title: '数据处理', subtitle: '处理和验证信息', duration: 1000 },
    { icon: '🎨', title: '优化结果', subtitle: '为您定制最佳体验', duration: 1000 }
  ],
  generating: [
    { icon: '🚀', title: '生成中...', subtitle: 'AI正在为您生成内容', duration: 1000 },
    { icon: '🎭', title: '创意构思', subtitle: '发挥创意，打造独特内容', duration: 1000 },
    { icon: '🌟', title: '完善细节', subtitle: '精雕细琢，追求完美', duration: 1000 }
  ]
}

const currentStepIndex = ref(0)
const progress = ref(0)
const startTime = ref(0)
const animationFrame = ref<number>()

const currentSteps = computed(() => loadingSteps[props.type] || loadingSteps.thinking)
const currentStep = computed(() => currentSteps.value[currentStepIndex.value] || currentSteps.value[0])

const updateProgress = () => {
  const elapsed = Date.now() - startTime.value
  const totalDuration = props.duration
  const newProgress = Math.min((elapsed / totalDuration) * 100, 100)
  
  progress.value = newProgress
  
  // 根据进度切换步骤
  const stepProgress = newProgress / 100 * currentSteps.value.length
  const newStepIndex = Math.min(Math.floor(stepProgress), currentSteps.value.length - 1)
  
  if (newStepIndex !== currentStepIndex.value) {
    currentStepIndex.value = newStepIndex
  }
  
  if (newProgress < 100) {
    animationFrame.value = requestAnimationFrame(updateProgress)
  }
}

const startAnimation = () => {
  startTime.value = Date.now()
  progress.value = 0
  currentStepIndex.value = 0
  updateProgress()
}

const stopAnimation = () => {
  if (animationFrame.value) {
    cancelAnimationFrame(animationFrame.value)
  }
}

onMounted(() => {
  startAnimation()
})

onUnmounted(() => {
  stopAnimation()
})

// 暴露方法供父组件调用
defineExpose({
  restart: startAnimation,
  stop: stopAnimation
})
</script>

<style scoped>
.loading-animation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-container {
  position: relative;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.2);
  max-width: 400px;
  width: 90%;
  text-align: center;
  overflow: hidden;
}

.loading-icon {
  position: relative;
  margin-bottom: 24px;
}

.icon-wrapper {
  position: relative;
  display: inline-block;
}

.main-icon {
  font-size: 3rem;
  display: block;
  animation: iconBounce 2s ease-in-out infinite;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
}

.icon-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 80px;
  height: 80px;
  border: 2px solid #667eea;
  border-radius: 50%;
  animation: iconPulse 2s ease-in-out infinite;
  opacity: 0.6;
}

.loading-text {
  margin-bottom: 32px;
}

.loading-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 8px;
  animation: textSlideIn 0.6s ease-out;
}

.loading-subtitle {
  font-size: 1rem;
  color: #718096;
  margin: 0;
  animation: textSlideIn 0.6s ease-out 0.2s both;
}

.progress-container {
  margin-bottom: 20px;
}

.progress-bar {
  position: relative;
  width: 100%;
  height: 8px;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 4px;
  transition: width 0.3s ease;
  position: relative;
}

.progress-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  animation: progressGlow 2s ease-in-out infinite;
}

.progress-text {
  font-size: 0.9rem;
  font-weight: 500;
  color: #667eea;
}

.loading-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: #667eea;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform-origin: 0 0;
  animation: particleFloat 3s ease-in-out infinite;
  animation-delay: var(--delay);
  opacity: 0.6;
}

.particle:nth-child(even) {
  background: #764ba2;
}

/* 动画定义 */
@keyframes iconBounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes iconPulse {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1);
    opacity: 0.6;
  }
  50% { 
    transform: translate(-50%, -50%) scale(1.2);
    opacity: 0.3;
  }
}

@keyframes textSlideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes progressGlow {
  0%, 100% { transform: translateX(-100%); }
  50% { transform: translateX(100%); }
}

@keyframes particleFloat {
  0%, 100% {
    transform: rotate(var(--angle)) translateX(0) rotate(calc(-1 * var(--angle)));
    opacity: 0;
  }
  50% {
    transform: rotate(var(--angle)) translateX(60px) rotate(calc(-1 * var(--angle)));
    opacity: 0.6;
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .loading-container {
    padding: 30px 20px;
    margin: 20px;
  }
  
  .main-icon {
    font-size: 2.5rem;
  }
  
  .loading-title {
    font-size: 1.3rem;
  }
  
  .loading-subtitle {
    font-size: 0.9rem;
  }
}
</style>