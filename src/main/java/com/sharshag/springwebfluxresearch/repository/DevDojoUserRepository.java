package com.sharshag.springwebfluxresearch.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.sharshag.springwebfluxresearch.domain.DevDojoUser;

import reactor.core.publisher.Mono;

@Repository
public interface DevDojoUserRepository extends ReactiveCrudRepository<DevDojoUser, Integer> {

    Mono<DevDojoUser> findByUsername(String username);
    
}
