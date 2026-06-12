<template>
  <div class="editor-panel" v-if="store.currentNote">
    <!-- 笔记标题 -->
    <input
      class="note-title-input"
      v-model="localTitle"
      placeholder="笔记标题..."
      @input="markDirty"
    />

    <!-- 文件夹选择 -->
    <div class="note-folder-row">
      <select class="input folder-select" v-model="localFolderId" @change="markDirty">
        <option :value="null">📁 未分类</option>
        <option v-for="f in store.folders" :key="f.id" :value="f.id">📁 {{ f.name }}</option>
      </select>
      <span class="note-time">{{ formatTime(store.currentNote.updatedAt) }}</span>
    </div>

    <!-- Markdown 工具栏 -->
    <MarkdownToolbar
      :toc-open="showToc"
      @insert="handleInsert"
      @insert-line="handleInsertLine"
      @insert-image="handleInsertImage"
      @mode-change="handleModeChange"
      @toggle-toc="showToc = !showToc"
    />

    <!-- 编辑/预览区域 + 目录面板 -->
    <div class="editor-main">
      <div ref="editorAreaRef" class="editor-area" :class="{ split: viewMode === 'split' }">
        <textarea
          v-show="viewMode === 'edit' || viewMode === 'split'"
          ref="textareaRef"
          class="editor-textarea"
          :class="{ split: viewMode === 'split' }"
          :style="viewMode === 'split' ? { width: splitRatio + '%' } : {}"
          v-model="localContent"
          @input="markDirty"
          @keydown.enter.exact="handleEnterKey"
        ></textarea>

        <!-- 拖拽分隔条 -->
        <div
          v-if="viewMode === 'split'"
          class="split-divider"
          @mousedown="startResize"
        >
          <div class="split-divider-line"></div>
        </div>

        <div
          v-show="viewMode === 'preview' || viewMode === 'split'"
          ref="previewRef"
          class="editor-preview"
          :class="{ split: viewMode === 'split' }"
          :style="viewMode === 'split' ? { width: (100 - splitRatio) + '%' } : {}"
          v-html="renderedMarkdown"
        ></div>
      </div>

      <!-- 目录面板 -->
      <Transition name="toc-slide">
        <div v-if="showToc" class="toc-panel">
          <div class="toc-header">
            <span>📑 目录</span>
            <button class="toc-close" @click="showToc = false">✕</button>
          </div>
          <div class="toc-list">
            <div v-if="headings.length === 0" class="toc-empty">暂无标题</div>
            <div
              v-for="(h, i) in headings"
              :key="i"
              class="toc-item"
              :class="'toc-level-' + h.level"
              @click="scrollToHeading(h)"
            >{{ h.text }}</div>
          </div>
        </div>
      </Transition>
    </div>

    <!-- 底部按钮 -->
    <div class="editor-footer">
      <button class="btn" @click="generateQuiz">📝 AI 出题</button>
      <div style="flex:1"></div>
      <button class="btn btn-primary" @click="save" :disabled="!isDirty">💾 保存</button>
    </div>

    <!-- AI 出题弹窗 -->
    <div v-if="quizContent" class="quiz-overlay" @click.self="quizContent = ''">
      <div class="quiz-card">
        <h3>📝 AI 自测题</h3>
        <div class="quiz-body" v-html="quizHtml"></div>
        <button class="btn" @click="quizContent = ''">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onUnmounted } from 'vue'
import { useNoteStore } from '../stores/noteStore.js'
import { updateNote, generateQuiz as fetchQuiz } from '../api/index.js'
import { renderMarkdown } from '../utils/markedConfig.js'
import MarkdownToolbar from './MarkdownToolbar.vue'

const store = useNoteStore()
const textareaRef = ref(null)
const previewRef = ref(null)
const editorAreaRef = ref(null)

const localTitle = ref('')
const localContent = ref('')
const localFolderId = ref(null)
const isDirty = ref(false)
const viewMode = ref('edit')
const quizContent = ref('')
const splitRatio = ref(50)
const isResizing = ref(false)
const showToc = ref(false)

// 当 store.currentNote 切换时，同步到本地
watch(() => store.currentNote, (note) => {
  if (note) {
    localTitle.value = note.title
    localContent.value = note.content || ''
    localFolderId.value = note.folderId
    isDirty.value = false
  }
}, { immediate: true })

// 监听外部对 currentNote.content 的修改（如生成笔记），同步到编辑器
watch(() => store.currentNote?.content, (newContent) => {
  if (newContent !== undefined && newContent !== localContent.value) {
    localContent.value = newContent
    isDirty.value = false
  }
})

const renderedMarkdown = computed(() => renderMarkdown(localContent.value || ''))
const quizHtml = computed(() => renderMarkdown(quizContent.value || ''))

// 解析 markdown 标题生成目录
const headings = computed(() => {
  const content = localContent.value || ''
  const lines = content.split('\n')
  const result = []
  for (let i = 0; i < lines.length; i++) {
    const m = lines[i].match(/^(#{1,3})\s+(.+)/)
    if (m) {
      result.push({ level: m[1].length, text: m[2].trim(), lineIndex: i })
    }
  }
  return result
})

// 点击目录项跳转
function scrollToHeading(h) {
  // 编辑/对比模式：光标跳到 textarea 对应行
  if (viewMode.value === 'edit' || viewMode.value === 'split') {
    const ta = textareaRef.value
    if (!ta) return
    const content = localContent.value
    const lines = content.split('\n')
    let pos = 0
    for (let i = 0; i < h.lineIndex; i++) {
      pos += lines[i].length + 1
    }
    ta.focus()
    ta.selectionStart = ta.selectionEnd = pos
    ta.scrollTop = h.lineIndex * 24 // 估算行高
    return
  }
  // 预览模式：在预览区查找对应标题元素并滚动
  const preview = previewRef.value
  if (!preview) return
  const tags = preview.querySelectorAll('h1, h2, h3')
  for (const el of tags) {
    if (el.textContent.trim() === h.text) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' })
      return
    }
  }
}

function markDirty() { isDirty.value = true }

function handleInsert(before, after) {
  const ta = textareaRef.value
  if (!ta) return
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const selected = localContent.value.substring(start, end)
  const newText = localContent.value.substring(0, start) + before + selected + after + localContent.value.substring(end)
  localContent.value = newText
  markDirty()
  setTimeout(() => {
    ta.focus()
    ta.selectionStart = start + before.length
    ta.selectionEnd = start + before.length + selected.length
  }, 0)
}

function handleInsertLine(prefix) {
  const ta = textareaRef.value
  if (!ta) return
  const start = localContent.value.lastIndexOf('\n', ta.selectionStart - 1) + 1
  localContent.value = localContent.value.substring(0, start) + prefix + localContent.value.substring(start)
  markDirty()
  setTimeout(() => { ta.focus() }, 0)
}

function handleInsertImage(url, width) {
  const ta = textareaRef.value
  if (!ta) return
  const start = ta.selectionStart
  let html
  if (width) {
    html = `<img src="${url}" width="${width}" alt="" />`
  } else {
    html = `<img src="${url}" alt="" />`
  }
  localContent.value = localContent.value.substring(0, start) + html + localContent.value.substring(start)
  markDirty()
  setTimeout(() => {
    ta.focus()
    ta.selectionStart = ta.selectionEnd = start + html.length
  }, 0)
}

function handleEnterKey(e) {
  const ta = textareaRef.value
  if (!ta) return
  const pos = ta.selectionStart
  const before = localContent.value.substring(0, pos)
  const lineStart = before.lastIndexOf('\n') + 1
  const currentLine = before.substring(lineStart)

  const patterns = [
    /^(\d+)\.\s/,
    /^(\d+)\)\s/,
    /^\((\d+)\)\s/
  ]

  for (const pat of patterns) {
    const m = currentLine.match(pat)
    if (m) {
      e.preventDefault()
      const num = parseInt(m[1])
      const prefix = currentLine.substring(0, m[0].length)
      const rest = currentLine.substring(m[0].length)

      if (rest.trim() === '') {
        localContent.value = before.substring(0, lineStart)
          + localContent.value.substring(pos)
        setTimeout(() => {
          ta.selectionStart = ta.selectionEnd = lineStart
        }, 0)
      } else {
        const nextNum = num + 1
        let nextPrefix
        if (pat === patterns[0]) nextPrefix = `${nextNum}. `
        else if (pat === patterns[1]) nextPrefix = `${nextNum}) `
        else nextPrefix = `(${nextNum}) `
        const newText = before + '\n' + nextPrefix + localContent.value.substring(pos)
        localContent.value = newText
        setTimeout(() => {
          ta.selectionStart = ta.selectionEnd = pos + 1 + nextPrefix.length
        }, 0)
      }
      markDirty()
      return
    }
  }
}

// 分隔条拖拽
function startResize(e) {
  isResizing.value = true
  const container = editorAreaRef.value
  if (!container) return

  const onMove = (ev) => {
    const rect = container.getBoundingClientRect()
    const x = ev.clientX - rect.left
    let pct = (x / rect.width) * 100
    if (pct < 15) pct = 15
    if (pct > 85) pct = 85
    splitRatio.value = pct
  }

  const onUp = () => {
    isResizing.value = false
    document.removeEventListener('mousemove', onMove)
    document.removeEventListener('mouseup', onUp)
    document.body.style.cursor = ''
    document.body.style.userSelect = ''
  }

  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onUp)
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
}

onUnmounted(() => {
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
})

function handleModeChange(mode) { viewMode.value = mode }

async function save() {
  await updateNote(store.currentNote.id, {
    title: localTitle.value,
    content: localContent.value,
    folderId: localFolderId.value
  })
  store.currentNote.title = localTitle.value
  store.currentNote.content = localContent.value
  store.currentNote.folderId = localFolderId.value
  isDirty.value = false
}

async function generateQuiz() {
  const quiz = await fetchQuiz(store.currentNote.id)
  quizContent.value = quiz
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
.editor-panel {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  height: 100%;
  max-width: 100%;
}
.note-title-input {
  border: none;
  font-size: 20px;
  font-weight: bold;
  color: var(--primary-dark);
  margin-bottom: 6px;
  outline: none;
  font-family: var(--font-serif);
  width: 100%;
}
.note-folder-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.folder-select {
  width: auto;
  font-size: 11px;
  font-family: var(--font-sans);
  padding: 4px 10px;
}
.note-time {
  font-size: 10px;
  color: var(--text-muted);
  font-family: var(--font-sans);
}
.editor-textarea {
  flex: 1;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 14px;
  font-size: 14px;
  font-family: var(--font-serif);
  color: var(--text);
  line-height: 1.7;
  resize: none;
  outline: none;
}
.editor-textarea:focus { border-color: var(--primary-light); }
.editor-textarea.split {
  flex: none;
  border-radius: var(--radius) 0 0 var(--radius);
  border-right: none;
}
.editor-preview {
  flex: 1;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 14px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.7;
}
.editor-preview.split {
  flex: none;
  border-radius: 0 var(--radius) var(--radius) 0;
  border-left: none;
}

/* 拖拽分隔条 */
.split-divider {
  width: 8px;
  cursor: col-resize;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F8F6FF;
  flex-shrink: 0;
  user-select: none;
}
.split-divider:hover .split-divider-line,
.split-divider:active .split-divider-line {
  background: var(--primary-light);
}
.split-divider-line {
  width: 3px;
  height: 40px;
  background: var(--border);
  border-radius: 2px;
  transition: background 0.15s;
}

.editor-footer {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-shrink: 0;
}

/* 编辑区 + 目录横向容器 */
.editor-main {
  flex: 1;
  display: flex;
  flex-direction: row;
  margin-bottom: 12px;
  overflow: hidden;
  position: relative;
}
.editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.editor-area.split {
  flex-direction: row;
  gap: 0;
}

/* 目录面板 */
.toc-panel {
  width: 200px;
  flex-shrink: 0;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--bg-card);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  margin-left: 8px;
}
.toc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
  font-weight: 600;
  color: var(--primary-dark);
  flex-shrink: 0;
}
.toc-close {
  border: none;
  background: transparent;
  font-size: 14px;
  cursor: pointer;
  color: var(--text-muted);
  padding: 2px 6px;
  border-radius: 4px;
}
.toc-close:hover { background: #F0ECF8; color: var(--text); }
.toc-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}
.toc-empty {
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  padding: 20px 0;
}
.toc-item {
  padding: 6px 12px;
  font-size: 12px;
  cursor: pointer;
  color: var(--text);
  line-height: 1.4;
  border-left: 3px solid transparent;
  transition: all 0.15s;
}
.toc-item:hover {
  background: #F8F6FF;
  border-left-color: var(--primary-light);
  color: var(--primary-dark);
}
.toc-level-1 { padding-left: 12px; font-weight: 600; font-size: 13px; }
.toc-level-2 { padding-left: 24px; }
.toc-level-3 { padding-left: 36px; font-size: 11px; color: var(--text-light); }

/* 目录滑入/滑出动画 */
.toc-slide-enter-active,
.toc-slide-leave-active {
  transition: width 0.2s ease, opacity 0.2s ease, margin-left 0.2s ease;
  overflow: hidden;
}
.toc-slide-enter-from,
.toc-slide-leave-to {
  width: 0 !important;
  opacity: 0;
  margin-left: 0;
}
/* 出题弹窗 */
.quiz-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.quiz-card {
  background: white;
  border-radius: var(--radius-lg);
  padding: 24px;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  width: 90%;
}
.quiz-card h3 { margin-bottom: 16px; color: var(--primary-dark); }
.quiz-body { font-size: 14px; line-height: 1.8; }
</style>
