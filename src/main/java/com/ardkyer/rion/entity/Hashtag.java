package com.ardkyer.rion.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Entity
@Table(name = "hashtags")
@Getter
@Setter
public class Hashtag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "hashtags")
    private Set<Video> videos;
}
