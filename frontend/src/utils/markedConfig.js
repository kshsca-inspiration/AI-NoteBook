import { marked } from 'marked'

// 启用 GFM（任务列表、表格、删除线）
marked.use({ gfm: true, breaks: false })

/**
 * 渲染 Markdown 文本为 HTML，支持自定义扩展：
 * - ==高亮==  → <mark>高亮</mark>
 * - 任务列表复选框可交互（移除 disabled）
 */
export function renderMarkdown(text) {
  if (!text) return ''

  // 预处理：将 ==text== 转为 <mark>text</mark>（避免与 marked 内部冲突）
  let processed = text.replace(/==([^=]+)==/g, '<mark>$1</mark>')

  // 使用 marked 渲染
  let html = marked.parse(processed)

  // 后处理：去掉 GFM 任务列表复选框的 disabled 属性，使其可交互
  html = html.replace(/<input disabled="" type="checkbox"/g, '<input type="checkbox" class="md-checkbox"')

  return html
}

// 导出原始 marked 供其他场景使用
export { marked }
