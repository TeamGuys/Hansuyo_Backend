package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.*;
import com.ardkyer.rion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class LikeServiceImpl implements LikeService {
    private final LikeRepository likeRepository;

    @Autowired
    public LikeServiceImpl(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    @Override
    @Transactional
    public boolean toggleLike(User user, Video video) {
        Optional<Like> existingLike = likeRepository.findByUserAndVideo(user, video);
        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            return false;
        } else {
            Like newLike = new Like();
            newLike.setUser(user);
            newLike.setVideo(video);
            likeRepository.save(newLike);
            return true;
        }
    }

    @Override
    @Transactional
    public void removeLike(User user, Video video) {
        likeRepository.deleteByUserAndVideo(user, video);
    }

    @Override
    public boolean hasUserLikedVideo(User user, Video video) {
        return likeRepository.existsByUserAndVideo(user, video);
    }

    @Override
    public long getLikeCountForVideo(Video video) {
        return likeRepository.countByVideo(video);
    }
}