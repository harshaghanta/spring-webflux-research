package com.sharshag.springwebfluxresearch.integration;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.sharshag.springwebfluxresearch.domain.Anime;
import com.sharshag.springwebfluxresearch.repository.AnimeRepository;
import com.sharshag.springwebfluxresearch.util.AnimeCreator;
import com.sharshag.springwebfluxresearch.util.WebTestClientUtil;

import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@ExtendWith(SpringExtension.class)
// @WebFluxTest
// @Import({AnimeService.class, CustomAttributes.class, WebTestClientUtil.class})
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class AnimeControllerIT {

    private static final String ADMIN_USER = "devdojo";
    private static final String REGULAR_USER = "harshaghanta";
    
    @Autowired
    private WebTestClientUtil webTestClientUtil;

    @MockBean
    private AnimeRepository animeRepositoryMock;

    
    private WebTestClient testClientUser;
    private WebTestClient testClientAdmin;
    private WebTestClient testClientInvalid;
    
    @Autowired
    private WebTestClient testClient;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    public void setup() {

        testClientUser = webTestClientUtil.authenticateClient("harshaghanta", "devdojo");

        testClientAdmin = webTestClientUtil.authenticateClient("devdojo", "devdojo");

        testClientInvalid = webTestClientUtil.authenticateClient("harshaghanta", "devdojo234");

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

        testClientUser.get()
            .uri("/animes")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.[0].id").isEqualTo(anime.getId())
            .jsonPath("$.[0].name").isEqualTo(anime.getName());
            
    }


    @Test
    public void listAll_Flavor2_ReturnFluxOfAnime_WhenSuccessful() {

        testClientUser.get()
            .uri("/animes")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Anime.class)
            .hasSize(1);
            
    }

    @Test
    public void findById_ReturnMonoOfAnime_WhenSuccessful() {

        testClientUser.get()
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

        testClientUser.get()
            .uri("/animes/{id}", 5)
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .jsonPath("$.status").isEqualTo(404)
            .jsonPath("$.developerMessage").isEqualTo("ResponseStatusException happened");
            
    }

    @Test
    @WithUserDetails(value = ADMIN_USER)
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

            testClientAdmin.post()
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

        testClientAdmin.delete()
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

        testClientAdmin.put()
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

        testClientAdmin.put()
            .uri("/animes/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(anime))            
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    public void saveBatch_Creates_ListOfAnime_WhenSuccessful() {

        List<Anime> animes = List.of(anime.withName("Hulk"), anime.withName("Thor"));

        BDDMockito.when(animeRepositoryMock.saveAll(ArgumentMatchers.anyList()))
            .thenReturn(Flux.fromIterable(animes));

            testClientAdmin.post()
            .uri("/animes/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(animes))
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.[0].name").isEqualTo("Hulk")
            .jsonPath("$.[1].name").isEqualTo("Thor");
    }

    @Test
    public void saveBatch_Gives_Error_WhenName_IsEmpty() {

        List<Anime> animes = List.of(anime.withName("Hulk"), anime.withName(""));

        BDDMockito.when(animeRepositoryMock.saveAll(ArgumentMatchers.anyList()))
            .thenReturn(Flux.fromIterable(animes));

        testClientUser.post()
            .uri("/animes/batch")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(animes))
            .exchange()
            .expectStatus().is4xxClientError();
            
    }

    @Test
    public void all_Post_API_Should__Fail_With_UserRole() {
        
        testClientUser.post()
        .uri("/animes/")
        .body(BodyInserters.fromValue(AnimeCreator.createAnimeToBeSaved()))
        .exchange()
        .expectStatus().isForbidden(); 

    }

    @Test
    public void all_Put_API_Should__Fail_With_UserRole() {
        
        testClientUser.method(HttpMethod.PUT)
        .uri("/animes/")
        .body(BodyInserters.fromValue(AnimeCreator.createAnimeToBeSaved()))
        .exchange()
        .expectStatus().isForbidden();
    }

    @Test
    public void all_Post_API_Should__Succeed_With_AdminRole() {
        
        testClientAdmin.method(HttpMethod.POST)
        .uri("/animes/")
        .body(BodyInserters.fromValue(AnimeCreator.createAnimeToBeSaved()))
        .exchange()
        .expectStatus().isCreated();
    }

    @Test
    public void invalid_credentials_Should_Return_UnAuthorized() {
        
        testClientInvalid.method(HttpMethod.POST)
        .uri("/animes/")
        .body(BodyInserters.fromValue(AnimeCreator.createAnimeToBeSaved()))
        .exchange()
        .expectStatus().isUnauthorized();
    }

}
