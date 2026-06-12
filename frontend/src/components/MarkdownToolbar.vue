<template>
  <div class="md-toolbar">
    <button class="md-btn" @click="insert('**', '**')" title="加粗"><b>B</b></button>
    <button class="md-btn" @click="insert('*', '*')" title="斜体"><i>I</i></button>
    <button class="md-btn" @click="insertLine('# ')" title="一级标题">H1</button>
    <button class="md-btn" @click="insertLine('## ')" title="二级标题">H2</button>
    <button class="md-btn" @click="insertLine('### ')" title="三级标题">H3</button>
    <button class="md-btn" @click="insert('~~', '~~')" title="删除线"><s>S</s></button>
    <button class="md-btn" @click="insert('==', '==')" title="荧光笔">🖍</button>
    <button class="md-btn" @click="insertLine('- [ ] ')" title="待办">☐</button>
    <button class="md-btn" @click="insertLine('1. ')" title="有序列表 1.">1.</button>
    <button class="md-btn" @click="insertLine('1) ')" title="有序列表 1)">1)</button>
    <button class="md-btn" @click="insertLine('(1) ')" title="有序列表 (1)">(1)</button>
    <button class="md-btn" @click="insertLine('I. ')" title="罗马数字列表">I.</button>
    <button class="md-btn" @click="insertLine('a. ')" title="字母列表">a.</button>
    <button class="md-btn" @click="insert('[', '](url)')" title="链接">🔗</button>

    <!-- 图片按钮 + 弹出选择 -->
    <div class="img-btn-wrap">
      <button class="md-btn" @click="showImgPop = !showImgPop" title="图片">🖼</button>
      <div v-if="showImgPop" class="img-popover">
        <input
          class="img-url-input"
          v-model="imgUrl"
          placeholder="输入图片 URL..."
          @keydown.enter="confirmImage"
        />
        <div class="img-size-row">
          <button class="md-btn size-btn" @click="confirmImage(200)">S 小</button>
          <button class="md-btn size-btn" @click="confirmImage(400)">M 中</button>
          <button class="md-btn size-btn" @click="confirmImage(600)">L 大</button>
          <button class="md-btn size-btn" @click="confirmImage(null)">原始</button>
        </div>
      </div>
    </div>

    <button class="md-btn" @click="insert('`', '`')" title="行内代码">&lt;/&gt;</button>
    <button class="md-btn" @click="insertLine('> ')" title="引用">❝</button>
    <span style="flex:1"></span>
    <button class="md-btn" :class="{ active: tocOpen }" @click="$emit('toggle-toc')" title="目录">≡ 目录</button>
    <span style="width:4px"></span>
    <button class="md-btn" :class="{ active: mode === 'edit' }" @click="setMode('edit')">源码</button>
    <button class="md-btn" :class="{ active: mode === 'split' }" @click="setMode('split')">对比</button>
    <button class="md-btn" :class="{ active: mode === 'preview' }" @click="setMode('preview')">预览</button>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const mode = ref('edit')
const props = defineProps({ tocOpen: Boolean })
const emit = defineEmits(['insert', 'insertLine', 'modeChange', 'insertImage', 'toggle-toc'])

watch(mode, (val) => emit('modeChange', val))

function setMode(m) { mode.value = m }

function insert(before, after) {
  emit('insert', before, after)
}

function insertLine(prefix) {
  emit('insertLine', prefix)
}

// 图片弹出
const showImgPop = ref(false)
const imgUrl = ref('')

function confirmImage(width) {
  if (!imgUrl.value.trim()) return
  emit('insertImage', imgUrl.value.trim(), width)
  imgUrl.value = ''
  showImgPop.value = false
}
</script>

<style scoped>
.md-toolbar {
  display: flex;
  gap: 2px;
  flex-wrap: wrap;
  padding: 6px 8px;
  background: #FAFAFA;
  border-radius: 6px;
  border: 1px solid #EEE;
  margin-bottom: 12px;
  align-items: center;
}
.md-btn {
  border: 1px solid #DDD;
  background: white;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 11px;
  cursor: pointer;
  font-family: var(--font-sans);
  color: var(--text);
  min-width: 28px;
}
.md-btn:hover { background: #F0ECF8; }
.md-btn.active { background: var(--primary-light); color: white; border-color: var(--primary); }

.img-btn-wrap {
  position: relative;
}
.img-popover {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 4px;
  background: white;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 8px;
  z-index: 50;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  min-width: 200px;
}
.img-url-input {
  width: 100%;
  border: 1px solid var(--border);
  border-radius: 4px;
  padding: 5px 8px;
  font-size: 11px;
  outline: none;
  font-family: var(--font-sans);
  margin-bottom: 6px;
}
.img-url-input:focus { border-color: var(--primary-light); }
.img-size-row {
  display: flex;
  gap: 4px;
}
.size-btn {
  font-size: 10px;
  padding: 3px 6px;
}
</style>
