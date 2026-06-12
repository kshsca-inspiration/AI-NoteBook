<template>
  <div id="app-container">
    <TopBar
      @new-note="handleNewNote"
      @search="handleSearch"
      @toggle-left="layoutRef.leftCollapsed = !layoutRef.leftCollapsed"
      @toggle-center="layoutRef.centerCollapsed = !layoutRef.centerCollapsed"
    />
    <ResizableLayout ref="layoutRef">
      <template #left>
        <NoteListPanel
          @select="handleSelectNote"
          @delete="handleDeleteNote"
        />
      </template>
      <template #center>
        <ChatPanel v-if="store.currentNote" :note-id="store.currentNote.id" />
        <div v-else class="empty-panel">
          <p>选择一篇笔记开始 AI 对话</p>
        </div>
      </template>
      <template #right>
        <EditorPanel v-if="store.currentNote" />
        <div v-else class="empty-panel">
          <p>选择或新建一篇笔记</p>
        </div>
      </template>
    </ResizableLayout>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useNoteStore } from './stores/noteStore.js'
import ResizableLayout from './components/ResizableLayout.vue'
import TopBar from './components/TopBar.vue'
import NoteListPanel from './components/NoteListPanel.vue'
import ChatPanel from './components/ChatPanel.vue'
import EditorPanel from './components/EditorPanel.vue'

const store = useNoteStore()
const layoutRef = ref(null)

onMounted(async () => {
  await store.loadFolders()
  await store.loadNotes()
})

async function handleNewNote() {
  await store.newNote(store.selectedFolderId)
}

function handleSelectNote(id) {
  store.selectNote(id)
}

async function handleDeleteNote(id) {
  if (confirm('确定删除这篇笔记吗？')) {
    await store.deleteCurrentNote(id)
  }
}

function handleSearch(keyword) {
  store.search(keyword)
}
</script>

<style scoped>
#app-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted);
  font-size: 14px;
}
</style>
