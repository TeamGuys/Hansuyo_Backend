package com.ardkyer.rion.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.ardkyer.rion.entity.Comment;
import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.Arrays;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/videos")
@Tag(name = "Video", description = "Video management API")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FollowService followService;

    @GetMapping
    @Operation(summary = "List all videos", description = "Retrieves a list of all videos with comments, sorted by like count")
    public String listVideos(Model model, Authentication authentication) {
        List<Video> videos = videoService.getAllVideosOrderByLikeCountDesc();
        prepareVideosForDisplay(videos, authentication);
        model.addAttribute("videos", videos);
        model.addAttribute("currentUser", getCurrentUser(authentication));
        return "videos";
    }

    @GetMapping("/{id}")
    @Operation(summary = "Watch a video", description = "Retrieves a specific video for watching")
    public String watchVideo(@Parameter(description = "ID of the video to watch") @PathVariable Long id, Model model, Authentication authentication) {
        Optional<Video> videoOptional = videoService.getVideoById(id);
        if (videoOptional.isPresent()) {
            Video video = videoOptional.get();
            prepareVideoForDisplay(video, authentication);
            model.addAttribute("video", video);
            return "watchVideo";
        } else {
            return "redirect:/videos";
        }
    }

    @GetMapping("/file/{fileName:.+}")
    @Operation(summary = "Serve video file", description = "Streams a video file")
    public ResponseEntity<InputStreamResource> serveFile(@Parameter(description = "Name of the file to serve") @PathVariable String fileName) {
        S3Object s3Object = videoService.getVideoFile(fileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()));
        headers.setContentLength(s3Object.getObjectMetadata().getContentLength());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(s3Object.getObjectContent()));
    }

    @GetMapping("/upload")
    @Operation(summary = "Show upload form", description = "Displays the video upload form")
    public String showUploadForm() {
        return "uploadForm";
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload a video", description = "Uploads a new video")
    public String handleFileUpload(@RequestParam("title") String title,
                                   @RequestParam("description") String description,
                                   @RequestParam("video") MultipartFile file,
                                   @RequestParam(value = "hashtags", required = false) String hashtags,
                                   Authentication authentication) throws IOException {
        User currentUser = userService.findByUsername(authentication.getName());

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setUser(currentUser);

        Set<String> hashtagSet = extractHashtags(description, hashtags);

        videoService.uploadVideo(video, file, hashtagSet);

        return "redirect:/videos";
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get video comments", description = "Retrieves comments for a specific video")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved comments", content = @Content(schema = @Schema(implementation = Page.class)))
    public ResponseEntity<Page<Comment>> getVideoComments(
            @Parameter(description = "ID of the video") @PathVariable Long id,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") int size) {
        Video video = new Video();
        video.setId(id);
        Page<Comment> comments = commentService.getCommentsByVideo(video, PageRequest.of(page, size));
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}/top-comment")
    @Operation(summary = "Get top comment", description = "Retrieves the top comment for a specific video")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved top comment", content = @Content(schema = @Schema(implementation = Comment.class)))
    @ApiResponse(responseCode = "404", description = "No comments found for the video")
    public ResponseEntity<Comment> getTopComment(@Parameter(description = "ID of the video") @PathVariable Long id) {
        Video video = new Video();
        video.setId(id);
        Optional<Comment> topComment = commentService.getTopCommentForVideo(video);
        return topComment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Video> videoOptional = videoService.getVideoById(id);
        if (videoOptional.isPresent()) {
            Video video = videoOptional.get();
            prepareVideoForDisplay(video, authentication);

            boolean isFollowing = false;
            if (authentication != null) {
                User currentUser = userService.findByUsername(authentication.getName());
                isFollowing = followService.isFollowing(currentUser, video.getUser());
            }

            model.addAttribute("video", video);
            model.addAttribute("isFollowing", isFollowing);
            model.addAttribute("currentUser", getCurrentUser(authentication));
            return "detailPage";
        } else {
            return "redirect:/videos";
        }
    }

    private void prepareVideosForDisplay(List<Video> videos, Authentication authentication) {
        User currentUser = getCurrentUser(authentication).orElse(null);
        for (Video video : videos) {
            if (currentUser != null) {
                video.setLikedByCurrentUser(likeService.hasUserLikedVideo(currentUser, video));
                video.setFollowedByCurrentUser(followService.isFollowing(currentUser, video.getUser()));
            }
        }
    }

    private void prepareVideoForDisplay(Video video, Authentication authentication) {
        User currentUser = getCurrentUser(authentication).orElse(null);
        prepareVideoForDisplay(video, currentUser);
    }

    private void prepareVideoForDisplay(Video video, User currentUser) {
        video.setLikeCount(likeService.getLikeCountForVideo(video));
        if (currentUser != null) {
            video.setLikedByCurrentUser(likeService.hasUserLikedVideo(currentUser, video));
            video.setFollowedByCurrentUser(followService.isFollowing(currentUser, video.getUser()));
        }
    }

    private Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication != null) {
            return userService.getUserByUsername(authentication.getName());
        }
        return Optional.empty();
    }

    private Set<String> extractHashtags(String description, String additionalHashtags) {
        Set<String> hashtagSet = Arrays.stream(description.split(" "))
                .map(String::trim)
                .filter(tag -> tag.startsWith("#"))
                .collect(Collectors.toSet());

        if (additionalHashtags != null && !additionalHashtags.trim().isEmpty()) {
            hashtagSet.addAll(Arrays.stream(additionalHashtags.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet()));
        }

        return hashtagSet;
    }
}