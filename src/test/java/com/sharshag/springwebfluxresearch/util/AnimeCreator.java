package com.sharshag.springwebfluxresearch.util;

import com.sharshag.springwebfluxresearch.domain.Anime;

public class AnimeCreator {

    public static Anime createAnimeToBeSaved() {
        return Anime.builder()
            .name("Tensei Shitara Slime Datta Ken")
            .build();
    }

    public static Anime createValidAnime() {
        return Anime.builder()
            .id(1)
            .name("Tensei Shitara Slime Datta Ken")
            .build();
    }

    public static Anime createValidUpdatedAnime() {
        return Anime.builder()
            .id(1)
            .name("Tensei Shitara Slime Datta Ken 2")
            .build();
    }

    public static Anime createUnSavedAnimeForUpdate() {
        return Anime.builder()
            .id(7)
            .name("Tensei Shitara Slime Datta Ken 2")
            .build();
    }
    
}
