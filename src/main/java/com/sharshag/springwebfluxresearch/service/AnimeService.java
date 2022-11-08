package com.sharshag.springwebfluxresearch.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sharshag.springwebfluxresearch.domain.Anime;
import com.sharshag.springwebfluxresearch.repository.AnimeRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AnimeService {
    
    private final AnimeRepository animeRepository;

    public Flux<Anime> findAll() {
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(int id) {
        return animeRepository.findById(id)
            .switchIfEmpty(monoResponseStatusNotFoundException())
        . log();
    }

    public <T> Mono<T> monoResponseStatusNotFoundException() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found"));
    }

    public Mono<Anime> save(Anime anime) {
        Mono<Anime> save = animeRepository.save(anime);
        return save;
    }

    public Mono<Void> update(Anime anime) {
              
        return findById(anime.getId())
                .map(animeFound -> anime.withId(animeFound.getId()))
                .flatMap(animeRepository::save)
                .thenEmpty(Mono.empty());
    }

    public Mono<Void> deleteById(int id) {
        return animeRepository.deleteById(id);
    }
}
