package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.entity.Video;
import com.ardkyer.rion.service.UserService;
import com.ardkyer.rion.service.VideoService;
import com.ardkyer.rion.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class RankingController {

    @Autowired
    private UserService userService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private FollowService followService;

    @GetMapping("/ranking")
    public String showRanking(Model model, Authentication authentication) {
        List<User> topUsers = userService.getTopUsersByFollowerCount(10);

        User currentUser = null;
        if (authentication != null) {
            currentUser = userService.findByUsername(authentication.getName());
        }

        for (User user : topUsers) {
            if (user.getAvatarUrl() == null) {
                user.setAvatarUrl("/default-avatar.png"); // 기본 아바타 URL 설정
            }
            user.setRecentVideos(videoService.getRecentVideosByUser(user, 5));
            if (currentUser != null) {
                user.setFollowedByCurrentUser(followService.isFollowing(currentUser, user));
            }
        }

        model.addAttribute("topUsers", topUsers);
        model.addAttribute("currentUser", currentUser);
        return "ranking";
    }
}