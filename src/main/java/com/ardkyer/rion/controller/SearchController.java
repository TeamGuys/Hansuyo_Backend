package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.Exercise;
import com.ardkyer.rion.entity.RecentSearch;
import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.service.ExerciseService;
import com.ardkyer.rion.service.RecentSearchService;
import com.ardkyer.rion.service.UserService;
import com.ardkyer.rion.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
@Tag(name = "Search", description = "Search management API")
public class SearchController {
    @Autowired
    private VideoService videoService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private RecentSearchService recentSearchService;

    @Autowired
    private UserService userService;

    @GetMapping("/search/results")
    @Operation(
            summary = "Search for videos",
            description = "Search for videos based on the query, which can be an exercise name or a hashtag.",
            parameters = {
                    @Parameter(name = "query", description = "The search query (exercise name or hashtag)", required = false)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved search results",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Video.class)))
            }
    )
    public String searchResults(@RequestParam(value = "query", required = false) String query,
                                Model model,
                                Authentication authentication) {
        List<Video> videos = null;
        if (query != null && !query.isEmpty()) {
            Set<String> hashtags = exerciseService.getHashtagsByExerciseName(query);
            if (hashtags.isEmpty()) {
                videos = videoService.searchVideos(query);
            } else {
                videos = videoService.searchVideosByHashtags(hashtags);
            }
            // 현재 사용자 가져오기
            User user = userService.findByUsername(authentication.getName());

            // 운동 목록에서 검색한 것이 아닌 경우에만 저장
            boolean isExercise = exerciseService.getAllExercises()
                    .stream()
                    .anyMatch(exercise -> exercise.getName().equalsIgnoreCase(query));
            if (!isExercise) {
                recentSearchService.addOrUpdateRecentSearch(user, query);
            }
        }

        // 최근 검색 기록 가져오기
        User user = userService.findByUsername(authentication.getName());
        List<RecentSearch> recentSearches = recentSearchService.getRecentSearches(user);
        model.addAttribute("videos", videos);
        model.addAttribute("exercises", exerciseService.getAllExercises());
        model.addAttribute("recentSearches", recentSearches);
        return "searchResults.html";
    }

    @PostMapping("/search/recent/delete")
    @Operation(
            summary = "Delete recent search query",
            description = "Delete a recent search query of the logged-in user.",
            parameters = {
                    @Parameter(name = "query", description = "The search query to delete", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully deleted recent search query")
            }
    )
    public String deleteRecentSearch(@RequestParam(value = "query") String query,
                                     Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        recentSearchService.deleteRecentSearch(user, query);
        return "redirect:/search/results";
    }
}