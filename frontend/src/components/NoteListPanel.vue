<template>
  <div class="note-list-panel">
    <!-- 文件夹选择 -->
    <div class="panel-section">
      <select class="input folder-select" v-model="folderFilter" @change="onFolderChange">
        <option :value="null">📁 全部笔记</option>
        <option v-for="f in store.folders" :key="f.id" :value="f.id">📁 {{ f.name }}</option>
      </select>
    </div>

    <!-- 新建文件夹 -->
    <div class="panel-section folder-create">
      <input class="input" v-model="newFolderName" placeholder="新建文件夹..." @keyup.enter="addFolder" />
    </div>

    <!-- 笔记列表 -->
    <div class="note-list">
      <NoteListItem
        v-for="note in store.notes"
        :key="note.id"
        :note="note"
        :active="store.currentNote?.id === note.id"
        :folder-name="getFolderName(note.folderId)"
        @select="handleSelect"
        @delete="handleDelete"
      />
      <div v-if="store.notes.length === 0" class="empty-notes">
        暂无笔记，点击右上角 + 新建
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useNoteStore } from '../stores/noteStore.js'
import { createFolder } from '../api/index.js'
import NoteListItem from './NoteListItem.vue'

const store = useNoteStore()
const emit = defineEmits(['select', 'delete'])

const folderFilter = ref(null)
const newFolderName = ref('')

function onFolderChange() {
  store.filterByFolder(folderFilter.value)
}

async function addFolder() {
  const name = newFolderName.value.trim()
  if (!name) return
  await createFolder(name)
  await store.loadFolders()
  newFolderName.value = ''
}

function getFolderName(folderId) {
  const f = store.folders.find(x => x.id === folderId)
  return f ? f.name : '未分类'
}

function handleSelect(id) { emit('select', id) }
function handleDelete(id) { emit('delete', id) }
</script>

<style scoped>
.note-list-panel {
  padding: 12px;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.panel-section { margin-bottom: 10px; }
.folder-select { font-size: 12px; font-family: var(--font-sans); }
.folder-create input { font-size: 11px; padding: 5px 10px; }
.note-list { flex: 1; overflow-y: auto; }
.empty-notes {
  text-align: center;
  color: var(--text-muted);
  font-size: 12px;
  margin-top: 40px;
}
</style>
