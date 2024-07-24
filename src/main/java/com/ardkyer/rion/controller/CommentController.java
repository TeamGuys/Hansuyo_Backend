package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.*;
import com.ardkyer.rion.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private final VideoService videoService;
    private final TemplateEngine templateEngine;

    @Autowired
    public CommentController(CommentService commentService, UserService userService, VideoService videoService, TemplateEngine templateEngine) {
        this.commentService = commentService;
        this.userService = userService;
        this.videoService = videoService;
        this.templateEngine = templateEngine;
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Map<String, Object>> toggleCommentLike(@PathVariable Long commentId, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(commentService.toggleLike(commentId, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long id) {
        return commentService.getCommentById(id)
                .map(comment -> new ResponseEntity<>(comment, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/videos")
    public String getVideos(Model model, Authentication authentication) {
        List<Video> videos = videoService.getAllVideos();
        Optional<Optional<User>> currentUser = Optional.empty();
        if (authentication != null) {
            currentUser = Optional.ofNullable(userService.getUserByUsername(authentication.getName()));
        }
        model.addAttribute("videos", videos);
        model.addAttribute("currentUser", currentUser);
        return "videos";
    }

    @GetMapping("/video/{videoId}")
    public ResponseEntity<Page<Comment>> getCommentsByVideo(@PathVariable Long videoId, Pageable pageable) {
        Video video = new Video();
        video.setId(videoId);
        Page<Comment> comments = commentService.getCommentsByVideo(video, pageable);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long id, @RequestBody Comment comment) {
        if (!commentService.getCommentById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        comment.setId(id);
        Comment updatedComment = commentService.updateComment(comment);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        if (!commentService.getCommentById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        commentService.deleteComment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping
    public ResponseEntity<String> addComment(@RequestBody Map<String, String> payload, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName());
            Video video = videoService.getVideoById(Long.parseLong(payload.get("videoId")))
                    .orElseThrow(() -> new RuntimeException("Video not found"));

            Comment comment = new Comment();
            comment.setContent(payload.get("content"));
            comment.setUser(user);
            comment.setVideo(video);

            Comment addedComment = commentService.addComment(comment);

            Context context = new Context();
            context.setVariable("comment", addedComment);
            context.setVariable("currentUser", user);

            String commentHtml = templateEngine.process("fragments/comment", context);
            return ResponseEntity.ok(commentHtml);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing comment: " + e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    private String renderCommentFragment(Comment comment, User currentUser) {
        Context context = new Context();
        context.setVariable("comment", comment);
        context.setVariable("currentUser", currentUser);

        return templateEngine.process("fragments/comment :: commentItem", context);
    }
}