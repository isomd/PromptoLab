<template>
  <div class="form-field">
    <div class="field-header">
      <label class="field-label">
        {{ field.question }}
        <span v-if="required" class="required-mark">*</span>
      </label>
      <p v-if="field.desc" class="field-desc">{{ field.desc }}</p>
    </div>

    <!-- 输入框字段 -->
    <div v-if="field.type === 'input'" class="field-input">
      <div class="field-input-wrapper">
        <input 
          :value="inputValue"
          @input="updateInputValue"
          :placeholder="`请输入${field.question}...`"
          class="field-input-control"
          :disabled="disabled"
        />
        <div class="input-focus-line"></div>
      </div>
    </div>

    <!-- 单选字段 -->
    <div v-else-if="field.type === 'single'" class="field-single">
      <SingleChoiceOptions
        :options="field.options || []"
        :selected-value="singleValue"
        @update:selected-value="updateSingleValue"
        :disabled="disabled"
        size="small"
      />
    </div>

    <!-- 多选字段 -->
    <div v-else-if="field.type === 'multi'" class="field-multi">
      <MultipleChoiceOptions
        :options="field.options || []"
        :selected-values="multipleValues"
        @update:selected-values="updateMultipleValues"
        :disabled="disabled"
        size="small"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import SingleChoiceOptions from './SingleChoiceOptions.vue'
import MultipleChoiceOptions from './MultipleChoiceOptions.vue'

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
}

interface Props {
  field: FormFieldData
  value?: any
  disabled?: boolean
  required?: boolean
}

interface Emits {
  'update:value': [value: any]
  'change': [fieldId: string, value: any]
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  required: false
})

const emit = defineEmits<Emits>()

// 计算属性用于处理不同类型的值
const inputValue = computed(() => {
  if (props.field.type === 'input') {
    return props.value || ''
  }
  return ''
})

const singleValue = computed(() => {
  if (props.field.type === 'single') {
    return props.value || ''
  }
  return ''
})

const multipleValues = computed(() => {
  if (props.field.type === 'multi') {
    return Array.isArray(props.value) ? props.value : []
  }
  return []
})

// 更新方法
const updateInputValue = (event: Event) => {
  const target = event.target as HTMLInputElement
  const newValue = target.value
  emit('update:value', newValue)
  emit('change', props.field.id, newValue)
}

const updateSingleValue = (value: string) => {
  emit('update:value', value)
  emit('change', props.field.id, value)
}

const updateMultipleValues = (values: string[]) => {
  emit('update:value', values)
  emit('change', props.field.id, values)
}
</script>

<style scoped>
.form-field {
  margin-bottom: 24px;
}

.field-header {
  margin-bottom: 16px;
}

.field-label {
  display: block;
  font-size: 1.1rem;
  font-weight: 600;
  color: #e8e8e8;
  margin-bottom: 8px;
  line-height: 1.4;
}

.required-mark {
  color: #e53e3e;
  margin-left: 4px;
}

.field-desc {
  font-size: 0.9rem;
  color: #a0a0a0;
  margin: 0;
  line-height: 1.5;
}

.field-input {
  width: 100%;
}

.field-input-wrapper {
  position: relative;
  width: 100%;
}

.field-input-control {
  width: 100%;
  padding: 14px 16px;
  border: 2px solid rgba(102, 126, 234, 0.1);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  font-size: 1rem;
  color: #2d3748;
  transition: all 0.3s ease;
  outline: none;
  box-sizing: border-box;
}

.field-input-control:focus {
  border-color: #667eea;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.field-input-control:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: rgba(247, 250, 252, 0.8);
}

.field-input-control::placeholder {
  color: #a0aec0;
}

.input-focus-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: all 0.3s ease;
  transform: translateX(-50%);
  border-radius: 1px;
}

.field-input-control:focus + .input-focus-line {
  width: 100%;
}

.field-single,
.field-multi {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .form-field {
    margin-bottom: 20px;
  }
  
  .field-label {
    font-size: 1rem;
  }
  
  .field-desc {
    font-size: 0.85rem;
  }
  
  .field-input-control {
    padding: 12px 14px;
    font-size: 0.95rem;
  }
}
</style>