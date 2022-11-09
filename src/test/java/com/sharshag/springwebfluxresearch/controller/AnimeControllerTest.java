package com.sharshag.springwebfluxresearch.controller;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import com.sharshag.springwebfluxresearch.domain.Anime;
import com.sharshag.springwebfluxresearch.service.AnimeService;
import com.sharshag.springwebfluxresearch.util.AnimeCreator;

import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class AnimeControllerTest {

    @InjectMocks
    private AnimeController animeController;

    @Mock
    private AnimeService animeServiceMock;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setup() {
        BDDMockito.when(animeServiceMock.findAll())
                .thenReturn(Flux.just(anime));
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
    @DisplayName("Listall returns a flux of anime")
    public void listAll_ReturnFluxOfAnime_WhenSuccessful() {

        StepVerifier.create(animeController.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("findById returns a Mono of anime when exists")
    public void findById_ReturnMonoOfAnime_WhenSuccessful() {

        BDDMockito.when(animeServiceMock.findById(1)).thenReturn(Mono.just(anime));

        StepVerifier.create(animeController.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

    }

    @Test
    @DisplayName("findById returns Mono error when anime doesn't exist")
    public void findById_ReturnMonoOfError_WhenUnSuccessful() {

        BDDMockito.when(animeServiceMock.findById(2)).thenReturn(Mono.error(new RuntimeException()));

        StepVerifier.create(animeController.findById(2))
                .expectSubscription()
                .expectError()
                .verify();

    }

    @Test
    @DisplayName("save returns Mono Anime when successful")
    public void save_ReturnMonoOfAnime_WhenSuccessful() {

        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        BDDMockito.when(animeServiceMock.save(animeToBeSaved))
                .thenReturn(Mono.just(animeToBeSaved));

        StepVerifier.create(animeController.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(animeToBeSaved)
                .verifyComplete();

    }

    @Test
    @DisplayName("saveAll returns Flux of Anime when successful")
    public void saveAll_ReturnFluxOfAnime_WhenSuccessful() {

        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        BDDMockito.when(animeServiceMock.saveAll(List.of(animeToBeSaved)))
                .thenReturn(Flux.fromIterable(List.of(animeToBeSaved)));

        StepVerifier.create(animeController.save(List.of(animeToBeSaved)))
                .expectSubscription()
                .expectNext(animeToBeSaved)
                .verifyComplete();
                

    }

    @Test
    @DisplayName("saveAll returns MonoError when invalid")
    public void saveAll_ReturnMonoError_WhenInvalid() {

        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        BDDMockito.when(animeServiceMock.saveAll(ArgumentMatchers.anyList()))
                .thenReturn(Flux.just(animeToBeSaved).concatWith(Flux.error(new ResponseStatusException(HttpStatus.BAD_REQUEST))));

        StepVerifier.create(animeController.save(List.of(animeToBeSaved, animeToBeSaved.withName(""))))
                .expectSubscription()
                .expectNext(animeToBeSaved)
                .expectError(ResponseStatusException.class)
                .verify();

    }

    @Test
    @DisplayName("delete returns a Mono of Void when found or not found")
    public void delete_ReturnMonoOfVoid_WhenSuccessful() {

        BDDMockito.when(animeServiceMock.deleteById(1)).thenReturn(Mono.empty());

        StepVerifier.create(animeController.delete(1))
                .expectSubscription()
                .verifyComplete();

    }

    @Test
    @DisplayName("update returns Mono of Void when successful or not")
    public void update_ReturnMonoOfVoid_WhenSuccessful() {

        Anime updatedAnime = AnimeCreator.createValidUpdatedAnime();

        BDDMockito.when(animeServiceMock.save(updatedAnime)).thenReturn(Mono.just(updatedAnime));

        StepVerifier.create(animeController.save(updatedAnime))
                .expectSubscription()
                .expectNext(updatedAnime)
                // .expectNextCount(1)
                .verifyComplete();

    }

    @Test
    @DisplayName("update returns Mono of error when unsuccessful ")
    public void update_ReturnMonoOfError_WhenUnSuccessful() {

        Anime notFoundAnime = AnimeCreator.createUnSavedAnimeForUpdate();

        BDDMockito.when(animeServiceMock.update(notFoundAnime))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeController.update(notFoundAnime.getId(), notFoundAnime))
                .expectSubscription()
                .verifyComplete();
    }
}
