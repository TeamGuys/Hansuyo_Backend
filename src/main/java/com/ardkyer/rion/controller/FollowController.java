package com.ardkyer.rion.controller;

import com.ardkyer.rion.entity.*;
import com.ardkyer.rion.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import com.ardkyer.rion.dto.UserDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/follows")
@Tag(name = "Follow", description = "Follow management API")
public class FollowController {
    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> followUser(@RequestParam Long followedId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            User follower = userService.findByUsername(authentication.getName());
            User followed = userService.findById(followedId);
            boolean isNowFollowing = followService.toggleFollow(follower, followed);
            return ResponseEntity.ok().body(Map.of("success", true, "isFollowing", isNowFollowing));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> unfollowUser(@RequestParam Long followedId, Authentication authentication) {
        try {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            User follower = userService.findByUsername(username);
            User followed = userService.findByUsername(followedId.toString()); // 여기를 적절히 수정해야 할 수 있습니다.
            followService.unfollowUser(follower, followed);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Successfully unfollowed user"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> isFollowing(@RequestParam Long followedId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            User follower = userService.findByUsername(authentication.getName());
            User followed = userService.findById(followedId);
            boolean isFollowing = followService.isFollowing(follower, followed);
            return ResponseEntity.ok().body(Map.of("isFollowing", isFollowing));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowers(@PathVariable Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.findById(userId);
        List<User> followers = followService.getFollowers(user);
        List<UserDTO> followerDTOs = followers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(followerDTOs);
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<UserDTO>> getFollowing(@PathVariable Long userId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userService.findById(userId);
        List<User> following = followService.getFollowing(user);
        List<UserDTO> followingDTOs = following.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(followingDTOs);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // 필요한 다른 필드들 설정
        return dto;
    }

    @GetMapping("/count/followers/{userId}")
    @Operation(summary = "Get follower count", description = "Retrieves the number of followers for a specified user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved follower count")
    public ResponseEntity<Long> getFollowerCount(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        User user = userService.findById(userId);
        long followerCount = followService.getFollowerCount(user);
        return new ResponseEntity<>(followerCount, HttpStatus.OK);
    }

    @GetMapping("/count/following/{userId}")
    @Operation(summary = "Get following count", description = "Retrieves the number of users a specified user is following")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved following count")
    public ResponseEntity<Long> getFollowingCount(
            @Parameter(description = "ID of the user") @PathVariable Long userId) {
        User user = userService.findById(userId);
        long followingCount = followService.getFollowingCount(user);
        return new ResponseEntity<>(followingCount, HttpStatus.OK);
    }
}