<template>
  <div class="test-container">
    <div class="test-header">
      <h1>QuestionRenderer 组件测试</h1>
      <div class="test-controls">
        <button 
          v-for="(test, index) in testCases" 
          :key="index"
          @click="setCurrentTest(index)"
          :class="{ active: currentTestIndex === index }"
          class="test-btn"
        >
          {{ test.name }}
        </button>
        <button @click="resetToInitial" class="test-btn reset-btn">
          重置到初始状态
        </button>
      </div>
    </div>
    
    <div class="test-content">
      <QuestionRenderer
        :current-question="currentQuestion"
        :is-loading="isLoading"
        @send-message="handleSendMessage"
        @submit-answer="handleSubmitAnswer"
      />
    </div>
    
    <div class="test-info">
      <h3>当前测试数据：</h3>
      <pre>{{ JSON.stringify(currentQuestion, null, 2) }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import QuestionRenderer from './QuestionRenderer.vue'

// 测试用例数据
const testCases = [
  {
    name: '输入题',
    data: {
      questionId: 'input_001',
      type: 'input',
      question: '请描述一下你的职业背景和主要技能？',
      desc: '请详细描述你的工作经历、专业技能以及你认为最有价值的能力。这将帮助我们更好地了解你的背景。',
      answer: ''
    }
  },
  {
    name: '单选题',
    data: {
      questionId: 'single_001',
      type: 'single',
      question: '你最感兴趣的技术领域是什么？',
      desc: '请从以下选项中选择一个你最感兴趣或最擅长的技术领域。',
      options: [
        { id: 'frontend', label: '前端开发' },
        { id: 'backend', label: '后端开发' },
        { id: 'mobile', label: '移动开发' },
        { id: 'ai', label: '人工智能' },
        { id: 'devops', label: 'DevOps运维' },
        { id: 'data', label: '数据科学' }
      ],
      answer: []
    }
  },
  {
    name: '多选题',
    data: {
      questionId: 'multi_001',
      type: 'multi',
      question: '你熟悉哪些编程语言？',
      desc: '请选择所有你熟悉或使用过的编程语言。可以选择多个选项。',
      options: [
        { id: 'javascript', label: 'JavaScript' },
        { id: 'typescript', label: 'TypeScript' },
        { id: 'python', label: 'Python' },
        { id: 'java', label: 'Java' },
        { id: 'csharp', label: 'C#' },
        { id: 'go', label: 'Go' },
        { id: 'rust', label: 'Rust' },
        { id: 'php', label: 'PHP' }
      ],
      answer: []
    }
  },
  {
    name: '表单题',
    data: {
      questionId: 'form_001',
      type: 'form',
      question: '个人信息调查表',
      desc: '请填写以下个人信息，这将帮助我们为你提供更个性化的服务。',
      fields: [
        {
          id: 'name',
          question: '姓名',
          type: 'input',
          desc: '请输入你的真实姓名'
        },
        {
          id: 'experience',
          question: '工作经验',
          type: 'single',
          desc: '请选择你的工作经验年限',
          options: [
            { id: '0-1', label: '0-1年' },
            { id: '1-3', label: '1-3年' },
            { id: '3-5', label: '3-5年' },
            { id: '5-10', label: '5-10年' },
            { id: '10+', label: '10年以上' }
          ]
        },
        {
          id: 'skills',
          question: '核心技能',
          type: 'multi',
          desc: '请选择你的核心技能（可多选）',
          options: [
            { id: 'vue', label: 'Vue.js' },
            { id: 'react', label: 'React' },
            { id: 'angular', label: 'Angular' },
            { id: 'nodejs', label: 'Node.js' },
            { id: 'spring', label: 'Spring Boot' },
            { id: 'docker', label: 'Docker' }
          ]
        },
        {
          id: 'introduction',
          question: '自我介绍',
          type: 'input',
          desc: '请简单介绍一下自己'
        }
      ],
      answer: []
    }
  }
]

const currentTestIndex = ref(-1) // -1 表示初始状态
const isLoading = ref(false)

const currentQuestion = computed(() => {
  if (currentTestIndex.value === -1) {
    return null
  }
  return testCases[currentTestIndex.value]?.data || null
})

const setCurrentTest = (index: number) => {
  currentTestIndex.value = index
  isLoading.value = false
}

const resetToInitial = () => {
  currentTestIndex.value = -1
  isLoading.value = false
}

const handleSendMessage = (content: string) => {
  console.log('发送消息:', content)
  // 模拟加载状态
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
    // 可以在这里设置一个测试问题
    if (content.includes('介绍') || content.includes('自己')) {
      setCurrentTest(0) // 显示输入题
    } else if (content.includes('模型') || content.includes('使用')) {
      setCurrentTest(1) // 显示单选题
    }
  }, 1500)
}

const handleSubmitAnswer = (answerData: any) => {
  console.log('提交答案:', answerData)
  // 模拟提交过程
  isLoading.value = true
  setTimeout(() => {
    isLoading.value = false
    alert(`答案已提交: ${JSON.stringify(answerData, null, 2)}`)
    // 可以切换到下一个测试或重置
    resetToInitial()
  }, 1000)
}
</script>

<style scoped>
.test-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.test-header {
  text-align: center;
  margin-bottom: 30px;
}

.test-header h1 {
  color: white;
  font-size: 2.5rem;
  margin-bottom: 20px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.test-controls {
  display: flex;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
}

.test-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.test-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.test-btn.active {
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
  font-weight: 600;
}

.reset-btn {
  background: rgba(255, 107, 107, 0.3) !important;
  border-color: rgba(255, 107, 107, 0.5) !important;
}

.reset-btn:hover {
  background: rgba(255, 107, 107, 0.5) !important;
}

.test-content {
  margin-bottom: 30px;
}

.test-info {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px;
  margin-top: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.test-info h3 {
  color: white;
  margin-bottom: 15px;
  font-size: 1.2rem;
}

.test-info pre {
  background: rgba(0, 0, 0, 0.3);
  color: #e2e8f0;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  font-size: 0.9rem;
  line-height: 1.5;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

@media (max-width: 768px) {
  .test-container {
    padding: 10px;
  }
  
  .test-header h1 {
    font-size: 2rem;
  }
  
  .test-controls {
    gap: 8px;
  }
  
  .test-btn {
    padding: 8px 16px;
    font-size: 0.9rem;
  }
}
</style>