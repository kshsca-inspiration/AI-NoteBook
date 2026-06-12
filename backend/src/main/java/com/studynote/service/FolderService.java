package com.studynote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studynote.entity.Folder;
import com.studynote.mapper.FolderMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FolderService {
    private final FolderMapper folderMapper;

    public FolderService(FolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }

    public List<Folder> list() {
        return folderMapper.selectList(
            new LambdaQueryWrapper<Folder>().orderByDesc(Folder::getCreatedAt)
        );
    }

    public Folder create(String name) {
        Folder folder = new Folder();
        folder.setName(name);
        folderMapper.insert(folder);
        return folder;
    }
}
