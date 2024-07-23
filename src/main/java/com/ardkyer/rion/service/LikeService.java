package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.*;

public interface LikeService {
    boolean toggleLike(User user, Video video);
    void removeLike(User user, Video video);
    boolean hasUserLikedVideo(User user, Video video);
    long getLikeCountForVideo(Video video);
}