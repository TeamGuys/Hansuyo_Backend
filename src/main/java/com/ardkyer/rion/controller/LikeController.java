package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.*;
import com.ardkyer.rion.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
public class LikeController {
    private final LikeService likeService;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public LikeController(LikeService likeService, CustomUserDetailsService customUserDetailsService) {
        this.likeService = likeService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> toggleLike(@RequestBody Map<String, Long> payload, Authentication authentication) {
        User user = customUserDetailsService.loadUserEntityByUsername(authentication.getName());
        Video video = new Video();
        video.setId(payload.get("videoId"));

        boolean liked = likeService.toggleLike(user, video);
        long likeCount = likeService.getLikeCountForVideo(video);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", liked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{videoId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long videoId, Authentication authentication) {
        User user = customUserDetailsService.loadUserEntityByUsername(authentication.getName());
        Video video = new Video();
        video.setId(videoId);

        likeService.removeLike(user, video);
        return ResponseEntity.noContent().build();
    }
}