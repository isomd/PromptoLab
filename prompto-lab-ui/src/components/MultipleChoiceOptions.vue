<template>
  <div class="multiple-choice-options">
    <div class="options-container">
      <div 
        v-for="option in options" 
        :key="option.id"
        @click="toggleOption(option.id)"
        class="option-item"
        :class="{ 
          selected: selectedValues.includes(option.id),
          disabled: disabled
        }"
      >
        <div class="option-checkbox" :class="{ small: size === 'small' }">
          <div class="checkbox-check">✓</div>
          <div class="checkbox-ripple"></div>
        </div>
        <div class="option-content">
          <span class="option-label">{{ option.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Option {
  id: string
  label: string
}

interface Props {
  options: Option[]
  selectedValues?: string[]
  disabled?: boolean
  size?: 'normal' | 'small'
  maxSelections?: number
}

interface Emits {
  'update:selectedValues': [values: string[]]
  'change': [values: string[]]
}

const props = withDefaults(defineProps<Props>(), {
  selectedValues: () => [],
  disabled: false,
  size: 'normal'
})

const emit = defineEmits<Emits>()

const toggleOption = (optionId: string) => {
  if (props.disabled) return
  
  const currentValues = [...props.selectedValues]
  const index = currentValues.indexOf(optionId)
  
  if (index > -1) {
    // 取消选择
    currentValues.splice(index, 1)
  } else {
    // 添加选择
    if (props.maxSelections && currentValues.length >= props.maxSelections) {
      return // 达到最大选择数量
    }
    currentValues.push(optionId)
  }
  
  emit('update:selectedValues', currentValues)
  emit('change', currentValues)
}
</script>

<style scoped>
.multiple-choice-options {
  width: 100%;
}

.options-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
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

.option-item:hover:not(.disabled) {
  border-color: rgba(212, 175, 55, 0.4);
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.1), rgba(244, 208, 63, 0.05));
  transform: translateX(4px);
}

.option-item.selected {
  border-color: rgba(212, 175, 55, 0.6);
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15), rgba(244, 208, 63, 0.1));
  box-shadow: 0 8px 24px rgba(212, 175, 55, 0.2);
}

.option-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.option-checkbox.small {
  width: 16px;
  height: 16px;
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

.option-checkbox.small .checkbox-check {
  font-size: 10px;
}

.option-item.selected .checkbox-check {
  opacity: 1;
}

.checkbox-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(102, 126, 234, 0.1);
  opacity: 0;
  transition: all 0.3s ease;
  pointer-events: none;
}

.option-item:active:not(.disabled) .checkbox-ripple {
  opacity: 1;
  transform: translate(-50%, -50%) scale(1.2);
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

.option-item.selected .option-label {
  color: #667eea;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .option-item {
    padding: 14px 16px;
  }
  
  .option-checkbox {
    width: 18px;
    height: 18px;
    margin-right: 14px;
  }
  
  .option-label {
    font-size: 0.95rem;
  }
}
</style>