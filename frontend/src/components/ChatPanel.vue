<template>
  <div class="chat-panel">
    <div class="chat-header">
      ✨ 讨论「{{ store.currentNote?.title || '笔记' }}」
      <button class="btn" style="margin-left:auto;font-size:10px" @click="generateNoteFromChat" :disabled="isStreaming">📝 生成笔记</button>
      <button v-if="prevContent !== null" class="btn" style="margin-left:4px;font-size:10px" @click="undoGenerate">↩ 撤回</button>
    </div>

    <div class="chat-messages" ref="msgList">
      <ChatMessage v-for="msg in messages" :key="msg.id" :message="msg" />
      <!-- 流式输出中的临时消息 -->
      <div v-if="streamingText" class="chat-msg ai">
        <div class="chat-msg-bubble streaming">{{ streamingText }}</div>
      </div>
      <div v-if="messages.length === 0 && !streamingText" class="chat-empty">
        开始和 AI 讨论这篇笔记吧
      </div>
    </div>

    <ChatInput @send="handleSend" :sending="isStreaming" />
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { useNoteStore } from '../stores/noteStore.js'
import { getMessages, chatStream, generateNote, updateNote } from '../api/index.js'
import ChatMessage from './ChatMessage.vue'
import ChatInput from './ChatInput.vue'

const props = defineProps({ noteId: Number })
const store = useNoteStore()
const messages = ref([])
const streamingText = ref('')
const isStreaming = ref(false)
const msgList = ref(null)
const prevContent = ref(null)

// 当 noteId 变化时重新加载聊天记录
watch(() => props.noteId, () => {
  if (props.noteId) loadMessages()
  prevContent.value = null
}, { immediate: true })

async function loadMessages() {
  if (!props.noteId) return
  messages.value = await getMessages(props.noteId)
  await nextTick()
  scrollBottom()
}

function handleSend(text) {
  isStreaming.value = true
  streamingText.value = ''
  messages.value.push({
    id: Date.now(),
    noteId: props.noteId,
    role: 'user',
    content: text
  })
  nextTick(() => scrollBottom())

  chatStream(
    props.noteId, text,
    (chunk) => { streamingText.value += chunk; nextTick(() => scrollBottom()) },
    () => {
      messages.value.push({
        id: Date.now() + 1,
        noteId: props.noteId,
        role: 'ai',
        content: streamingText.value
      })
      streamingText.value = ''
      isStreaming.value = false
    },
    (err) => {
      streamingText.value = '[发送失败: ' + err.message + ']'
      isStreaming.value = false
    }
  )
}

async function generateNoteFromChat() {
  // 保存当前内容以便撤回
  prevContent.value = store.currentNote?.content || ''
  const newPart = await generateNote(props.noteId)
  if (newPart) {
    const merged = prevContent.value + '\n\n' + newPart
    await updateNote(props.noteId, { content: merged })
    store.currentNote.content = merged
  }
}

async function undoGenerate() {
  if (prevContent.value === null) return
  await updateNote(props.noteId, { content: prevContent.value })
  store.currentNote.content = prevContent.value
  prevContent.value = null
}

function scrollBottom() {
  if (msgList.value) msgList.value.scrollTop = msgList.value.scrollHeight
}
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.chat-header {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
  font-size: 12px;
  font-weight: 600;
  color: var(--text-light);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}
.chat-empty {
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 60px;
}
.streaming {
  border-left: 2px solid var(--primary-light);
  animation: pulse 1s infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>
