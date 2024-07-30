package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.*;
import com.ardkyer.rion.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@Tag(name = "Like", description = "Like management API")
public class LikeController {
    private final LikeService likeService;
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public LikeController(LikeService likeService, CustomUserDetailsService customUserDetailsService) {
        this.likeService = likeService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping
    @Operation(summary = "Toggle like on a video", description = "Likes or unlikes a video for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully toggled like", content = @Content(schema = @Schema(implementation = Map.class)))
    public ResponseEntity<Map<String, Object>> toggleLike(
            @Parameter(description = "Video ID to like/unlike", required = true) @RequestBody Map<String, Long> payload,
            Authentication authentication) {
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
    @Operation(summary = "Remove like from a video", description = "Removes the like from a video for the authenticated user")
    @ApiResponse(responseCode = "204", description = "Successfully removed like")
    public ResponseEntity<Void> removeLike(
            @Parameter(description = "ID of the video to remove like from", required = true) @PathVariable Long videoId,
            Authentication authentication) {
        User user = customUserDetailsService.loadUserEntityByUsername(authentication.getName());
        Video video = new Video();
        video.setId(videoId);

        likeService.removeLike(user, video);
        return ResponseEntity.noContent().build();
    }
}