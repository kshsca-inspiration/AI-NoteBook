package com.studynote.controller;

import com.studynote.dto.NoteRequest;
import com.studynote.entity.Note;
import com.studynote.service.NoteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<Note> list(
        @RequestParam(required = false) Long folderId,
        @RequestParam(required = false) String keyword
    ) {
        return noteService.list(folderId, keyword);
    }

    @GetMapping("/{id}")
    public Note getById(@PathVariable Long id) {
        return noteService.getById(id);
    }

    @PostMapping
    public Note create(@RequestBody NoteRequest req) {
        return noteService.create(req.getTitle(), req.getContent(), req.getFolderId());
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody NoteRequest req) {
        noteService.update(id, req.getTitle(), req.getContent(), req.getFolderId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        noteService.delete(id);
    }
}
