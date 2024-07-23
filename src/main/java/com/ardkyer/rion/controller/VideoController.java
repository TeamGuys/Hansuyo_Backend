package com.ardkyer.rion.controller;

import com.amazonaws.services.s3.model.S3Object;
import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.service.VideoService;
import com.ardkyer.rion.service.UserService;
import com.ardkyer.rion.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    private static final String UPLOADED_FOLDER = "C:\\Users\\k0207\\datas\\";

    @GetMapping
    public String listVideos(Model model, Authentication authentication) {
        List<Video> videos = videoService.getAllVideos();
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

    private Resource loadAsResource(String filename) {
        try {
            Path file = Paths.get(UPLOADED_FOLDER).resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    @GetMapping("/upload")
    public String showUploadForm() {
        return "uploadForm";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("title") String title,
                                   @RequestParam("description") String description,
                                   @RequestParam("video") MultipartFile file,
                                   Authentication authentication) throws IOException {
        User currentUser = userService.findByUsername(authentication.getName());

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setUser(currentUser);
        videoService.uploadVideo(video, file);
        return "redirect:/videos";
    }
}