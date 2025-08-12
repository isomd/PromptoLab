<template>
  <div class="hello">
    <h2>{{ msg }}</h2>
    <p>
      这是一个使用 Vue 3 + TypeScript + Webpack 构建的项目。
    </p>
    <button @click="increment">点击次数: {{ count }}</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  msg: string
}

defineProps<Props>()

const count = ref(0)

const increment = () => {
  count.value++
}
</script>

<style scoped>
.hello {
  padding: 20px;
}

button {
  background-color: #42b883;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}

button:hover {
  background-color: #369870;
}
</style>