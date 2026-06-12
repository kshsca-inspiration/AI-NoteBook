import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 120000
})

// ===== 文件夹 =====
export const getFolders = () => http.get('/folders').then(r => r.data)
export const createFolder = (name) => http.post('/folders', { name }).then(r => r.data)

// ===== 笔记 =====
export const getNotes = (folderId, keyword) => {
  const params = {}
  if (folderId) params.folderId = folderId
  if (keyword) params.keyword = keyword
  return http.get('/notes', { params }).then(r => r.data)
}
export const getNote = (id) => http.get(`/notes/${id}`).then(r => r.data)
export const createNote = (data) => http.post('/notes', data).then(r => r.data)
export const updateNote = (id, data) => http.put(`/notes/${id}`, data)
export const deleteNote = (id) => http.delete(`/notes/${id}`)

// ===== 聊天 =====
export const getMessages = (noteId) => http.get(`/notes/${noteId}/messages`).then(r => r.data)

// SSE 流式聊天用原生 fetch（Axios 不支持流式读取）
export function chatStream(noteId, content, onChunk, onDone, onError) {
  fetch(`/api/notes/${noteId}/chat/send`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content })
  }).then(async (response) => {
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          onChunk(line.slice(5).trim())
        }
      }
    }
    if (buffer.trim() && buffer.startsWith('data:')) {
      onChunk(buffer.slice(5).trim())
    }
    onDone()
  }).catch(onError)
}

// ===== AI 功能 =====
export const generateNote = (noteId) =>
  http.post(`/notes/${noteId}/generate-note`).then(r => r.data.content)
export const generateQuiz = (noteId) =>
  http.post(`/notes/${noteId}/generate-quiz`).then(r => r.data.content)

export default http
