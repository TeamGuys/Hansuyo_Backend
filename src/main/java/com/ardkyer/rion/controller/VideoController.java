package com.ardkyer.rion.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.ardkyer.rion.entity.Comment;
import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.service.CommentService;
import com.ardkyer.rion.service.VideoService;
import com.ardkyer.rion.service.UserService;
import com.ardkyer.rion.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @GetMapping
    public String listVideos(Model model, Authentication authentication) {
        List<Video> videos = videoService.getAllVideosWithComments();
        User currentUser = null;
        if (authentication != null) {
            currentUser = userService.findByUsername(authentication.getName());
        }
        for (Video video : videos) {
            video.setLikeCount(likeService.getLikeCountForVideo(video));
            if (currentUser != null) {
                video.setLikedByCurrentUser(likeService.hasUserLikedVideo(currentUser, video));
            }
        }
        model.addAttribute("videos", videos);
        return "videos";
    }

    @GetMapping("/videos")
    public String getVideos(Model model, Authentication authentication) {
        List<Video> videos = videoService.getAllVideos();
        Optional<User> currentUser = Optional.empty();
        if (authentication != null) {
            currentUser = userService.getUserByUsername(authentication.getName());
        }
        model.addAttribute("videos", videos);
        model.addAttribute("currentUser", currentUser);
        return "videos";
    }

    @GetMapping("/{id}")
    public String watchVideo(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Video> videoOptional = videoService.getVideoById(id);
        if (videoOptional.isPresent()) {
            Video video = videoOptional.get();
            video.setLikeCount(likeService.getLikeCountForVideo(video));
            if (authentication != null) {
                User currentUser = userService.findByUsername(authentication.getName());
                video.setLikedByCurrentUser(likeService.hasUserLikedVideo(currentUser, video));
            }
            model.addAttribute("video", video);
            return "watchVideo";
        } else {
            return "redirect:/videos";
        }
    }

    @GetMapping("/file/{fileName:.+}")
    public ResponseEntity<InputStreamResource> serveFile(@PathVariable String fileName) {
        S3Object s3Object = videoService.getVideoFile(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()));
        headers.setContentLength(s3Object.getObjectMetadata().getContentLength());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(s3Object.getObjectContent()));
    }

    @GetMapping("/upload")
    public String showUploadForm() {
        return "uploadForm";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("title") String title,
                                   @RequestParam("description") String description,
                                   @RequestParam("video") MultipartFile file,
                                   @RequestParam(value = "hashtags", required = false)String hashtags,
                                   Authentication authentication) throws IOException {
        User currentUser = userService.findByUsername(authentication.getName());

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setUser(currentUser);

        // description에서 해시태그 추출
        Set<String> hashtagSet = Arrays.stream(description.split(" "))
                .map(String::trim)
                .filter(tag -> tag.startsWith("#"))
                .collect(Collectors.toSet());

        // 추가적인 해시태그 파라미터가 있다면 추가
        if (hashtags != null && !hashtags.trim().isEmpty()) {
            hashtagSet.addAll(Arrays.stream(hashtags.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }

        videoService.uploadVideo(video, file, hashtagSet);
        return "redirect:/videos";
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<Comment>> getVideoComments(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Video video = new Video();
        video.setId(id);
        Page<Comment> comments = commentService.getCommentsByVideo(video, PageRequest.of(page, size));
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}/top-comment")
    public ResponseEntity<Comment> getTopComment(@PathVariable Long id) {
        Video video = new Video();
        video.setId(id);
        Optional<Comment> topComment = commentService.getTopCommentForVideo(video);
        return topComment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}