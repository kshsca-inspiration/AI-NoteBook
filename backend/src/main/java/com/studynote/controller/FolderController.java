package com.studynote.controller;

import com.studynote.entity.Folder;
import com.studynote.service.FolderService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderController {
    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    public List<Folder> list() {
        return folderService.list();
    }

    @PostMapping
    public Folder create(@RequestBody Map<String, String> body) {
        return folderService.create(body.get("name"));
    }
}
