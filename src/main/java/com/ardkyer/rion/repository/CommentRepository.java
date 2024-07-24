package com.ardkyer.rion.repository;

import com.ardkyer.rion.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByVideoOrderByCreatedAtDesc(Video video, Pageable pageable);
    long countByVideo(Video video);
    Optional<Comment> findTopByVideoOrderByLikeCountDesc(Video video);
    List<Comment> findByUser(User user);
    Page<Comment> findByVideoOrderByLikeCountDescCreatedAtDesc(Video video, Pageable pageable);
    @Query("SELECT c FROM Comment c WHERE c.video = :video ORDER BY c.likeCount DESC, c.createdAt DESC")
    List<Comment> findTop5ByVideoOrderByLikeCountDescCreatedAtDesc(@Param("video") Video video, Pageable pageable);
}