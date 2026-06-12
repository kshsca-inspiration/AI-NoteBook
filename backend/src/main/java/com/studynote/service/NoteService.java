package com.studynote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studynote.entity.Note;
import com.studynote.mapper.NoteMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteService {
    private final NoteMapper noteMapper;

    public NoteService(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    public List<Note> list(Long folderId, String keyword) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        if (folderId != null) {
            wrapper.eq(Note::getFolderId, folderId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getContent, keyword));
        }
        wrapper.orderByDesc(Note::getUpdatedAt);
        return noteMapper.selectList(wrapper);
    }

    public Note getById(Long id) {
        return noteMapper.selectById(id);
    }

    public Note create(String title, String content, Long folderId) {
        Note note = new Note();
        note.setTitle(title != null ? title : "未命名笔记");
        note.setContent(content != null ? content : "");
        note.setFolderId(folderId);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return note;
    }

    public void update(Long id, String title, String content, Long folderId) {
        Note note = noteMapper.selectById(id);
        if (note == null) return;
        if (title != null) note.setTitle(title);
        if (content != null) note.setContent(content);
        if (folderId != null) note.setFolderId(folderId);
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
    }

    public void delete(Long id) {
        noteMapper.deleteById(id);
    }
}
