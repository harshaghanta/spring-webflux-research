package com.sharshag.springwebfluxresearch.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sharshag.springwebfluxresearch.domain.Anime;
import com.sharshag.springwebfluxresearch.service.AnimeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("animes")
@Slf4j
@RequiredArgsConstructor
@SecurityScheme(name = "Basic Authentication",
    type = SecuritySchemeType.HTTP,
    scheme = "basic" 
)
public class AnimeController {
    
    private final AnimeService animeService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lists all animes", tags = { "animes"}, 
        security = @SecurityRequirement(name = "Basic Authentication")
    )
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping
    public Flux<Anime> listAll() {
        log.info("list all method called..");
         Flux<Anime> animes = animeService.findAll();        
         log.info("list all methoc completed");
        //  animes.subscribe(x-> System.out.println(x));
         return animes;
    }

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(path = "/{id}")
    public Mono<Anime> findById(@PathVariable int id) {
        return animeService.findById(id);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Anime> save(@Valid @RequestBody Anime anime) {
        return animeService.save(anime);
    }

    // @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public Flux<Anime> save(@RequestBody List<Anime> animes) {
        return animeService.saveAll(animes);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> update(@PathVariable int id, @Valid @RequestBody Anime anime) {
        return animeService.update(anime.withId(id));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable int id) {
        Mono<Void> deleted = animeService.deleteById(id);
        return deleted;
        
    }

}