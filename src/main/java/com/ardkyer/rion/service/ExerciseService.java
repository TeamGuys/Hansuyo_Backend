package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.Exercise;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface ExerciseService {
    List<Exercise> getAllExercises();
    Set<String> getHashtagsByExerciseName(String name);
}