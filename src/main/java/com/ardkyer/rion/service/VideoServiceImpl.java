package com.ardkyer.rion.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.ardkyer.rion.entity.*;
import com.ardkyer.rion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;
    private final CommentRepository commentRepository;
    private final HashtagRepository hashtagRepository;
    private final AmazonS3 amazonS3Client;
    private LikeService likeService;

    @Value("ardkyerspring1")
    private String bucketName;

    @Autowired
    public VideoServiceImpl(VideoRepository videoRepository,
                            CommentRepository commentRepository,
                            HashtagRepository hashtagRepository,
                            AmazonS3 amazonS3Client,
                            LikeService likeService) {
        this.videoRepository = videoRepository;
        this.commentRepository = commentRepository;
        this.hashtagRepository = hashtagRepository;
        this.amazonS3Client = amazonS3Client;
        this.likeService = likeService;
    }

    @Override
    @Transactional
    public Video uploadVideo(Video video, MultipartFile file, Set<String> hashtagNames) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

        video.setVideoUrl(fileName);

        Set<Hashtag> hashtags = convertNamesToHashtags(hashtagNames);
        video.setHashtags(hashtags);

        video = videoRepository.save(video);

        return video;
    }

    private Set<Hashtag> convertNamesToHashtags(Set<String> hashtagNames) {
        return hashtagNames.stream()
                .map(name -> hashtagRepository.findByName(name)
                        .orElseGet(() -> {
                            Hashtag newHashtag = new Hashtag();
                            newHashtag.setName(name);
                            return hashtagRepository.save(newHashtag);
                        }))
                .collect(Collectors.toSet());
    }

    @Override
    public S3Object getVideoFile(String fileName) {
        return amazonS3Client.getObject(bucketName, fileName);
    }

    @Override
    public Optional<Video> getVideoById(Long id) {
        return videoRepository.findById(id);
    }

    @Override
    public List<Video> getVideosByUser(User user) {
        return videoRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Video> getLikedVideosByUser(User user) {
        return videoRepository.findByLikesUser(user);
    }

    @Override
    public List<Video> getTopVideos() {
        return videoRepository.findTop10ByOrderByViewCountDesc();
    }

    @Override
    @Transactional
    public Video updateVideo(Video video) {
        return videoRepository.save(video);
    }

    @Override
    @Transactional
    public void deleteVideo(Long id) {
        Optional<Video> videoOptional = videoRepository.findById(id);
        if (videoOptional.isPresent()) {
            Video video = videoOptional.get();

            // Delete the file from S3
            amazonS3Client.deleteObject(bucketName, video.getVideoUrl());

            // Remove the video from the database
            videoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Video not found with id: " + id);
        }
    }

    // You might want to add this method to increment view count
    @Transactional
    public void incrementViewCount(Long videoId) {
        Optional<Video> videoOptional = videoRepository.findById(videoId);
        if (videoOptional.isPresent()) {
            Video video = videoOptional.get();
            video.setViewCount(video.getViewCount() + 1);
            videoRepository.save(video);
        }
    }

    @Override
    public List<Video> getAllVideosWithComments() {
        List<Video> videos = videoRepository.findAll();
        for (Video video : videos) {
            video.getComments().size();
        }
        return videos;
    }

    @Override
    public List<Video> getAllVideosWithSortedComments() {
        List<Video> videos = videoRepository.findAll();
        PageRequest topFiveComments = PageRequest.of(0, 5);
        for (Video video : videos) {
            video.setComments(new HashSet<>(commentRepository.findTop5ByVideoOrderByLikeCountDescCreatedAtDesc(video, topFiveComments)));
        }
        return videos;
    }

    @Override
    public void saveHashtagsFromDescription(String description) {
        if (description != null && !description.trim().isEmpty()) {
            Set<String> hashtags = Arrays.stream(description.split(" "))
                    .map(String::trim)
                    .filter(tag -> tag.startsWith("#"))
                    .collect(Collectors.toSet());

            saveHashtags(hashtags);
        }
    }

    @Override
    public List<Video> searchVideos(String query) {
        return videoRepository.findByHashtagsNameContainingOrUserUsernameContaining(query, query);
    }

    @Override
    public List<Video> searchVideosByHashtags(Set<String> hashtags) {
        return videoRepository.findByHashtagsIn(hashtags);
    }

    private void saveHashtags(Set<String> hashtags) {
        hashtags.forEach(name -> hashtagRepository.findByName(name)
                .orElseGet(() -> {
                    Hashtag newHashtag = new Hashtag();
                    newHashtag.setName(name);
                    return hashtagRepository.save(newHashtag);
                }));
    }

    @Override
    public List<Video> getAllVideosOrderByLikeCountDesc() {
        List<Video> videos = videoRepository.findAll();
        for (Video video : videos) {
            video.setLikeCount(likeService.getLikeCountForVideo(video));
        }
        return videos.stream()
                .sorted(Comparator.comparingLong(Video::getLikeCount).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Video> getAllVideos() {
        return getAllVideosOrderByLikeCountDesc();  // 좋아요 수로 정렬된 비디오 목록을 반환
    }

    @Override
    public List<Video> getRandomRecentVideos(int count) {
        List<Video> recentVideos = videoRepository.findRecentVideos(PageRequest.of(0, 100));
        Collections.shuffle(recentVideos);
        return recentVideos.stream().limit(count).collect(Collectors.toList());
    }
}