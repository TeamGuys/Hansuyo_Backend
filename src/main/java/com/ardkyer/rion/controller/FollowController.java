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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@Tag(name = "Follow", description = "Follow management API")
public class FollowController {
    private final FollowService followService;

    @Autowired
    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping
    @Operation(summary = "Follow a user", description = "Creates a new follow relationship between two users")
    @ApiResponse(responseCode = "201", description = "Successfully followed user", content = @Content(schema = @Schema(implementation = Follow.class)))
    public ResponseEntity<Follow> followUser(
            @Parameter(description = "ID of the follower") @RequestParam Long followerId,
            @Parameter(description = "ID of the user to be followed") @RequestParam Long followedId) {
        User follower = new User();
        follower.setId(followerId);
        User followed = new User();
        followed.setId(followedId);
        Follow follow = followService.followUser(follower, followed);
        return new ResponseEntity<>(follow, HttpStatus.CREATED);
    }

    @DeleteMapping
    @Operation(summary = "Unfollow a user", description = "Removes an existing follow relationship between two users")
    @ApiResponse(responseCode = "204", description = "Successfully unfollowed user")
    public ResponseEntity<Void> unfollowUser(
            @Parameter(description = "ID of the follower") @RequestParam Long followerId,
            @Parameter(description = "ID of the user to be unfollowed") @RequestParam Long followedId) {
        User follower = new User();
        follower.setId(followerId);
        User followed = new User();
        followed.setId(followedId);
        followService.unfollowUser(follower, followed);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/check")
    @Operation(summary = "Check if a user is following another", description = "Checks if there's a follow relationship between two users")
    @ApiResponse(responseCode = "200", description = "Successfully checked follow status")
    public ResponseEntity<Boolean> isFollowing(
            @Parameter(description = "ID of the potential follower") @RequestParam Long followerId,
            @Parameter(description = "ID of the potential followed user") @RequestParam Long followedId) {
        User follower = new User();
        follower.setId(followerId);
        User followed = new User();
        followed.setId(followedId);
        boolean isFollowing = followService.isFollowing(follower, followed);
        return new ResponseEntity<>(isFollowing, HttpStatus.OK);
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "Get followers of a user", description = "Retrieves a list of users following the specified user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved followers", content = @Content(schema = @Schema(implementation = User.class)))
    public ResponseEntity<List<User>> getFollowers(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        List<User> followers = followService.getFollowers(user);
        return new ResponseEntity<>(followers, HttpStatus.OK);
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "Get users followed by a user", description = "Retrieves a list of users that the specified user is following")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved following users", content = @Content(schema = @Schema(implementation = User.class)))
    public ResponseEntity<List<User>> getFollowing(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        List<User> following = followService.getFollowing(user);
        return new ResponseEntity<>(following, HttpStatus.OK);
    }

    @GetMapping("/count/followers/{userId}")
    @Operation(summary = "Get follower count", description = "Retrieves the number of followers for a specified user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved follower count")
    public ResponseEntity<Long> getFollowerCount(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        long followerCount = followService.getFollowerCount(user);
        return new ResponseEntity<>(followerCount, HttpStatus.OK);
    }

    @GetMapping("/count/following/{userId}")
    @Operation(summary = "Get following count", description = "Retrieves the number of users a specified user is following")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved following count")
    public ResponseEntity<Long> getFollowingCount(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        long followingCount = followService.getFollowingCount(user);
        return new ResponseEntity<>(followingCount, HttpStatus.OK);
    }
}