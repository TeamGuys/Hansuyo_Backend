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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@Tag(name = "User", description = "User management API")
public class UserController {
    private final UserService userService;
    private final VideoService videoService;
    private final CommentService commentService;
    private final FollowService followService;

    @Autowired
    public UserController(UserService userService, VideoService videoService, CommentService commentService, FollowService followService) {
        this.userService = userService;
        this.videoService = videoService;
        this.commentService = commentService;
        this.followService = followService;
    }

    @PostMapping("/api/users/register")
    @ResponseBody
    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponse(responseCode = "201", description = "Successfully registered user", content = @Content(schema = @Schema(implementation = User.class)))
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        User registeredUser = userService.registerUser(user);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @GetMapping("/api/users/{id}")
    @ResponseBody
    @Operation(summary = "Get a user by ID", description = "Retrieves a user's information by their ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user", content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true) @PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/api/users/{id}")
    @ResponseBody
    @Operation(summary = "Update a user", description = "Updates an existing user's information")
    @ApiResponse(responseCode = "200", description = "Successfully updated user", content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "ID of the user to update", required = true) @PathVariable Long id,
            @RequestBody User user) {
        if (!userService.getUserById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseBody
    @Operation(summary = "Delete a user", description = "Deletes an existing user")
    @ApiResponse(responseCode = "204", description = "Successfully deleted user")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete", required = true) @PathVariable Long id) {
        if (!userService.getUserById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/profile")
    @Operation(summary = "Show user profile", description = "Displays the profile page for the authenticated user")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        List<Video> userVideos = videoService.getVideosByUser(currentUser);
        List<Video> likedVideos = videoService.getLikedVideosByUser(currentUser);
        List<Comment> userComments = commentService.getCommentsByUser(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("followerCount", followService.getFollowerCount(currentUser));
        model.addAttribute("followingCount", followService.getFollowingCount(currentUser));
        model.addAttribute("followers", followService.getFollowers(currentUser));
        model.addAttribute("following", followService.getFollowing(currentUser));
        model.addAttribute("userVideos", userVideos);
        model.addAttribute("likedVideos", likedVideos);
        model.addAttribute("userComments", userComments);

        return "profile";
    }

    @PostMapping("/profile/delete-video/{id}")
    @Operation(summary = "Delete a video", description = "Deletes a video owned by the authenticated user")
    @ApiResponse(responseCode = "302", description = "Redirects to profile page after deletion")
    public String deleteVideo(
            @Parameter(description = "ID of the video to delete", required = true) @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());

        Optional<Video> videoOptional = videoService.getVideoById(id);
        if (videoOptional.isPresent() && videoOptional.get().getUser().getId().equals(currentUser.getId())) {
            videoService.deleteVideo(id);
        }

        return "redirect:/profile";
    }

    @GetMapping("/user/{username}/videos")
    public String showUserVideos(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return "error"; // 사용자를 찾을 수 없을 때 에러 페이지로 리다이렉트
        }
        List<Video> userVideos = videoService.getVideosByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("userVideos", userVideos);
        return "user-videos";
    }
}
