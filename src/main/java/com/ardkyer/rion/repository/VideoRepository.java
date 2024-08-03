package com.ardkyer.rion.repository;

import com.ardkyer.rion.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByUserOrderByCreatedAtDesc(User user);
    List<Video> findTop10ByOrderByViewCountDesc();
    List<Video> findByLikesUser(User user);
    List<Video> findByHashtagsNameContainingOrUserUsernameContaining(String hashtag, String username);
    @Query("SELECT v FROM Video v JOIN v.hashtags h WHERE h.name IN :hashtags")
    List<Video> findByHashtagsIn(@Param("hashtags") Set<String> hashtags);
}
