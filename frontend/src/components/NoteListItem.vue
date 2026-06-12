<template>
  <div class="note-item" :class="{ active: active }" @click="emit('select', note.id)">
    <div class="note-item-title">{{ note.title }}</div>
    <div class="note-item-folder">📁 {{ folderName }}</div>
    <div class="note-item-time">{{ formatTime(note.updatedAt || note.createdAt) }}</div>
    <button class="note-item-delete" @click.stop="emit('delete', note.id)">×</button>
  </div>
</template>

<script setup>
const props = defineProps({
  note: Object,
  active: Boolean,
  folderName: { type: String, default: '未分类' }
})
const emit = defineEmits(['select', 'delete'])

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth()+1}-${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2,'0')}`
}
</script>

<style scoped>
.note-item {
  padding: 10px 12px;
  border-radius: var(--radius);
  cursor: pointer;
  position: relative;
  background: white;
  margin-bottom: 4px;
  transition: all 0.15s;
}
.note-item:hover { background: #F0ECF8; }
.note-item.active {
  border: 1.5px solid var(--primary-light);
  box-shadow: 0 1px 4px rgba(100,80,160,0.08);
}
.note-item-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--primary-dark);
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.note-item-folder {
  font-size: 10px;
  color: var(--text-muted);
  font-family: var(--font-sans);
  margin-bottom: 2px;
}
.note-item-time {
  font-size: 9px;
  color: #CCC;
  font-family: var(--font-sans);
}
.note-item-delete {
  position: absolute;
  right: 6px;
  top: 6px;
  background: none;
  border: none;
  color: #CCC;
  font-size: 16px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.15s;
}
.note-item:hover .note-item-delete { opacity: 1; }
.note-item-delete:hover { color: #E88; }
</style>
