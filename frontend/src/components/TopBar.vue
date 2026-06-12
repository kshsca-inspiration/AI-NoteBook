<template>
  <div class="topbar">
    <div class="topbar-left">
      <div class="logo">N</div>
      <span class="title">我的学习笔记</span>
    </div>
    <div class="topbar-center">
      <input
        class="input search-input"
        placeholder="搜索笔记..."
        v-model="keyword"
        @input="emit('search', keyword)"
      />
    </div>
    <div class="topbar-right">
      <button class="btn" @click="emit('toggle-left')" title="切换笔记列表">📋</button>
      <button class="btn" @click="emit('toggle-center')" title="切换AI面板">💬</button>
      <button class="btn btn-primary" @click="emit('new-note')">+ 新建笔记</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const emit = defineEmits(['new-note', 'search', 'toggle-left', 'toggle-center'])
const keyword = ref('')
</script>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 16px;
  background: white;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo {
  width: 32px; height: 32px;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: bold;
  flex-shrink: 0;
}
.title {
  font-weight: bold;
  color: var(--primary-dark);
  font-size: 16px;
  white-space: nowrap;
}
.topbar-center {
  flex: 1;
  max-width: 300px;
}
.search-input {
  font-size: 12px;
  padding: 6px 12px;
}
.topbar-right {
  display: flex;
  gap: 6px;
  align-items: center;
}
</style>
