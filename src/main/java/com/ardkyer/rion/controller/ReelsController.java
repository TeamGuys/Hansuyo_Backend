package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/reels")
public class ReelsController {

    private final VideoService videoService;

    @Autowired
    public ReelsController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public String showReels(Model model) {
        List<Video> reels = videoService.getRandomRecentVideos(10);
        model.addAttribute("reels", reels);
        return "reels";
    }
}