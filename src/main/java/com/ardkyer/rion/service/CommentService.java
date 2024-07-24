package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface CommentService {
    Comment addComment(Comment comment);
    Optional<Comment> getCommentById(Long id);
    Page<Comment> getCommentsByVideo(Video video, Pageable pageable);
    Comment updateComment(Comment comment);
    void deleteComment(Long id);
    long getCommentCountForVideo(Video video);
    Optional<Comment> getTopCommentForVideo(Video video);
    List<Comment> getCommentsByUser(User user);
    Map<String, Object> toggleLike(Long commentId, User user);
}