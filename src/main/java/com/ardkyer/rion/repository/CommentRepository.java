package com.ardkyer.rion.repository;

import com.ardkyer.rion.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByVideoOrderByCreatedAtDesc(Video video, Pageable pageable);
    long countByVideo(Video video);
    Optional<Comment> findTopByVideoOrderByLikeCountDesc(Video video);
    List<Comment> findByUser(User user);
}