<template>
  <div class="chat-msg" :class="message.role">
    <div class="chat-msg-bubble" v-html="renderedContent"></div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { renderMarkdown } from '../utils/markedConfig.js'

const props = defineProps({ message: Object })
const renderedContent = computed(() => renderMarkdown(props.message.content || ''))
</script>

<style scoped>
.chat-msg { margin-bottom: 10px; display: flex; }
.chat-msg.user { justify-content: flex-end; }
.chat-msg.ai { justify-content: flex-start; }
.chat-msg-bubble {
  max-width: 85%;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.6;
  word-break: break-word;
}
.user .chat-msg-bubble {
  background: #F0ECF8;
  color: var(--primary-dark);
  border-bottom-right-radius: 3px;
}
.ai .chat-msg-bubble {
  background: linear-gradient(135deg, #E8E0FF, #F0E8FF);
  color: var(--primary-dark);
  border-bottom-left-radius: 3px;
}
</style>
