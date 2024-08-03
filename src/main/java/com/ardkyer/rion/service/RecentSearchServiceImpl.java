package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.RecentSearch;
import com.ardkyer.rion.entity.User;
import com.ardkyer.rion.repository.RecentSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecentSearchServiceImpl implements RecentSearchService {

    @Autowired
    private RecentSearchRepository recentSearchRepository;

    @Override
    public List<RecentSearch> getRecentSearches(User user) {
        return recentSearchRepository.findByUserOrderBySearchTimeDesc(user);
    }

    @Override
    public void addOrUpdateRecentSearch(User user, String keyword) {
        Optional<RecentSearch> existingSearch = recentSearchRepository.findByUserAndKeyword(user, keyword);
        if (existingSearch.isPresent()) {
            RecentSearch recentSearch = existingSearch.get();
            recentSearch.setSearchTime(LocalDateTime.now());
            recentSearchRepository.save(recentSearch);
        } else {
            RecentSearch newSearch = new RecentSearch();
            newSearch.setUser(user);
            newSearch.setKeyword(keyword);
            newSearch.setSearchTime(LocalDateTime.now());
            recentSearchRepository.save(newSearch);
        }
    }

    @Override
    public void deleteRecentSearch(User user, String keyword) {
        Optional<RecentSearch> existingSearch = recentSearchRepository.findByUserAndKeyword(user, keyword);
        existingSearch.ifPresent(recentSearchRepository::delete);
    }
}