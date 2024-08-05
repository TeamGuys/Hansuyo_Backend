package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.RecentSearch;
import com.ardkyer.rion.entity.User;

import java.util.List;

public interface RecentSearchService {
    List<RecentSearch> getRecentSearches(User user);
    void addOrUpdateRecentSearch(User user, String keyword);
    void deleteRecentSearch(User user, String keyword);
    void deleteAllRecentSearches(User user);
}