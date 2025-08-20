<template>
  <div class="prompt-builder-view">
    <!-- 动态背景 -->
    <div class="background-container">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="floating-particles">
        <div class="particle" v-for="i in 8" :key="i" :style="getParticleStyle(i)"></div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="builder-container">
      <!-- AI聊天页面组件 -->
      <AIChatPage />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AIChatPage from '@/components/Chat/AIChatPage.vue'

// 粒子动画样式
const getParticleStyle = (index: number) => {
  const delay = index * 0.5
  const duration = 3 + Math.random() * 2
  return {
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}
</script>

<style scoped>
.prompt-builder-view {
  height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a1a 100%);
  color: #e8e8e8;
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.background-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.6;
  animation: float 6s ease-in-out infinite;
}

.orb-1 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #d4af37 0%, transparent 70%);
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.orb-2 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, #f4d03f 0%, transparent 70%);
  top: 60%;
  right: 20%;
  animation-delay: 2s;
}

.orb-3 {
  width: 250px;
  height: 250px;
  background: radial-gradient(circle, #b8860b 0%, transparent 70%);
  bottom: 20%;
  left: 30%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-20px) rotate(120deg); }
  66% { transform: translateY(10px) rotate(240deg); }
}

.floating-particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: linear-gradient(45deg, #d4af37, #f4d03f);
  border-radius: 50%;
  animation: particle-float 4s linear infinite;
}

.particle:nth-child(1) { left: 10%; animation-delay: 0s; }
.particle:nth-child(2) { left: 20%; animation-delay: 0.5s; }
.particle:nth-child(3) { left: 30%; animation-delay: 1s; }
.particle:nth-child(4) { left: 40%; animation-delay: 1.5s; }
.particle:nth-child(5) { left: 50%; animation-delay: 2s; }
.particle:nth-child(6) { left: 60%; animation-delay: 2.5s; }
.particle:nth-child(7) { left: 70%; animation-delay: 3s; }
.particle:nth-child(8) { left: 80%; animation-delay: 3.5s; }

@keyframes particle-float {
  0% {
    transform: translateY(100vh) scale(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) scale(1);
    opacity: 0;
  }
}

/* 主要布局 */
.builder-container {
  height: 100vh;
  position: relative;
  z-index: 2;
}
</style>