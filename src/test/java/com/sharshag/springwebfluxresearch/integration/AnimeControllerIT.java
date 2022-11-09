package com.sharshag.springwebfluxresearch.integration;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.sharshag.springwebfluxresearch.domain.Anime;
import com.sharshag.springwebfluxresearch.exception.CustomAttributes;
import com.sharshag.springwebfluxresearch.repository.AnimeRepository;
import com.sharshag.springwebfluxresearch.service.AnimeService;
import com.sharshag.springwebfluxresearch.util.AnimeCreator;

import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import({AnimeService.class, CustomAttributes.class})
public class AnimeControllerIT {
    
    @MockBean
    private AnimeRepository animeRepositoryMock;

    @Autowired
    private WebTestClient testClient;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setup() {

        BDDMockito.when(animeRepositoryMock.findAll())
            .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepositoryMock.findById(1))
            .thenReturn(Mono.just(anime));



    }
    
    @Test
    public void blockHoundWorks() {
        try {
            FutureTask<String> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }

    }

    @Test
    public void listAll_ReturnFluxOfAnime_WhenSuccessful() {

        testClient.get()
            .uri("/animes")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.[0].id").isEqualTo(anime.getId())
            .jsonPath("$.[0].name").isEqualTo(anime.getName());
            
    }


    @Test
    public void listAll_Flavor2_ReturnFluxOfAnime_WhenSuccessful() {

        testClient.get()
            .uri("/animes")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Anime.class)
            .hasSize(1);
            
    }

    @Test
    public void findById_ReturnMonoOfAnime_WhenSuccessful() {

        testClient.get()
            .uri("/animes/{id}", 1)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Anime.class)
            .isEqualTo(anime);

    }

    @Test
    public void findById_ReturnMonoError_WhenEmptyMonoIsReturned() {

        BDDMockito.when(animeRepositoryMock.findById(5))
            .thenReturn(Mono.empty());

        testClient.get()
            .uri("/animes/{id}", 5)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("ResponseStatusException happened");
            
    }

    @Test
    public void save_CreatesAnime_WhenSuccessful() {

        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        BDDMockito.when(animeRepositoryMock.save(animeToBeSaved))
            .thenReturn(Mono.just(animeToBeSaved));

        testClient.post()
            .uri("/animes")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(animeToBeSaved))
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Anime.class)
            .isEqualTo(animeToBeSaved);

    }

    @Test
    public void save_ReturnsError_WhenNameIsEmpty() {

        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");

        BDDMockito.when(animeRepositoryMock.save(animeToBeSaved))
            .thenReturn(Mono.just(animeToBeSaved));

        testClient.post()
            .uri("/animes")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(animeToBeSaved))
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.status").isEqualTo(400);
            
    }

    @Test
    public void delete_ReturnsNoContent_WhenSuccessOrFail() {

        BDDMockito.when(animeRepositoryMock.deleteById(1)).thenReturn(Mono.empty());

        testClient.delete()
            .uri("/animes/{id}", 1)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    public void update_ReturnsNoContent_WhenSuccess() {

        // Anime animeToBeUpdated = Anime.builder().id(5).name("Spiderman").build();

        // BDDMockito.when(animeRepositoryMock.findById(1).thenReturn(Mono.just(animeToBeUpdated)));

        Anime animeTobeUpdated = AnimeCreator.createValidAnime();
        BDDMockito.when(animeRepositoryMock.save(animeTobeUpdated)).thenReturn(Mono.just(animeTobeUpdated));

        testClient.put()
            .uri("/animes/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(animeTobeUpdated))            
            .exchange()
            .expectStatus()
            .isNoContent();
    }

    @Test
    public void update_ReturnsNoContent_WhenFail() {

        BDDMockito.when(animeRepositoryMock.findById(1)).thenReturn(Mono.empty());        

        testClient.put()
            .uri("/animes/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(anime))            
            .exchange()
            .expectStatus()
            .isNotFound();
    }

}
