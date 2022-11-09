package com.sharshag.springwebfluxresearch.service;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import com.sharshag.springwebfluxresearch.domain.Anime;
import com.sharshag.springwebfluxresearch.repository.AnimeRepository;
import com.sharshag.springwebfluxresearch.util.AnimeCreator;

import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
public class AnimeServiceTest {
    
    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository animeRepositoryMock;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setup() {
        BDDMockito.when(animeRepositoryMock.findAll())
            .thenReturn(Flux.just(anime));


        
    }

    @Test
    public void blockHoundWorks() {
        try 
        {
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
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {

        StepVerifier.create(animeService.findAll())
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete();

    }

    @Test
    @DisplayName("findById returns a Mono of anime when exists")
    public void findById_ReturnMonoOfAnime_WhenSuccessful() {

        BDDMockito.when(animeRepositoryMock.findById(1)).thenReturn(Mono.just(anime));

        StepVerifier.create(animeService.findById(1))
            .expectSubscription()
            .expectNext(anime)
            .verifyComplete();

    }

    @Test
    @DisplayName("findById returns Mono error when anime doesn't exist")
    public void findById_ReturnMonoOfError_WhenUnSuccessful() {

        BDDMockito.when(animeRepositoryMock.findById(2)).thenReturn(Mono.empty());

        StepVerifier.create(animeService.findById(2))
            .expectSubscription()
            .expectError(ResponseStatusException.class)
            .verify();

    }

    @Test
    @DisplayName("save returns Mono Anime when successful")
    public void save_ReturnMonoOfAnime_WhenSuccessful() {

        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        BDDMockito.when(animeRepositoryMock.save(animeToBeSaved))
            .thenReturn(Mono.just(animeToBeSaved));

        StepVerifier.create(animeService.save(animeToBeSaved))
            .expectSubscription()
            .expectNext(animeToBeSaved)
            .verifyComplete();

    }

    @Test
    @DisplayName("delete returns a Mono of Void when found or not found")
    public void delete_ReturnMonoOfVoid_WhenSuccessful() {

        BDDMockito.when(animeRepositoryMock.deleteById(1)).thenReturn(Mono.empty());

        StepVerifier.create(animeService.deleteById(1))
            .expectSubscription()
            .verifyComplete();

    }

    @Test
    @DisplayName("update returns Mono of Void when successful or not")
    public void update_ReturnMonoOfVoid_WhenSuccessful() {

        Anime updatedAnime = AnimeCreator.createValidUpdatedAnime();

        BDDMockito.when(animeRepositoryMock.save(updatedAnime)).thenReturn(Mono.just(updatedAnime));

        StepVerifier.create(animeService.save(updatedAnime))
            .expectSubscription()
            .expectNextCount(1)
            .verifyComplete();

    }

    @Test
    @DisplayName("update returns Mono of error when unsuccessful ")
    public void update_ReturnMonoOfError_WhenUnSuccessful() {

        Anime notFoundAnime = AnimeCreator.createUnSavedAnimeForUpdate();

        BDDMockito.when(animeRepositoryMock.findById(notFoundAnime.getId()))
            .thenReturn(Mono.empty());

        StepVerifier.create(animeService.update(notFoundAnime))
            .expectSubscription()
            .expectError(ResponseStatusException.class)
            .verify();

    }

    @Test
    public void saveAll_Returns_FluxOfAnime_WhenSuccessful() {
        List<Anime> animes = List.of(anime, anime);
        
        BDDMockito.when(animeRepositoryMock.saveAll(animes)).thenReturn(Flux.fromIterable(animes));
        
        StepVerifier.create(animeService.saveAll(animes))
            .expectSubscription()
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    public void saveAll_Returns_MonoError_WhenInvalid() {
        List<Anime> animes = List.of(anime, anime.withName(""));
        
        BDDMockito.when(animeRepositoryMock.saveAll(ArgumentMatchers.anyIterable()))
            .thenReturn(Flux.fromIterable(animes));
        
        StepVerifier.create(animeService.saveAll(animes))
            .expectSubscription()
            .expectNext(anime)
            .expectError(ResponseStatusException.class)
            .verify();
    }
    

}
