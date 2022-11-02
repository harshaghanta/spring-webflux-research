package com.sharshag.springwebfluxresearch.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.sharshag.springwebfluxresearch.domain.Anime;

public interface AnimeRepository extends ReactiveCrudRepository<Anime, Integer> {
    
}
