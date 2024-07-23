package com.ardkyer.rion.repository;

import com.ardkyer.rion.entity.Like;
import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndVideo(User user, Video video);
    void deleteByUserAndVideo(User user, Video video);
    boolean existsByUserAndVideo(User user, Video video);
    long countByVideo(Video video);
}