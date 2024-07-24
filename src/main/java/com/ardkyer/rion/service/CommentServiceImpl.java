package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.*;
import com.ardkyer.rion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, CommentLikeRepository commentLikeRepository) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
    }

    @Override
    @Transactional
    public Comment addComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    public Optional<Comment> getCommentById(Long id) {
        return commentRepository.findById(id);
    }

    @Override
    public Page<Comment> getCommentsByVideo(Video video, Pageable pageable) {
        return commentRepository.findByVideoOrderByLikeCountDescCreatedAtDesc(video, pageable);
    }

    @Override
    @Transactional
    public Comment updateComment(Comment comment) {
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public long getCommentCountForVideo(Video video) {
        return commentRepository.countByVideo(video);
    }

    @Override
    public Optional<Comment> getTopCommentForVideo(Video video) {
        return commentRepository.findTopByVideoOrderByLikeCountDesc(video);
    }

    @Override
    public List<Comment> getCommentsByUser(User user) {
        return commentRepository.findByUser(user);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean liked = false;
        CommentLike commentLike = commentLikeRepository.findByCommentAndUser(comment, user);
        if (commentLike == null) {
            commentLike = new CommentLike();
            commentLike.setComment(comment);
            commentLike.setUser(user);
            commentLikeRepository.save(commentLike);
            comment.setLikeCount(comment.getLikeCount() + 1);
            liked = true;
        } else {
            commentLikeRepository.delete(commentLike);
            comment.setLikeCount(comment.getLikeCount() - 1);
        }
        commentRepository.save(comment);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likeCount", comment.getLikeCount());
        return result;
    }

}