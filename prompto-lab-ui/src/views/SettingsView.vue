<template>
  <div class="settings">
    <!-- 动态背景 -->
    <div class="background-container">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="floating-particles">
        <div class="particle" v-for="i in 12" :key="i" :style="getParticleStyle(i)"></div>
      </div>
    </div>

    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">
          <div class="icon-wrapper">
            <div class="icon-inner-glow"></div>
            <span class="icon">⚙️</span>
          </div>
        </div>
        <div class="header-text">
          <h1>系统设置</h1>
          <p>管理您的应用配置和偏好设置</p>
        </div>
      </div>
    </div>

    <!-- 设置卡片网格 -->
    <div class="settings-container">
      <div class="settings-grid">
        <!-- AI配置卡片 -->
        <div class="setting-card featured" @click="goToApiConfig">
          <div class="card-background"></div>
          <div class="card-header">
            <div class="card-icon">
              <div class="icon-glow"></div>
              <span class="icon">🔗</span>
            </div>
            <div class="card-title">
              <h3>AI配置</h3>
              <p>配置AI模型接口、参数和所有AI相关设置</p>
            </div>
          </div>
          <div class="card-content">
            <div class="setting-preview">
              <div class="preview-item">
                <span class="preview-label">已配置模型</span>
                <span class="preview-value">{{ configuredModelsCount }}</span>
              </div>
              <div class="preview-item">
                <span class="preview-label">系统状态</span>
                <span class="preview-status configured">运行中</span>
              </div>
              <div class="preview-item">
                <span class="preview-label">最后更新</span>
                <span class="preview-value">{{ lastUpdateTime }}</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <span class="card-action">管理配置 →</span>
          </div>
        </div>

        <!-- 其他设置卡片 -->
        <div class="setting-card" @click="goToThemeSettings">
          <div class="card-background"></div>
          <div class="card-header">
            <div class="card-icon">
              <div class="icon-glow"></div>
              <span class="icon">🎨</span>
            </div>
            <div class="card-title">
              <h3>主题设置</h3>
              <p>自定义界面主题和外观</p>
            </div>
          </div>
          <div class="card-content">
            <div class="setting-preview">
              <div class="preview-item">
                <span class="preview-label">当前主题</span>
                <span class="preview-value">奢华暗色</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <span class="card-action coming-soon">即将推出</span>
          </div>
        </div>

        <div class="setting-card" @click="goToNotificationSettings">
          <div class="card-background"></div>
          <div class="card-header">
            <div class="card-icon">
              <div class="icon-glow"></div>
              <span class="icon">🔔</span>
            </div>
            <div class="card-title">
              <h3>通知设置</h3>
              <p>管理消息通知和提醒</p>
            </div>
          </div>
          <div class="card-content">
            <div class="setting-preview">
              <div class="preview-item">
                <span class="preview-label">邮件通知</span>
                <span class="preview-value">已开启</span>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <span class="card-action coming-soon">即将推出</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { toast } from '@/utils/toast'

const router = useRouter()

// 响应式数据
const configuredModelsCount = ref(0)
const lastUpdateTime = ref('--')

// 粒子样式生成
const getParticleStyle = (index: number) => {
  const size = Math.random() * 4 + 2
  const left = Math.random() * 100
  const animationDelay = Math.random() * 6
  const animationDuration = Math.random() * 3 + 4
  
  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${animationDelay}s`,
    animationDuration: `${animationDuration}s`
  }
}

// 跳转到AI配置页面
const goToApiConfig = () => {
  router.push('/settings/api-config')
}

// 其他设置页面跳转（预留）
const goToThemeSettings = () => {
  toast.info({
    title: '功能开发中',
    message: '主题设置功能即将推出'
  })
}

const goToNotificationSettings = () => {
  toast.info({
    title: '功能开发中',
    message: '通知设置功能即将推出'
  })
}

// 获取系统概览信息
const fetchSystemOverview = async () => {
  try {
    const response = await fetch('/sf/api/overview')
    if (response.ok) {
      const data = await response.json()
      configuredModelsCount.value = data.enabledModels || 0
      lastUpdateTime.value = new Date(data.lastUpdate).toLocaleString()
    }
  } catch (error) {
    console.error('Failed to fetch system overview:', error)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchSystemOverview()
})
</script>

<style scoped>
.settings {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  color: #e8e8e8;
  font-family: 'Inter', 'SF Pro Display', -apple-system, BlinkMacSystemFont, sans-serif;
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.background-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;
  animation: float 20s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: linear-gradient(45deg, #d4af37, #ffd700);
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: linear-gradient(45deg, #c0c0c0, #ffffff);
  top: 60%;
  right: 10%;
  animation-delay: -10s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: linear-gradient(45deg, #d4af37, #b8860b);
  bottom: 20%;
  left: 50%;
  animation-delay: -5s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  background: linear-gradient(45deg, #d4af37, #ffd700);
  border-radius: 50%;
  opacity: 0.6;
  animation: particle-float 6s ease-in-out infinite;
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0;
  }
  10%, 90% {
    opacity: 0.6;
  }
  50% {
    transform: translateY(-100px) rotate(180deg);
    opacity: 1;
  }
}

/* 页面标题 */
.page-header {
  margin-bottom: 60px;
  padding: 60px 40px 0;
  position: relative;
  z-index: 2;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 32px;
  background: rgba(8, 8, 8, 0.85);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 24px;
  padding: 40px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.02),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  overflow: hidden;
}

.header-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(212, 175, 55, 0.03) 0%, 
    transparent 30%, 
    transparent 70%, 
    rgba(212, 175, 55, 0.03) 100%
  );
  animation: header-shimmer 8s ease-in-out infinite;
}

@keyframes header-shimmer {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.header-icon {
  flex-shrink: 0;
}

.icon-wrapper {
  width: 88px;
  height: 88px;
  background: linear-gradient(135deg, #d4af37 0%, #f4d03f 50%, #d4af37 100%);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 12px 32px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    inset 0 -1px 0 rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.icon-wrapper:hover {
  transform: scale(1.05) rotate(5deg);
  box-shadow: 
    0 16px 40px rgba(212, 175, 55, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.icon-inner-glow {
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), transparent);
  border-radius: 20px;
  pointer-events: none;
}

.header-icon .icon {
  font-size: 40px;
  color: #1a1a1a;
  z-index: 2;
  position: relative;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.header-text {
  flex: 1;
  z-index: 2;
  position: relative;
}

.header-text h1 {
  font-size: 3rem;
  font-weight: 800;
  background: linear-gradient(135deg, #ffffff 0%, #d4af37 50%, #f4d03f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 12px 0;
  letter-spacing: -1px;
  line-height: 1.1;
}

.header-text p {
  font-size: 1.125rem;
  color: #a0a0a0;
  margin: 0;
  font-weight: 400;
  line-height: 1.6;
}

/* 设置容器 */
.settings-container {
  padding: 0 40px 60px;
  position: relative;
  z-index: 2;
}

/* 设置网格 */
.settings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 32px;
  max-width: 1400px;
  margin: 0 auto;
}

/* 设置卡片 */
.setting-card {
  background: rgba(8, 8, 8, 0.85);
  backdrop-filter: blur(24px) saturate(180%);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 24px;
  padding: 32px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(255, 255, 255, 0.02),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.card-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, 
    rgba(212, 175, 55, 0.03) 0%, 
    transparent 30%, 
    transparent 70%, 
    rgba(212, 175, 55, 0.03) 100%
  );
  opacity: 0;
  transition: opacity 0.4s ease;
}

.setting-card.featured {
  background: linear-gradient(135deg, 
    rgba(212, 175, 55, 0.15) 0%, 
    rgba(8, 8, 8, 0.9) 30%, 
    rgba(8, 8, 8, 0.9) 70%, 
    rgba(212, 175, 55, 0.15) 100%
  );
  border-color: rgba(212, 175, 55, 0.4);
  box-shadow: 
    0 12px 40px rgba(212, 175, 55, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.setting-card:hover {
  transform: translateY(-8px) scale(1.02);
  border-color: rgba(212, 175, 55, 0.4);
  box-shadow: 
    0 16px 48px rgba(0, 0, 0, 0.5),
    0 0 0 1px rgba(212, 175, 55, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.setting-card:hover .card-background {
  opacity: 1;
}

.setting-card.featured:hover {
  transform: translateY(-8px) scale(1.04);
  box-shadow: 
    0 20px 60px rgba(212, 175, 55, 0.3),
    0 0 0 1px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.15);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 32px;
  position: relative;
  z-index: 2;
}

.card-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #d4af37 0%, #f4d03f 50%, #d4af37 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 
    0 8px 24px rgba(212, 175, 55, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    inset 0 -1px 0 rgba(0, 0, 0, 0.2);
  position: relative;
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.setting-card:hover .card-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 
    0 12px 32px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.icon-glow {
  position: absolute;
  top: 2px;
  left: 2px;
  right: 2px;
  bottom: 2px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.2), transparent);
  border-radius: 16px;
  pointer-events: none;
}

.card-icon .icon {
  font-size: 28px;
  color: #1a1a1a;
  z-index: 2;
  position: relative;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.card-title {
  flex: 1;
}

.card-title h3 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 8px 0;
  line-height: 1.2;
}

.card-title p {
  font-size: 0.95rem;
  color: #a0a0a0;
  margin: 0;
  line-height: 1.5;
  font-weight: 400;
}

/* 卡片内容 */
.card-content {
  margin-bottom: 32px;
  position: relative;
  z-index: 2;
}

.setting-preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.preview-item:hover {
  background: rgba(212, 175, 55, 0.1);
  border-color: rgba(212, 175, 55, 0.3);
  transform: translateX(4px);
}

.preview-label {
  font-size: 0.9rem;
  color: #a0a0a0;
  font-weight: 500;
}

.preview-value {
  font-size: 0.9rem;
  color: #ffffff;
  font-weight: 600;
}

.preview-status {
  font-size: 0.8rem;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 600;
}

.preview-status.configured {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.2), rgba(34, 197, 94, 0.1));
  color: #22c55e;
  border: 1px solid rgba(34, 197, 94, 0.3);
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: flex-end;
  position: relative;
  z-index: 2;
}

.card-action {
  font-size: 0.95rem;
  font-weight: 600;
  color: #d4af37;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-action.coming-soon {
  color: #6b7280;
}

.setting-card:hover .card-action {
  color: #f4d03f;
  transform: translateX(4px);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .settings-grid {
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    gap: 24px;
  }

  .settings-container,
  .page-header {
    padding-left: 24px;
    padding-right: 24px;
  }
}

@media (max-width: 768px) {
  .settings {
    padding-top: 20px;
  }

  .settings-container {
    padding: 0 16px 40px;
  }

  .page-header {
    padding: 40px 16px 0;
    margin-bottom: 40px;
  }

  .header-content {
    padding: 24px;
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .icon-wrapper {
    width: 64px;
    height: 64px;
  }

  .header-icon .icon {
    font-size: 28px;
  }

  .header-text h1 {
    font-size: 2rem;
  }

  .header-text p {
    font-size: 1rem;
  }

  .settings-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .setting-card {
    padding: 24px;
  }

  .card-header {
    gap: 16px;
  }

  .card-icon {
    width: 48px;
    height: 48px;
  }

  .card-icon .icon {
    font-size: 20px;
  }

  .card-title h3 {
    font-size: 1.25rem;
  }

  .setting-card:hover {
    transform: translateY(-4px);
  }

  .setting-card.featured:hover {
    transform: translateY(-4px);
  }
}
</style>
