package com.studynote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.studynote.entity.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
