<template>
  <div class="single-choice-options">
    <div class="options-container">
      <div 
        v-for="option in options" 
        :key="option.id"
        @click="selectOption(option.id)"
        class="option-item"
        :class="{ 
          selected: selectedValue === option.id,
          disabled: disabled
        }"
      >
        <div class="option-radio" :class="{ small: size === 'small' }">
          <div class="radio-dot"></div>
          <div class="radio-ripple"></div>
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
  selectedValue?: string
  disabled?: boolean
  size?: 'normal' | 'small'
}

interface Emits {
  'update:selectedValue': [value: string]
  'change': [value: string]
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  size: 'normal'
})

const emit = defineEmits<Emits>()

const selectOption = (optionId: string) => {
  if (props.disabled) return
  
  emit('update:selectedValue', optionId)
  emit('change', optionId)
}
</script>

<style scoped>
.single-choice-options {
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

.option-radio.small {
  width: 16px;
  height: 16px;
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

.radio-ripple {
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

.option-item:active:not(.disabled) .radio-ripple {
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
  
  .option-radio {
    width: 18px;
    height: 18px;
    margin-right: 14px;
  }
  
  .option-label {
    font-size: 0.95rem;
  }
}
</style>