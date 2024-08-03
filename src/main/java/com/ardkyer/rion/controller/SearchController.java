package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.Exercise;
import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.service.ExerciseService;
import com.ardkyer.rion.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
public class SearchController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private ExerciseService exerciseService;

    @GetMapping("/search/results")
    public String searchResults(@RequestParam(value = "query", required = false) String query, Model model) {
        List<Video> videos = null;
        if (query != null && !query.isEmpty()) {
            Set<String> hashtags = exerciseService.getHashtagsByExerciseName(query);
            if (hashtags.isEmpty()) {
                videos = videoService.searchVideos(query);
            } else {
                videos = videoService.searchVideosByHashtags(hashtags);
            }
        }
        model.addAttribute("videos", videos);
        model.addAttribute("exercises", exerciseService.getAllExercises());
        return "searchResults";
    }
}
