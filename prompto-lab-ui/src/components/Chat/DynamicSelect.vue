<template>
  <div class="dynamic-form-field" :class="{ dark: isDark }">
    <h3 class="field-title" v-if="fieldConfig.title">{{ fieldConfig.title }}</h3>
    <p class="field-description" v-if="fieldConfig.description">{{ fieldConfig.description }}</p>
    
    <!-- 单选模式 -->
    <div v-if="fieldConfig.type === 'radio'" class="radio-group">
      <div 
        v-for="option in fieldConfig.options" 
        :key="option.value"
        class="option-item radio-option"
        :class="{ 'selected': modelValue === option.value }"
        @click="handleRadioSelect(option.value)"
      >
        <div class="option-content">
          <div class="option-header">
            <span class="option-label">{{ option.label }}</span>
            <!-- <span class="option-weight" v-if="option.weight">权重: {{ option.weight }}</span> -->
          </div>
          <p class="option-description" v-if="option.description">
            {{ option.description }}
          </p>
        </div>
        <div class="selection-indicator">
          <div class="radio" :class="{ checked: modelValue === option.value }">
            <span v-if="modelValue === option.value">●</span>
          </div>
        </div>
      </div>
      
      <!-- 其他选项 -->
      <div v-if="fieldConfig.allowOther" class="option-item other-option" :class="{ 'selected': isOtherSelected }">
        <div class="option-content">
          <div class="option-header">
            <span class="option-label">其他</span>
          </div>
          <input 
            v-if="isOtherSelected || otherValue"
            v-model="otherValue"
            type="text"
            :placeholder="fieldConfig.otherPlaceholder || '请输入其他选项...'"
            class="other-input"
            @click.stop
            @input="handleOtherInput"
          />
        </div>
        <div class="selection-indicator" @click="handleOtherClick">
          <div class="radio" :class="{ checked: isOtherSelected }">
            <span v-if="isOtherSelected">●</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 多选模式 -->
    <div v-else-if="fieldConfig.type === 'checkbox'" class="checkbox-group">
      <div 
        v-for="option in fieldConfig.options" 
        :key="option.value"
        class="option-item checkbox-option"
        :class="{ 'selected': isMultiSelected(option.value) }"
        @click="handleCheckboxSelect(option.value)"
      >
        <div class="option-content">
          <div class="option-header">
            <span class="option-label">{{ option.label }}</span>
            <span class="option-weight" v-if="option.weight">权重: {{ option.weight }}</span>
          </div>
          <p class="option-description" v-if="option.description">
            {{ option.description }}
          </p>
        </div>
        <div class="selection-indicator">
          <div class="checkbox" :class="{ checked: isMultiSelected(option.value) }">
            <span v-if="isMultiSelected(option.value)">✓</span>
          </div>
        </div>
      </div>
      
      <!-- 其他选项 -->
      <div v-if="fieldConfig.allowOther" class="option-item other-option" :class="{ 'selected': isOtherSelected }">
        <div class="option-content">
          <div class="option-header">
            <span class="option-label">其他</span>
          </div>
          <input 
            v-if="isOtherSelected || otherValue"
            v-model="otherValue"
            type="text"
            :placeholder="fieldConfig.otherPlaceholder || '请输入其他选项...'"
            class="other-input"
            @click.stop
            @input="handleOtherInput"
          />
        </div>
        <div class="selection-indicator" @click="handleOtherClick">
          <div class="checkbox" :class="{ checked: isOtherSelected }">
            <span v-if="isOtherSelected">✓</span>
          </div>
        </div>
      </div>
      
      <!-- 多选操作按钮 -->
      <div class="action-buttons">
        <button 
          class="confirm-btn"
          :disabled="!hasSelection"
          @click="handleConfirm"
        >
          确定 ({{ getSelectionCount() }})
        </button>
        <button class="clear-btn" @click="handleClear" v-if="hasSelection">
          清空
        </button>
      </div>
    </div>
    
    <!-- 输入模式 -->
    <div v-else-if="fieldConfig.type === 'input'" class="input-group">
      <input 
        v-model="inputValue"
        :type="fieldConfig.inputType || 'text'"
        :placeholder="fieldConfig.placeholder || '请输入...'"
        :required="fieldConfig.required"
        :maxlength="fieldConfig.maxLength"
        :minlength="fieldConfig.minLength"
        :pattern="fieldConfig.pattern"
        class="form-input"
        @input="handleInputChange"
        @blur="handleInputBlur"
      />
      <div v-if="fieldConfig.maxLength" class="input-counter">
        {{ inputValue.length }} / {{ fieldConfig.maxLength }}
      </div>
    </div>
    
    <!-- 文本域模式 -->
    <div v-else-if="fieldConfig.type === 'textarea'" class="textarea-group">
      <textarea 
        v-model="textareaValue"
        :placeholder="fieldConfig.placeholder || '请输入...'"
        :required="fieldConfig.required"
        :maxlength="fieldConfig.maxLength"
        :rows="fieldConfig.rows || 4"
        class="form-textarea"
        @input="handleTextareaChange"
        @blur="handleTextareaBlur"
      ></textarea>
      <div v-if="fieldConfig.maxLength" class="input-counter">
        {{ textareaValue.length }} / {{ fieldConfig.maxLength }}
      </div>
    </div>
    
    <!-- 下拉选择模式 -->
    <div v-else-if="fieldConfig.type === 'select'" class="select-group">
      <select 
        v-model="selectValue"
        :required="fieldConfig.required"
        :multiple="fieldConfig.multiple"
        class="form-select"
        @change="handleSelectChange"
      >
        <option value="" v-if="!fieldConfig.multiple">{{ fieldConfig.placeholder || '请选择...' }}</option>
        <option 
          v-for="option in fieldConfig.options" 
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </option>
      </select>
    </div>
    
    <!-- 验证错误信息 -->
    <div v-if="validationError" class="error-message">
      {{ validationError }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue'

// 选项接口
interface Option {
  value: string | number
  label: string
  description?: string
  weight?: string | number
  disabled?: boolean
}

// 字段配置接口
interface FieldConfig {
  id: string
  type: 'radio' | 'checkbox' | 'input' | 'textarea' | 'select'
  title?: string
  description?: string
  options?: Option[]
  allowOther?: boolean
  otherPlaceholder?: string
  placeholder?: string
  required?: boolean
  maxLength?: number
  minLength?: number
  pattern?: string
  inputType?: string
  rows?: number
  multiple?: boolean
  validation?: {
    rules?: Array<{
      type: 'required' | 'minLength' | 'maxLength' | 'pattern' | 'custom'
      value?: any
      message: string
      validator?: (value: any) => boolean
    }>
  }
}

// Props接口
interface Props {
  fieldConfig: FieldConfig | string // 支持JSON字符串或对象
  modelValue?: any
  isDark?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isDark: false
})

const emit = defineEmits<{
  'update:modelValue': [value: any]
  'change': [value: any, fieldId: string]
  'validate': [isValid: boolean, error?: string]
}>()

// 解析字段配置
const fieldConfig = computed(() => {
  if (typeof props.fieldConfig === 'string') {
    try {
      return JSON.parse(props.fieldConfig) as FieldConfig
    } catch (error) {
      console.error('Invalid JSON field config:', error)
      return {} as FieldConfig
    }
  }
  return props.fieldConfig
})

// 响应式数据
const selectedValues = ref<(string | number)[]>([])
const otherValue = ref('')
const inputValue = ref('')
const textareaValue = ref('')
const selectValue = ref<any>(fieldConfig.value.multiple ? [] : '')
const validationError = ref('')

// 计算属性
const isOtherSelected = computed(() => {
  if (fieldConfig.value.type === 'radio') {
    return typeof props.modelValue === 'string' && props.modelValue.startsWith('other:')
  }
  return Array.isArray(props.modelValue) && props.modelValue.some(v => 
    typeof v === 'string' && v.startsWith('other:')
  )
})

const hasSelection = computed(() => {
  return selectedValues.value.length > 0 || isOtherSelected.value
})

const getSelectionCount = () => {
  let count = selectedValues.value.length
  if (isOtherSelected.value) count++
  return count
}

// 方法
const isMultiSelected = (value: string | number) => {
  return selectedValues.value.includes(value)
}

const handleRadioSelect = (value: string | number) => {
  otherValue.value = ''
  emit('update:modelValue', value)
  emit('change', value, fieldConfig.value.id)
  validateField(value)
}

const handleCheckboxSelect = (value: string | number) => {
  const index = selectedValues.value.indexOf(value)
  if (index > -1) {
    selectedValues.value.splice(index, 1)
  } else {
    selectedValues.value.push(value)
  }
}

const handleOtherClick = () => {
  if (fieldConfig.value.type === 'radio') {
    selectedValues.value = []
    if (!isOtherSelected.value && otherValue.value) {
      emit('update:modelValue', `other:${otherValue.value}`)
      emit('change', `other:${otherValue.value}`, fieldConfig.value.id)
    }
  } else {
    // 多选模式切换其他选项
    if (isOtherSelected.value) {
      otherValue.value = ''
    }
  }
}

const handleOtherInput = () => {
  if (fieldConfig.value.type === 'radio' && otherValue.value) {
    selectedValues.value = []
    emit('update:modelValue', `other:${otherValue.value}`)
    emit('change', `other:${otherValue.value}`, fieldConfig.value.id)
    validateField(`other:${otherValue.value}`)
  }
}

const handleConfirm = () => {
  if (fieldConfig.value.type !== 'checkbox') return
  
  const results = [...selectedValues.value]
  if (isOtherSelected.value && otherValue.value) {
    results.push(`other:${otherValue.value}`)
  }
  
  emit('update:modelValue', results)
  emit('change', results, fieldConfig.value.id)
  validateField(results)
}

const handleClear = () => {
  selectedValues.value = []
  otherValue.value = ''
  
  const emptyValue = fieldConfig.value.type === 'checkbox' ? [] : ''
  emit('update:modelValue', emptyValue)
  emit('change', emptyValue, fieldConfig.value.id)
  validateField(emptyValue)
}

const handleInputChange = () => {
  emit('update:modelValue', inputValue.value)
  emit('change', inputValue.value, fieldConfig.value.id)
}

const handleInputBlur = () => {
  validateField(inputValue.value)
}

const handleTextareaChange = () => {
  emit('update:modelValue', textareaValue.value)
  emit('change', textareaValue.value, fieldConfig.value.id)
}

const handleTextareaBlur = () => {
  validateField(textareaValue.value)
}

const handleSelectChange = () => {
  emit('update:modelValue', selectValue.value)
  emit('change', selectValue.value, fieldConfig.value.id)
  validateField(selectValue.value)
}

// 验证字段
const validateField = (value: any) => {
  validationError.value = ''
  
  if (!fieldConfig.value.validation?.rules) {
    emit('validate', true)
    return
  }
  
  for (const rule of fieldConfig.value.validation.rules) {
    let isValid = true
    
    switch (rule.type) {
      case 'required':
        isValid = value !== '' && value !== null && value !== undefined && 
                 (!Array.isArray(value) || value.length > 0)
        break
      case 'minLength':
        isValid = typeof value === 'string' && value.length >= (rule.value || 0)
        break
      case 'maxLength':
        isValid = typeof value === 'string' && value.length <= (rule.value || Infinity)
        break
      case 'pattern':
        isValid = typeof value === 'string' && new RegExp(rule.value).test(value)
        break
      case 'custom':
        isValid = rule.validator ? rule.validator(value) : true
        break
    }
    
    if (!isValid) {
      validationError.value = rule.message
      emit('validate', false, rule.message)
      return
    }
  }
  
  emit('validate', true)
}

// 初始化数据
const initializeValue = () => {
  if (props.modelValue !== undefined) {
    switch (fieldConfig.value.type) {
      case 'radio':
        if (typeof props.modelValue === 'string' && props.modelValue.startsWith('other:')) {
          otherValue.value = props.modelValue.replace('other:', '')
        }
        break
      case 'checkbox':
        if (Array.isArray(props.modelValue)) {
          selectedValues.value = props.modelValue.filter(v => 
            !String(v).startsWith('other:')
          )
          const otherItem = props.modelValue.find(v => 
            typeof v === 'string' && v.startsWith('other:')
          )
          if (otherItem) {
            otherValue.value = String(otherItem).replace('other:', '')
          }
        }
        break
      case 'input':
        inputValue.value = String(props.modelValue || '')
        break
      case 'textarea':
        textareaValue.value = String(props.modelValue || '')
        break
      case 'select':
        selectValue.value = props.modelValue
        break
    }
  }
}

// 监听modelValue变化
watch(() => props.modelValue, () => {
  initializeValue()
}, { immediate: true })

// 监听字段配置变化
watch(() => fieldConfig.value, () => {
  initializeValue()
}, { deep: true })

onMounted(() => {
  initializeValue()
})
</script>

<style scoped>
.dynamic-form-field {
  /* CSS 变量定义 */
  --bg-primary: #fff;
  --bg-secondary: #f8faff;
  --bg-selected: #4f46e5;
  --text-primary: #333;
  --text-secondary: #6b7280;
  --text-muted: #9ca3af;
  --border-default: #e1e5e9;
  --border-focus: #4f46e5;
  --border-disabled: #d1d5db;
  --shadow-hover: rgba(79, 70, 229, 0.1);
  --error-color: #ef4444;
  
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  background: var(--bg-primary);
  color: var(--text-primary);
}

/* 暗色主题 */
.dynamic-form-field.dark {
  --bg-primary: #0a0a0a;
  --bg-secondary: #141414;
  --bg-selected: #d4af37;
  --text-primary: #ffffff;
  --text-secondary: #e8e8e8;
  --text-muted: #a0a0a0;
  --border-default: rgba(212, 175, 55, 0.2);
  --border-focus: #d4af37;
  --border-disabled: #333333;
  --shadow-hover: rgba(212, 175, 55, 0.3);
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  position: relative;
  z-index: 2;
  scroll-behavior: smooth;
}

.field-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.field-description {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 16px;
  line-height: 1.5;
}

.option-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  margin-bottom: 8px;
  border: 2px solid var(--border-default);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.option-item:hover {
  border-color: var(--border-focus);
  box-shadow: 0 2px 8px var(--shadow-hover);
}

.option-item.selected {
  border-color: var(--border-focus);
  background: var(--bg-secondary);
}

.radio-option.selected {
  background: var(--bg-selected);
  color: white;
}

.option-content {
  flex: 1;
}

.option-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.option-label {
  font-weight: 500;
  font-size: 16px;
}

.option-weight {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 400;
  white-space: nowrap;
}

.option-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.4;
  margin: 0;
}

.selection-indicator {
  margin-left: 12px;
}

.checkbox, .radio {
  width: 20px;
  height: 20px;
  border: 2px solid var(--border-disabled);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  transition: all 0.2s ease;
  background: var(--bg-primary);
}

.checkbox {
  border-radius: 4px;
}

.radio {
  border-radius: 50%;
}

.checkbox.checked, .radio.checked {
  background: var(--bg-selected);
  border-color: var(--bg-selected);
  color: white;
}

.other-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid var(--border-disabled);
  border-radius: 4px;
  font-size: 14px;
  margin-top: 8px;
  outline: none;
  transition: border-color 0.2s ease;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.other-input:focus {
  border-color: var(--border-focus);
}

.form-input, .form-textarea, .form-select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--border-default);
  border-radius: 8px;
  font-size: 16px;
  outline: none;
  transition: border-color 0.2s ease;
  background: var(--bg-primary);
  color: var(--text-primary);
  font-family: inherit;
}

.form-input:focus, .form-textarea:focus, .form-select:focus {
  border-color: var(--border-focus);
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
}

.input-counter {
  text-align: right;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}

.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 16px;
  border-top: 1px solid var(--border-disabled);
  margin-top: 16px;
}

.confirm-btn, .clear-btn {
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  font-size: 14px;
}

.confirm-btn {
  background: var(--bg-selected);
  color: white;
}

.confirm-btn:hover:not(:disabled) {
  background: #4338ca;
}

.confirm-btn:disabled {
  background: var(--text-muted);
  cursor: not-allowed;
}

.clear-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid var(--border-disabled);
}

.clear-btn:hover {
  background: #e5e7eb;
}

.error-message {
  color: var(--error-color);
  font-size: 14px;
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: 4px;
  border-left: 3px solid var(--error-color);
}

@media (max-width: 640px) {
  .dynamic-form-field {
    padding: 16px;
  }
  
  .option-item {
    padding: 12px;
  }
  
  .option-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .confirm-btn, .clear-btn {
    width: 100%;
  }
}
</style>