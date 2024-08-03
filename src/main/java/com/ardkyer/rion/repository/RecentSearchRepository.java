package com.ardkyer.rion.repository;

import com.ardkyer.rion.entity.RecentSearch;
import com.ardkyer.rion.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {
    List<RecentSearch> findByUserOrderBySearchTimeDesc(User user);
    Optional<RecentSearch> findByUserAndKeyword(User user, String keyword);
}