package com.ardkyer.rion.service;

import com.amazonaws.services.s3.model.S3Object;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ardkyer.rion.entity.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public interface VideoService {
    Video uploadVideo(Video video, MultipartFile file , Set<String> hashtagNames) throws IOException;
    S3Object getVideoFile(String fileName);
    Optional<Video> getVideoById(Long id);
    List<Video> getVideosByUser(User user);
    List<Video> getLikedVideosByUser(User user);
    List<Video> getTopVideos();
    Video updateVideo(Video video);
    void deleteVideo(Long id);
    List<Video> getAllVideos();
    void incrementViewCount(Long videoId);
    List<Video> getAllVideosWithComments();
    List<Video> getAllVideosWithSortedComments();
    void saveHashtagsFromDescription(String description);
    List<Video> searchVideos(String query);
    List<Video> searchVideosByHashtags(Set<String> hashtags);
}