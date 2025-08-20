<template>
  <div class="question-renderer">
    <!-- 动态背景粒子 -->
    <div class="question-background">
      <div class="background-particles">
        <div class="bg-particle" v-for="i in 12" :key="i"></div>
      </div>
    </div>

    <!-- 初始状态：大输入框和快捷按钮 -->
    <div v-if="!currentQuestion" class="initial-state">
      <div class="welcome-section">
        <div class="welcome-icon">
          <div class="icon-glow"></div>
          <span class="icon">✨</span>
        </div>
        <h2 class="welcome-title">开始您的AI问答之旅</h2>
        <p class="welcome-subtitle">请输入您的问题，或选择下方快捷选项开始</p>
      </div>

      <div class="main-input-section">
        <div class="input-container">
          <div class="input-wrapper">
            <div class="input-glow"></div>
            <textarea 
              v-model="mainInput"
              @keydown="handleMainInputKeydown"
              placeholder="请输入您想了解的问题或需求..."
              class="main-input"
              rows="3"
              ref="mainInputRef"
            ></textarea>
            <button 
              @click="submitMainInput"
              :disabled="!mainInput.trim() || isLoading"
              class="main-submit-btn"
              :class="{ active: mainInput.trim() && !isLoading }"
            >
              <div class="btn-glow"></div>
              <span class="btn-icon">🚀</span>
              <span class="btn-text">开始问答</span>
            </button>
          </div>
        </div>
      </div>

      <div class="quick-actions">
        <div class="action-group">
          <button 
            @click="toggleQuickInput('introduce')"
            class="quick-btn"
            :class="{ expanded: expandedAction === 'introduce' }"
          >
            <span class="btn-icon">👋</span>
            <span class="btn-text">介绍自己</span>
          </button>
          
          <div v-if="expandedAction === 'introduce'" class="quick-input-container">
            <div class="quick-input-wrapper">
              <input 
                v-model="quickInputs.introduce"
                @keydown.enter="submitQuickInput('introduce')"
                placeholder="请简单介绍一下您自己..."
                class="quick-input"
                ref="introduceInputRef"
              />
              <button 
                @click="submitQuickInput('introduce')"
                :disabled="!quickInputs.introduce.trim()"
                class="quick-submit-btn"
              >
                <span>确认</span>
              </button>
            </div>
          </div>
        </div>

        <div class="action-group">
          <button 
            @click="toggleQuickInput('model')"
            class="quick-btn"
            :class="{ expanded: expandedAction === 'model' }"
          >
            <span class="btn-icon">🤖</span>
            <span class="btn-text">使用模型</span>
          </button>
          
          <div v-if="expandedAction === 'model'" class="quick-input-container">
            <div class="quick-input-wrapper">
              <input 
                v-model="quickInputs.model"
                @keydown.enter="submitQuickInput('model')"
                placeholder="请描述您想使用的AI模型或功能..."
                class="quick-input"
                ref="modelInputRef"
              />
              <button 
                @click="submitQuickInput('model')"
                :disabled="!quickInputs.model.trim()"
                class="quick-submit-btn"
              >
                <span>确认</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 问答状态：根据问题类型渲染不同界面 -->
    <div v-else class="question-state">
      <!-- 问题标题和描述 -->
      <div class="question-header">
        <div class="question-title">
          <div class="title-icon">
            <span>{{ getQuestionIcon(currentQuestion.type) }}</span>
          </div>
          <h3>{{ currentQuestion.question }}</h3>
        </div>
        <p v-if="currentQuestion.desc" class="question-desc">{{ currentQuestion.desc }}</p>
      </div>

      <!-- 输入框问题 -->
      <div v-if="currentQuestion.type === 'input'" class="input-question">
        <div class="answer-input-container">
          <div class="answer-input-wrapper">
            <div class="input-glow"></div>
            <textarea 
              v-model="answers.input"
              @keydown="handleAnswerKeydown"
              placeholder="请输入您的答案..."
              class="answer-input"
              rows="4"
            ></textarea>
          </div>
        </div>
      </div>

      <!-- 单选问题 -->
      <div v-else-if="currentQuestion.type === 'single'" class="single-choice-question">
        <SingleChoiceOptions
          :options="currentQuestion.options"
          v-model:selected-value="answers.single"
          :disabled="isLoading"
        />
      </div>

      <!-- 多选问题 -->
      <div v-else-if="currentQuestion.type === 'multi'" class="multiple-choice-question">
        <MultipleChoiceOptions
          :options="currentQuestion.options"
          v-model:selected-values="answers.multiple"
          :disabled="isLoading"
        />
      </div>

      <!-- 表单问题 -->
      <div v-else-if="currentQuestion.type === 'form'" class="form-question">
        <div class="form-fields">
          <FormField
            v-for="field in currentQuestion.fields"
            :key="field.id"
            :field="field"
            v-model:value="answers.form[field.id]"
            :disabled="isLoading"
            :required="true"
            @change="handleFormFieldChange"
          />
        </div>
      </div>

      <!-- 提交按钮 -->
      <div class="question-actions">
        <button 
          @click="submitAnswer"
          :disabled="!isAnswerValid() || isLoading"
          class="submit-answer-btn"
          :class="{ active: isAnswerValid() && !isLoading }"
        >
          <div class="btn-glow"></div>
          <span class="btn-icon">📝</span>
          <span class="btn-text">提交答案</span>
        </button>
        
        <button 
          @click="resetQuestion"
          class="reset-btn"
        >
          <span class="btn-icon">🔄</span>
          <span class="btn-text">重新开始</span>
        </button>
      </div>
    </div>

    <!-- 加载状态 -->
    <LoadingAnimation 
      v-if="isLoading" 
      :type="loadingType"
      :duration="3000"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, computed, watch } from 'vue'
import LoadingAnimation from './LoadingAnimation.vue'
import SingleChoiceOptions from './SingleChoiceOptions.vue'
import MultipleChoiceOptions from './MultipleChoiceOptions.vue'
import FormField from './FormField.vue'

// 定义问题类型接口
interface Option {
  id: string
  label: string
}

interface FormFieldData {
  id: string
  question: string
  type: 'input' | 'single' | 'multi'
  options?: Option[]
  desc?: string
  weight?: string
}

interface BaseQuestion {
  type: 'input' | 'single' | 'multi' | 'form'
  question: string
  desc?: string
  questionId?: string
}

interface InputQuestion extends BaseQuestion {
  type: 'input'
  answer?: string
}

interface SingleChoiceQuestion extends BaseQuestion {
  type: 'single'
  options: Option[]
  answer?: string[]
}

interface MultipleChoiceQuestion extends BaseQuestion {
  type: 'multi'
  options: Option[]
  answer?: string[]
}

interface FormQuestion extends BaseQuestion {
  type: 'form'
  fields: FormFieldData[]
  answer?: Array<{ id: string; value: string[] }>
}

type Question = InputQuestion | SingleChoiceQuestion | MultipleChoiceQuestion | FormQuestion

interface Props {
  currentQuestion?: Question | null
  isLoading?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  sendMessage: [content: string]
  submitAnswer: [answer: any]
}>()

// 响应式数据
const mainInput = ref('')
const expandedAction = ref<string | null>(null)
const quickInputs = reactive({
  introduce: '',
  model: ''
})

// 答案数据
const answers = reactive({
  input: '',
  single: '',
  multiple: [] as string[],
  form: {} as Record<string, any>
})

// 引用
const mainInputRef = ref<HTMLTextAreaElement>()
const introduceInputRef = ref<HTMLInputElement>()
const modelInputRef = ref<HTMLInputElement>()

// 计算属性
const loadingType = computed(() => {
  if (!props.currentQuestion) {
    return 'thinking'
  }
  
  switch (props.currentQuestion.type) {
    case 'input':
      return 'analyzing'
    case 'single':
    case 'multi':
      return 'processing'
    case 'form':
      return 'generating'
    default:
      return 'thinking'
  }
})

// 监听问题变化，重置答案
watch(() => props.currentQuestion, (newQuestion, oldQuestion) => {
  if (newQuestion && newQuestion !== oldQuestion) {
    resetAnswers()
  }
}, { deep: true })

// 方法
const resetAnswers = () => {
  answers.input = ''
  answers.single = ''
  answers.multiple = []
  answers.form = {}
}

const handleMainInputKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    submitMainInput()
  }
}

const submitMainInput = () => {
  if (!mainInput.value.trim() || props.isLoading) return
  
  emit('sendMessage', mainInput.value.trim())
  mainInput.value = ''
}

const toggleQuickInput = (action: string) => {
  if (expandedAction.value === action) {
    expandedAction.value = null
  } else {
    expandedAction.value = action
    nextTick(() => {
      if (action === 'introduce' && introduceInputRef.value) {
        introduceInputRef.value.focus()
      } else if (action === 'model' && modelInputRef.value) {
        modelInputRef.value.focus()
      }
    })
  }
}

const submitQuickInput = (action: string) => {
  const content = quickInputs[action as keyof typeof quickInputs]
  if (!content.trim()) return
  
  const prefix = action === 'introduce' ? '自我介绍：' : '使用模型：'
  emit('sendMessage', prefix + content.trim())
  
  quickInputs[action as keyof typeof quickInputs] = ''
  expandedAction.value = null
}

const getQuestionIcon = (type: string) => {
  const icons = {
    input: '✏️',
    single: '🔘',
    multi: '☑️',
    form: '📋'
  }
  return icons[type as keyof typeof icons] || '❓'
}

const handleFormFieldChange = (fieldId: string, value: any) => {
  // 表单字段变化处理，已通过v-model自动处理
  console.log(`Field ${fieldId} changed to:`, value)
}

const handleAnswerKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Enter' && event.ctrlKey) {
    event.preventDefault()
    submitAnswer()
  }
}

const isAnswerValid = () => {
  if (!props.currentQuestion) return false
  
  switch (props.currentQuestion.type) {
    case 'input':
      return answers.input.trim().length > 0
    case 'single':
      return answers.single.length > 0
    case 'multi':
      return answers.multiple.length > 0
    case 'form':
      return props.currentQuestion.fields.every(field => {
        const answer = answers.form[field.id]
        if (field.type === 'input') {
          return answer && answer.trim().length > 0
        } else if (field.type === 'single') {
          return answer && answer.length > 0
        } else if (field.type === 'multi') {
          return answer && Array.isArray(answer) && answer.length > 0
        }
        return false
      })
    default:
      return false
  }
}

const submitAnswer = () => {
  if (!isAnswerValid() || props.isLoading) return
  
  let answerData: any
  
  switch (props.currentQuestion!.type) {
    case 'input':
      answerData = answers.input.trim()
      break
    case 'single':
      answerData = [answers.single]
      break
    case 'multi':
      answerData = [...answers.multiple]
      break
    case 'form':
      answerData = props.currentQuestion!.fields.map(field => ({
        id: field.id,
        value: field.type === 'input' 
          ? [answers.form[field.id]] 
          : field.type === 'single'
          ? [answers.form[field.id]]
          : answers.form[field.id] || []
      }))
      break
  }
  
  emit('submitAnswer', answerData)
}

const resetQuestion = () => {
  // 重置所有答案
  answers.input = ''
  answers.single = ''
  answers.multiple = []
  answers.form = {}
  
  // 发送重置信号
  emit('sendMessage', '重新开始')
}
</script>

<style scoped>
/* 主容器 */
.question-renderer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(135deg, rgba(10, 10, 10, 0.95) 0%, rgba(20, 20, 20, 0.95) 100%);
  backdrop-filter: blur(20px);
  position: relative;
  overflow: hidden;
}

/* 动态背景 */
.question-background {
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
  animation: bg-float 15s ease-in-out infinite;
}

.bg-particle:nth-child(1) { width: 4px; height: 4px; top: 10%; left: 10%; animation-delay: 0s; }
.bg-particle:nth-child(2) { width: 6px; height: 6px; top: 20%; left: 80%; animation-delay: -2s; }
.bg-particle:nth-child(3) { width: 3px; height: 3px; top: 70%; right: 15%; animation-delay: -4s; }
.bg-particle:nth-child(4) { width: 5px; height: 5px; bottom: 30%; left: 30%; animation-delay: -6s; }
.bg-particle:nth-child(5) { width: 4px; height: 4px; top: 50%; right: 25%; animation-delay: -8s; }
.bg-particle:nth-child(6) { width: 7px; height: 7px; top: 30%; right: 60%; animation-delay: -10s; }
.bg-particle:nth-child(7) { width: 3px; height: 3px; bottom: 60%; right: 10%; animation-delay: -12s; }
.bg-particle:nth-child(8) { width: 5px; height: 5px; bottom: 20%; left: 60%; animation-delay: -14s; }
.bg-particle:nth-child(9) { width: 4px; height: 4px; top: 80%; left: 20%; animation-delay: -16s; }
.bg-particle:nth-child(10) { width: 6px; height: 6px; top: 40%; left: 70%; animation-delay: -18s; }
.bg-particle:nth-child(11) { width: 3px; height: 3px; bottom: 80%; right: 40%; animation-delay: -20s; }
.bg-particle:nth-child(12) { width: 5px; height: 5px; top: 60%; left: 50%; animation-delay: -22s; }

@keyframes bg-float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
    opacity: 0.1;
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
    opacity: 0.3;
  }
}

/* 初始状态样式 */
.initial-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 40px;
  position: relative;
  z-index: 2;
  gap: 48px;
}

.welcome-section {
  text-align: center;
  margin-bottom: 20px;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #d4af37 0%, #f4d03f 50%, #d4af37 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  position: relative;
  box-shadow: 
    0 12px 32px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.icon-glow {
  position: absolute;
  top: -3px;
  left: -3px;
  right: -3px;
  bottom: -3px;
  background: linear-gradient(45deg, #d4af37, transparent, #f4d03f);
  border-radius: 50%;
  opacity: 0.6;
  animation: icon-pulse 3s ease-in-out infinite;
}

@keyframes icon-pulse {
  0%, 100% { opacity: 0.6; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.05); }
}

.icon {
  font-size: 36px;
  z-index: 2;
  position: relative;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #ffffff 0%, #d4af37 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 16px;
  color: #a0a0a0;
  margin: 0;
  font-weight: 400;
}

/* 主输入区域 */
.main-input-section {
  width: 100%;
  max-width: 800px;
}

.input-container {
  width: 100%;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
  background: linear-gradient(135deg, rgba(15, 15, 15, 0.9), rgba(25, 25, 25, 0.9));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 24px;
  padding: 32px;
  position: relative;
  backdrop-filter: blur(20px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 
    0 12px 40px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
}

.input-wrapper:focus-within {
  border-color: rgba(212, 175, 55, 0.5);
  box-shadow: 
    0 16px 48px rgba(0, 0, 0, 0.4),
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
  border-radius: 24px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.input-wrapper:focus-within .input-glow {
  opacity: 1;
}

.main-input {
  width: 100%;
  min-height: 120px;
  background: transparent;
  border: none;
  outline: none;
  resize: vertical;
  font-family: inherit;
  font-size: 18px;
  line-height: 1.6;
  color: #e8e8e8;
  font-weight: 400;
}

.main-input::placeholder {
  color: #666;
  font-weight: 400;
}

.main-submit-btn {
  align-self: flex-end;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 32px;
  background: rgba(15, 15, 15, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 16px;
  color: #888;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.main-submit-btn.active {
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  border-color: #d4af37;
  color: #1a1a1a;
  box-shadow: 
    0 8px 24px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.main-submit-btn.active:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 32px rgba(212, 175, 55, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

/* 快捷操作区域 */
.quick-actions {
  display: flex;
  gap: 24px;
  width: 100%;
  max-width: 600px;
}

.action-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.quick-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(15, 15, 15, 0.9), rgba(25, 25, 25, 0.9));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 16px;
  color: #e8e8e8;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.quick-btn:hover {
  border-color: rgba(212, 175, 55, 0.4);
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(244, 208, 63, 0.05));
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(212, 175, 55, 0.2);
}

.quick-btn.expanded {
  border-color: rgba(212, 175, 55, 0.5);
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15), rgba(244, 208, 63, 0.1));
}

.quick-input-container {
  animation: expand-down 0.3s ease-out;
}

@keyframes expand-down {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.quick-input-wrapper {
  display: flex;
  gap: 12px;
  background: rgba(15, 15, 15, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  padding: 16px;
  backdrop-filter: blur(10px);
}

.quick-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #e8e8e8;
  font-size: 14px;
  font-weight: 400;
}

.quick-input::placeholder {
  color: #666;
}

.quick-submit-btn {
  padding: 8px 16px;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  border: none;
  border-radius: 8px;
  color: #1a1a1a;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-submit-btn:hover {
  transform: scale(1.05);
}

.quick-submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 问答状态样式 */
.question-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 40px;
  position: relative;
  z-index: 2;
  gap: 32px;
  overflow-y: auto;
}

.question-header {
  text-align: center;
  margin-bottom: 20px;
}

.question-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 16px;
}

.title-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.3);
}

.question-title h3 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #e8e8e8;
}

.question-desc {
  font-size: 16px;
  color: #a0a0a0;
  margin: 0;
  line-height: 1.6;
}

/* 输入框问题样式 */
.input-question {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.answer-input-container {
  width: 100%;
}

.answer-input-wrapper {
  background: linear-gradient(135deg, rgba(15, 15, 15, 0.9), rgba(25, 25, 25, 0.9));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
  padding: 24px;
  position: relative;
  backdrop-filter: blur(20px);
  transition: all 0.3s ease;
}

.answer-input-wrapper:focus-within {
  border-color: rgba(212, 175, 55, 0.5);
  box-shadow: 0 0 0 1px rgba(212, 175, 55, 0.3);
}

.answer-input {
  width: 100%;
  min-height: 120px;
  background: transparent;
  border: none;
  outline: none;
  resize: vertical;
  font-family: inherit;
  font-size: 16px;
  line-height: 1.6;
  color: #e8e8e8;
  font-weight: 400;
}

.answer-input::placeholder {
  color: #666;
}

/* 选择题样式 */
.single-choice-question,
.multiple-choice-question {
  max-width: 800px;
  margin: 0 auto;
  width: 100%;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: linear-gradient(135deg, rgba(15, 15, 15, 0.9), rgba(25, 25, 25, 0.9));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(20px);
}

.option-item:hover {
  border-color: rgba(212, 175, 55, 0.4);
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(244, 208, 63, 0.05));
  transform: translateX(4px);
}

.option-item.selected {
  border-color: rgba(212, 175, 55, 0.6);
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15), rgba(244, 208, 63, 0.1));
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.2);
}

.option-radio {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(212, 175, 55, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.option-item.selected .option-radio {
  border-color: #d4af37;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
}

.radio-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1a1a1a;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.option-item.selected .radio-dot {
  opacity: 1;
}

.option-checkbox {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(212, 175, 55, 0.4);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.option-item.selected .option-checkbox {
  border-color: #d4af37;
  background: linear-gradient(135deg, #d4af37, #f4d03f);
}

.checkbox-check {
  font-size: 12px;
  color: #1a1a1a;
  font-weight: 700;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.option-item.selected .checkbox-check {
  opacity: 1;
}

.option-content {
  flex: 1;
}

.option-label {
  font-size: 16px;
  color: #e8e8e8;
  font-weight: 500;
  line-height: 1.4;
}

/* 表单问题样式 */
.form-question {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.form-fields {
  display: grid;
  gap: 24px;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
}

/* 根据字段数量调整布局 */
.form-fields:has(.form-field:nth-child(1):nth-last-child(1)) {
  grid-template-columns: 1fr;
  max-width: 600px;
  margin: 0 auto;
}

.form-fields:has(.form-field:nth-child(2):nth-last-child(1)) {
  grid-template-columns: repeat(2, 1fr);
}

.form-fields:has(.form-field:nth-child(3):nth-last-child(1)) {
  grid-template-columns: repeat(3, 1fr);
}

.form-fields:has(.form-field:nth-child(4):nth-last-child(1)) {
  grid-template-columns: repeat(2, 1fr);
}

.form-fields:has(.form-field:nth-child(5):nth-last-child(1)) {
  grid-template-columns: repeat(3, 1fr);
}

/* 响应式调整 */
@media (max-width: 1024px) {
  .form-fields {
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  }
  
  .form-fields:has(.form-field:nth-child(3):nth-last-child(1)),
  .form-fields:has(.form-field:nth-child(4):nth-last-child(1)),
  .form-fields:has(.form-field:nth-child(5):nth-last-child(1)) {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .form-fields {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

.form-field {
  background: linear-gradient(135deg, rgba(15, 15, 15, 0.9), rgba(25, 25, 25, 0.9));
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 20px;
  padding: 20px;
  backdrop-filter: blur(20px);
  min-height: 200px;
  display: flex;
  flex-direction: column;
}

.field-header {
  margin-bottom: 16px;
  flex-shrink: 0;
}

.field-label {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #e8e8e8;
  margin-bottom: 6px;
  line-height: 1.3;
}

.field-desc {
  font-size: 13px;
  color: #a0a0a0;
  margin: 0;
  line-height: 1.4;
}

.field-input-wrapper {
  background: rgba(10, 10, 10, 0.5);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
}

.field-input-wrapper:focus-within {
  border-color: rgba(212, 175, 55, 0.5);
  box-shadow: 0 0 0 1px rgba(212, 175, 55, 0.3);
}

.field-input-control {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  color: #e8e8e8;
  font-size: 16px;
  font-weight: 400;
}

.field-input-control::placeholder {
  color: #666;
}

.field-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.field-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: rgba(10, 10, 10, 0.5);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.field-option:hover {
  border-color: rgba(212, 175, 55, 0.4);
  background: rgba(212, 175, 55, 0.05);
}

.field-option.selected {
  border-color: rgba(212, 175, 55, 0.6);
  background: rgba(212, 175, 55, 0.1);
}

.option-radio.small {
  width: 16px;
  height: 16px;
}

.option-radio.small .radio-dot {
  width: 6px;
  height: 6px;
}

.option-checkbox.small {
  width: 16px;
  height: 16px;
}

.option-checkbox.small .checkbox-check {
  font-size: 10px;
}

/* 操作按钮 */
.question-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.submit-answer-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 32px;
  background: rgba(15, 15, 15, 0.8);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 16px;
  color: #888;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.submit-answer-btn.active {
  background: linear-gradient(135deg, #d4af37, #f4d03f);
  border-color: #d4af37;
  color: #1a1a1a;
  box-shadow: 
    0 8px 24px rgba(212, 175, 55, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
}

.submit-answer-btn.active:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 12px 32px rgba(212, 175, 55, 0.5),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  background: rgba(15, 15, 15, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  color: #a0a0a0;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
}

.reset-btn:hover {
  border-color: rgba(255, 255, 255, 0.2);
  color: #e8e8e8;
  transform: translateY(-1px);
}

.btn-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(45deg, rgba(212, 175, 55, 0.2), transparent, rgba(212, 175, 55, 0.2));
  border-radius: 16px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.submit-answer-btn.active .btn-glow {
  opacity: 1;
  animation: btn-shimmer 2s ease-in-out infinite;
}

@keyframes btn-shimmer {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
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

/* 加载状态已移至LoadingAnimation组件 */

/* 滚动条样式 */
.question-state::-webkit-scrollbar {
  width: 6px;
}

.question-state::-webkit-scrollbar-track {
  background: rgba(15, 15, 15, 0.3);
  border-radius: 3px;
}

.question-state::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(212, 175, 55, 0.3), rgba(212, 175, 55, 0.6));
  border-radius: 3px;
  transition: all 0.3s ease;
}

.question-state::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, rgba(212, 175, 55, 0.6), rgba(212, 175, 55, 0.8));
}

/* 响应式设计 */
@media (max-width: 768px) {
  .initial-state {
    padding: 24px;
    gap: 32px;
  }
  
  .welcome-icon {
    width: 64px;
    height: 64px;
  }
  
  .icon {
    font-size: 28px;
  }
  
  .welcome-title {
    font-size: 24px;
  }
  
  .input-wrapper {
    padding: 24px;
  }
  
  .main-input {
    font-size: 16px;
    min-height: 100px;
  }
  
  .quick-actions {
    flex-direction: column;
    gap: 16px;
  }
  
  .question-state {
    padding: 24px;
    gap: 24px;
  }
  
  .question-title {
    flex-direction: column;
    gap: 12px;
  }
  
  .title-icon {
    width: 40px;
    height: 40px;
    font-size: 20px;
  }
  
  .question-title h3 {
    font-size: 20px;
  }
  
  .form-fields {
    gap: 24px;
  }
  
  .form-field {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .initial-state {
    padding: 16px;
  }
  
  .input-wrapper {
    padding: 20px;
  }
  
  .main-submit-btn {
    padding: 12px 24px;
    font-size: 14px;
  }
  
  .quick-btn {
    padding: 16px 20px;
    font-size: 14px;
  }
  
  .question-state {
    padding: 16px;
  }
  
  .option-item {
    padding: 16px 20px;
  }
  
  .form-field {
    padding: 16px;
  }
  
  .question-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .submit-answer-btn,
  .reset-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>