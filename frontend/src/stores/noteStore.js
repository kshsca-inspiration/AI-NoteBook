import { defineStore } from 'pinia'
import { getFolders, getNotes, getNote, createNote, updateNote, deleteNote } from '../api/index.js'

export const useNoteStore = defineStore('note', {
  state: () => ({
    folders: [],
    notes: [],
    currentNote: null,
    selectedFolderId: null,
    searchKeyword: '',
    chatOpen: false
  }),

  actions: {
    async loadFolders() {
      this.folders = await getFolders()
    },

    async loadNotes() {
      this.notes = await getNotes(this.selectedFolderId, this.searchKeyword)
    },

    async selectNote(id) {
      this.currentNote = await getNote(id)
    },

    async newNote(folderId = null) {
      const note = await createNote({
        title: '未命名笔记',
        content: '',
        folderId
      })
      await this.loadNotes()
      this.currentNote = note
      return note
    },

    async saveNote(id, data) {
      await updateNote(id, data)
      if (this.currentNote && this.currentNote.id === id) {
        Object.assign(this.currentNote, data)
      }
      await this.loadNotes()
    },

    async deleteCurrentNote(id) {
      await deleteNote(id)
      this.currentNote = null
      await this.loadNotes()
    },

    filterByFolder(folderId) {
      this.selectedFolderId = folderId
      this.loadNotes()
    },

    search(keyword) {
      this.searchKeyword = keyword
      this.loadNotes()
    }
  }
})
