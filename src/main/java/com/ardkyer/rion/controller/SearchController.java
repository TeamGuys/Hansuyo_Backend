package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {
    @Autowired
    private VideoService videoService;

    @GetMapping("/search")
    public String searchPage() {
        return "search"; // search.html로 이동
    }

    @GetMapping("/search/results")
    public String searchResults(@RequestParam("query") String query, Model model) {

        List<Video> videos = videoService.searchVideos(query);

        model.addAttribute("videos", videos);
        return "searchResults"; // searchResults.html로 이동
    }
}
