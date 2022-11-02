package com.sharshag.springwebfluxresearch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sharshag.springwebfluxresearch.domain.Anime;
import com.sharshag.springwebfluxresearch.service.AnimeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("animes")
@Slf4j
@RequiredArgsConstructor
public class AnimeController {
    
    private final AnimeService animeService;

    @GetMapping
    public Flux<Anime> listAll() {
        log.info("list all method called..");
        return animeService.findAll();
    }

    @GetMapping(path = "/{id}")
    public Mono<Anime> findById(@PathVariable int id) {
        return animeService.findById(id);
    }

}
