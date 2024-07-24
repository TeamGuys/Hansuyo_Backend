package com.ardkyer.rion.repository;

import com.ardkyer.rion.entity.Comment;
import com.ardkyer.rion.entity.CommentLike;
import com.ardkyer.rion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    CommentLike findByCommentAndUser(Comment comment, User user);
}