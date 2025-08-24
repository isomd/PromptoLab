<template>
  <div class="chat-main">
    <!-- 动态背景粒子 -->
    <div class="chat-background">
      <div class="background-particles">
        <div class="bg-particle" v-for="i in 8" :key="i"></div>
      </div>
    </div>

    <div class="chat-header">
      <div class="header-content">
        <div class="header-icon">
          <div class="icon-glow"></div>
          <span class="icon">🤖</span>
        </div>
        <div class="header-text">
          <h2>AI 对话助手</h2>
          <div class="status-indicator">
            <div class="status-dot"></div>
            <span>智能分析中</span>
          </div>
        </div>
      </div>
      <div class="header-actions">
        <button class="action-btn" title="设置">
          <span>⚙</span>
        </button>
        <button class="action-btn" title="历史">
          <span>📋</span>
        </button>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div class="chat-history">
        <MessageItem v-for="message in messages" :key="message.id" :message="message"
        :is-streaming="isMessageStreaming(message.id)" />
      </div>
      
      <div class="chat-input">
        <div class="input-container">
          <div class="input-wrapper">
            <div class="input-glow"></div>
            <input v-model="inputMessage" @keydown="handleKeydown" placeholder="输入您的问题，让AI为您解答..." rows="1"
              ref="textareaRef" class="message-input"></input>
            <div class="input-actions">
              <button class="attachment-btn" title="附件">
                <span>📎</span>
              </button>
              <button @click="sendMessage" :disabled="!inputMessage.trim() || isLoading" class="send-button"
                :class="{ active: inputMessage.trim() && !isLoading }">
                <div class="btn-glow"></div>
                <span class="btn-icon">⚡</span>
                <span class="btn-text">发送</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import DynamicSelect from './DynamicSelect.vue'
import MessageItem from './MessageItem.vue'
interface Message {
  id: string
  content: string
  type: 'user' | 'assistant'
  timestamp: Date
}

interface Props {
  messages: Message[]
  isLoading?: boolean
  streamingNodeId?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  sendMessage: [content: string]
}>()

const inputMessage = ref('')
const messagesContainer = ref<HTMLElement>()
const textareaRef = ref<HTMLTextAreaElement>()

// 判断消息是否正在流式输出
const isMessageStreaming = (messageId: string) => {
  return props.streamingNodeId === messageId
}

const sendMessage = () => {
  if (!inputMessage.value.trim() || props.isLoading) return

  emit('sendMessage', inputMessage.value.trim())
  inputMessage.value = ''

  // 重置textarea高度
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto'
  }
}

const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    sendMessage()
  }

  // 自动调整textarea高度
  nextTick(() => {
    if (textareaRef.value) {
      textareaRef.value.style.height = 'auto'
      textareaRef.value.style.height = textareaRef.value.scrollHeight + 'px'
    }
  })
}

// 自动滚动到底部
watch(() => props.messages.length, () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
})
</script>

<style scoped>
/* 主容器 - 黑金风格 */
.chat-main {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(135deg, rgba(10, 10, 10, 0.95) 0%, rgba(20, 20, 20, 0.95) 100%);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.chat-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.background-particles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.bg-particle {
  position: absolute;
  background: linear-gradient(45deg, #d4af37, #ffd700);
  border-radius: 50%;
  opacity: 0.1;
  animation: bg-float 12s ease-in-out infinite;
}

.bg-particle:nth-child(1) {
  width: 4px;
  height: 4px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.bg-particle:nth-child(2) {
  width: 6px;
  height: 6px;
  top: 60%;
  left: 20%;
  animation-delay: -2s;
}

.bg-particle:nth-child(3) {
  width: 3px;
  height: 3px;
  top: 40%;
  right: 15%;
  animation-delay: -4s;
}

.bg-particle:nth-child(4) {
  width: 5px;
  height: 5px;
  bottom: 30%;
  left: 30%;
  animation-delay: -6s;
}

.bg-particle:nth-child(5) {
  width: 4px;
  height: 4px;
  top: 80%;
  right: 25%;
  animation-delay: -8s;
}

.bg-particle:nth-child(6) {
  width: 7px;
  height: 7px;
  top: 10%;
  right: 40%;
  animation-delay: -10s;
}

.bg-particle:nth-child(7) {
  width: 3px;
  height: 3px;
  bottom: 60%;
  right: 10%;
  animation-delay: -12s;
}

.bg-particle:nth-child(8) {
  width: 5px;
  height: 5px;
  bottom: 20%;
  left: 60%;
  animation-delay: -14s;
}

@keyframes bg-float {

  0%,
  100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.1;
  }

  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.3;
  }
}

/* 聊天头部 - 奢华设计 */
.chat-header {
  padding: 24px 32px;
  background: linear-gradient(135deg, rgba(8, 8, 8, 0.9), rgba(18, 18, 18, 0.9));
  backdrop-filter: blur(24px) saturate(180%);
  border-bottom: 1px solid rgba(212, 175, 55, 0.15);
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow:
    0 1px 0 rgba(212, 175, 55, 0.1),
    0 8px 32px rgba(0, 0, 0, 0.3);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #d4af37 0%, #f4d03f 50%, #d4af37 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  box-shadow:
    0 8px 24px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.icon-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: linear-gradient(45deg, #d4af37, transparent, #f4d03f);
  border-radius: 18px;
  opacity: 0.6;
  animation: icon-pulse 3s ease-in-out infinite;
}

@keyframes icon-pulse {

  0%,
  100% {
    opacity: 0.6;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.05);
  }
}

.icon {
  font-size: 28px;
  z-index: 2;
  position: relative;
}

.header-text h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #ffffff 0%, #d4af37 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: rgba(34, 197, 94, 0.1);
  border: 1px solid rgba(34, 197, 94, 0.3);
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #22c55e;
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(34, 197, 94, 0.6);
  animation: status-pulse 2s ease-in-out infinite;
}

@keyframes status-pulse {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.5;
  }
}

.status-indicator span {
  font-size: 12px;
  color: #22c55e;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  width: 44px;
  height: 44px;
  background: rgba(15, 15, 15, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  font-size: 18px;
}

.action-btn:hover {
  border-color: rgba(212, 175, 55, 0.5);
  background: rgba(212, 175, 55, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.2);
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  position: relative;
  z-index: 2;
  scroll-behavior: smooth;
}
.message {
  margin-bottom: 32px;
  display: flex;
  gap: 16px;
  animation: message-appear 0.5s ease-out;
}

@keyframes message-appear {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.avatar-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s ease;
}

.avatar-wrapper.user {
  background: linear-gradient(135deg, #22c55e, #16a34a);
  box-shadow: 0 4px 16px rgba(34, 197, 94, 0.3);
}

.avatar-wrapper.assistant {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
}

.avatar-glow {
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 50%;
  opacity: 0.6;
  animation: avatar-glow 3s ease-in-out infinite;
}

.avatar-wrapper.user .avatar-glow {
  background: linear-gradient(45deg, #22c55e, transparent, #16a34a);
}

.avatar-wrapper.assistant .avatar-glow {
  background: linear-gradient(45deg, #3b82f6, transparent, #1d4ed8);
}

@keyframes avatar-glow {

  0%,
  100% {
    opacity: 0.6;
    transform: scale(1);
  }

  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

.avatar-icon {
  font-size: 20px;
  z-index: 2;
  position: relative;
}

.message-content {
  flex: 1;
  max-width: 70%;
}

.message-bubble {
  padding: 20px 24px;
  border-radius: 20px;
  position: relative;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.message-bubble.user {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.15), rgba(22, 163, 74, 0.1));
  border-color: rgba(34, 197, 94, 0.3);
  margin-left: auto;
}

.message-bubble.assistant {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(29, 78, 216, 0.1));
  border-color: rgba(59, 130, 246, 0.3);
}

.bubble-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 20px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.message-bubble.user .bubble-glow {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.1), transparent);
}

.message-bubble.assistant .bubble-glow {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), transparent);
}

.message-bubble:hover .bubble-glow {
  opacity: 1;
}

.message-text {
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
  color: #e8e8e8;
  font-size: 15px;
  font-weight: 400;
  position: relative;
  z-index: 2;
}

/* 流式输出样式 */
.message-text.streaming {
  position: relative;
}

.cursor-blink {
  display: inline-block;
  animation: blink 1s infinite;
  color: #d4af37;
  font-weight: bold;
  margin-left: 2px;
}

@keyframes blink {

  0%,
  50% {
    opacity: 1;
  }

  51%,
  100% {
    opacity: 0;
  }
}

/* 流式消息的特殊效果 */
.message-text.streaming::after {
  content: '';
  position: absolute;
  right: -20px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: linear-gradient(45deg, #d4af37, #f4d03f);
  border-radius: 2px;
  animation: typing-indicator 1.5s ease-in-out infinite;
}

@keyframes typing-indicator {

  0%,
  100% {
    opacity: 0.3;
  }

  50% {
    opacity: 1;
  }
}

.message-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 8px;
  color: #a0a0a0;
  font-weight: 500;
  position: relative;
  z-index: 2;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 2;
}

.typing-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(45deg, #3b82f6, #1d4ed8);
  animation: typing-bounce 1.4s infinite ease-in-out;
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.4);
}

.typing-dots span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dots span:nth-child(2) {
  animation-delay: -0.16s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0s;
}

@keyframes typing-bounce {

  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }

  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

.typing-text {
  font-size: 14px;
  color: #a0a0a0;
  font-weight: 500;
  font-style: italic;
}
.chat-history{
  min-height: calc(100%-240px);
}
/* 输入区域 - 奢华设计 */
.chat-input {
  padding: 24px 32px 32px 24px;
  background: linear-gradient(135deg, rgba(8, 8, 8, 0.9), rgba(18, 18, 18, 0.9));
  backdrop-filter: blur(24px) saturate(180%);
  border-top: 1px solid rgba(212, 175, 55, 0.15);
  z-index: 2;
  box-shadow:
    0 -1px 0 rgba(212, 175, 55, 0.1),
    0 -8px 32px rgba(0, 0, 0, 0.3);
}

.input-container {
  max-width: 100%;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 16px;
  background: linear-gradient(135deg, rgba(15, 15, 15, 0.9), rgba(25, 25, 25, 0.9));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
  padding: 16px 20px;
  position: relative;
  backdrop-filter: blur(20px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.input-wrapper:focus-within {
  border-color: rgba(212, 175, 55, 0.5);
  box-shadow:
    0 12px 40px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(212, 175, 55, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
}

.input-glow {
  position: absolute;
  top: -1px;
  left: -1px;
  right: -1px;
  bottom: -1px;
  background: linear-gradient(45deg, rgba(212, 175, 55, 0.3), transparent, rgba(212, 175, 55, 0.3));
  border-radius: 20px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.input-wrapper:focus-within .input-glow {
  opacity: 1;
}

.message-input {
  flex: 1;
  min-height: 24px;
  max-height: 150px;
  background: transparent;
  border: none;
  outline: none;
  resize: none;
  font-family: inherit;
  font-size: 15px;
  line-height: 1.5;
  color: #e8e8e8;
  font-weight: 400;
  overflow-y: auto;
}

.message-input::placeholder {
  color: #666;
  font-weight: 400;
}

.input-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.attachment-btn {
  width: 40px;
  height: 40px;
  background: rgba(15, 15, 15, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 16px;
  backdrop-filter: blur(10px);
}

.attachment-btn:hover {
  border-color: rgba(212, 175, 55, 0.4);
  background: rgba(212, 175, 55, 0.1);
  transform: scale(1.05);
}

.send-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: rgba(15, 15, 15, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  color: #888;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.send-button.active {
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  border-color: #d4af37;
  color: #1a1a1a;
  box-shadow:
    0 8px 24px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.send-button.active:hover {
  transform: translateY(-2px);
  box-shadow:
    0 12px 32px rgba(212, 175, 55, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.send-button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.btn-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(212, 175, 55, 0.2), transparent, rgba(212, 175, 55, 0.2));
  border-radius: 12px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.send-button.active .btn-glow {
  opacity: 1;
  animation: btn-shimmer 2s ease-in-out infinite;
}

@keyframes btn-shimmer {

  0%,
  100% {
    opacity: 0.5;
  }

  50% {
    opacity: 1;
  }
}

.btn-icon {
  font-size: 16px;
  position: relative;
  z-index: 2;
}

.btn-text {
  position: relative;
  z-index: 2;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(15, 15, 15, 0.3);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.6));
  border-radius: 3px;
  transition: all 0.3s ease;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, rgba(212, 175, 55, 0.6), rgba(212, 175, 55, 0.8));
}

.message-input::-webkit-scrollbar {
  width: 4px;
}

.message-input::-webkit-scrollbar-track {
  background: transparent;
}

.message-input::-webkit-scrollbar-thumb {
  background: rgba(212, 175, 55, 0.3);
  border-radius: 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-header {
    padding: 16px 20px;
  }

  .header-content {
    gap: 12px;
  }

  .header-icon {
    width: 48px;
    height: 48px;
  }

  .icon {
    font-size: 24px;
  }

  .header-text h2 {
    font-size: 20px;
  }

  .chat-messages {
    padding: 20px;
  }

  .message {
    margin-bottom: 24px;
  }

  .message-content {
    max-width: 85%;
  }

  .chat-input {
    padding: 16px 20px 20px;
  }

  .input-wrapper {
    padding: 12px 16px;
  }

  .chat-background {
    display: none;
  }
}

@media (max-width: 480px) {
  .header-actions {
    display: none;
  }

  .avatar-wrapper {
    width: 40px;
    height: 40px;
  }

  .avatar-icon {
    font-size: 16px;
  }

  .message-bubble {
    padding: 16px 20px;
  }

  .message-content {
    max-width: 90%;
  }
}
</style>