<template>
  <div class="resizable-layout">
    <!-- 左栏：笔记列表 -->
    <div class="panel panel-left" :style="{ width: leftWidth + 'px' }" v-show="!leftCollapsed">
      <slot name="left"></slot>
    </div>

    <!-- 分隔条 -->
    <div class="resizer" @mousedown="startResize('left', $event)" v-show="!leftCollapsed && !centerCollapsed"></div>

    <!-- 中栏：AI 聊天 -->
    <div class="panel panel-center" :style="{ width: centerWidth + 'px' }" v-show="!centerCollapsed">
      <slot name="center"></slot>
    </div>

    <!-- 分隔条 -->
    <div class="resizer" @mousedown="startResize('center', $event)" v-show="!centerCollapsed"></div>

    <!-- 右栏：编辑器 -->
    <div class="panel panel-right" v-show="!rightCollapsed">
      <slot name="right"></slot>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const leftWidth = ref(180)
const centerWidth = ref(260)
const leftCollapsed = ref(false)
const centerCollapsed = ref(false)
const rightCollapsed = ref(false)

let dragging = null

function startResize(panel, e) {
  dragging = panel
  document.addEventListener('mousemove', onMouseMove)
  document.addEventListener('mouseup', onMouseUp)
}

function onMouseMove(e) {
  if (dragging === 'left') {
    leftWidth.value = Math.max(120, Math.min(350, e.clientX))
  } else if (dragging === 'center') {
    centerWidth.value = Math.max(180, Math.min(600, e.clientX - leftWidth.value))
  }
}

function onMouseUp() {
  dragging = null
  document.removeEventListener('mousemove', onMouseMove)
  document.removeEventListener('mouseup', onMouseUp)
}

defineExpose({ leftCollapsed, centerCollapsed, rightCollapsed })
</script>

<style scoped>
.resizable-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}
.panel {
  overflow-y: auto;
  flex-shrink: 0;
}
.panel-left { background: var(--bg-panel); border-right: 1px solid var(--border); }
.panel-center { background: var(--bg-card); border-right: 1px solid var(--border); }
.panel-right { flex: 1; overflow-y: auto; }
.resizer {
  width: 4px;
  cursor: col-resize;
  background: transparent;
  flex-shrink: 0;
  transition: background 0.15s;
}
.resizer:hover { background: var(--primary-light); }
</style>
