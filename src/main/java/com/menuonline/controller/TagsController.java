package com.menuonline.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menuonline.config.AuthFilter;
import com.menuonline.entity.Tags;
import com.menuonline.entity.UserEntity;
import com.menuonline.repository.TagsRepository;
import com.menuonline.service.TagsService;

import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagsController {

    private final TagsRepository repository;
    private final TagsService tagsService;

    @GetMapping
    public ResponseEntity<List<TagResponse>> get(@RequestAttribute(AuthFilter.USER_ATTR_KEY) UserEntity user) {
        List<TagResponse> list = repository.findByUser(user).stream().map(tag -> {
            return TagResponse.builder()
                    .id(tag.getId())
                    .name(tag.getName())
                    .build();
        }).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestAttribute(AuthFilter.USER_ATTR_KEY) UserEntity user,
            @RequestBody String name) {
        try {
            Tags savedTag = repository.save(new Tags(null, name, user, null));
            TagResponse body = TagResponse.builder()
                    .id(savedTag.getId())
                    .name(savedTag.getName())
                    .build();
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestAttribute(AuthFilter.USER_ATTR_KEY) UserEntity user,
            @PathVariable Long id) {
        tagsService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Builder
    public static record TagResponse(Long id, String name) {
    }
}
