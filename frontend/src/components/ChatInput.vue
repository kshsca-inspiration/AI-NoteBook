<template>
  <div class="chat-input-area">
    <textarea
      class="input chat-textarea"
      v-model="text"
      placeholder="输入你的问题..."
      @keydown.enter.exact.prevent="send"
      rows="2"
    ></textarea>
    <button class="btn btn-primary chat-send-btn" @click="send" :disabled="!text.trim() || sending">
      {{ sending ? '...' : '发送' }}
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const emit = defineEmits(['send'])
const props = defineProps({ sending: Boolean })

const text = ref('')

function send() {
  if (!text.value.trim() || props.sending) return
  emit('send', text.value)
  text.value = ''
}
</script>

<style scoped>
.chat-input-area {
  display: flex;
  gap: 6px;
  padding: 10px 12px;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}
.chat-textarea {
  flex: 1;
  font-size: 12px;
  resize: none;
  font-family: var(--font-sans);
}
.chat-send-btn {
  align-self: flex-end;
  flex-shrink: 0;
}
</style>
