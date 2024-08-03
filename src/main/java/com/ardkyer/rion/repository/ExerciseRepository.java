package com.ardkyer.rion.repository;

import com.ardkyer.rion.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    Exercise findByName(String name);
    @Query("SELECT h.name FROM Exercise e JOIN e.hashtags h WHERE e.name = :name")
    Set<String> findHashtagsByExerciseName(@Param("name") String name);
}