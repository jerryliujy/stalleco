package org.example.stalleco_backend.controller;

import org.example.stalleco_backend.model.post.Tag;

import java.util.List;
import java.util.Optional;
import org.example.stalleco_backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * 帖子的标签
 */
@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    // 获取所有标签
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagRepository.findAll());
    }

    // 创建标签
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        // 检查标签是否已存在
        Optional<Tag> existingTag = tagRepository.findByName(tag.getName());
        return existingTag.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.ok(tagRepository.save(tag)));
    }
}
