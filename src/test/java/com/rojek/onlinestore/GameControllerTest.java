package com.rojek.onlinestore;

import com.rojek.onlinestore.auth.AuthenticationRequest;
import com.rojek.onlinestore.auth.AuthenticationResponse;
import com.rojek.onlinestore.game.Game;
import com.rojek.onlinestore.tag.Tag;
import com.rojek.onlinestore.tag.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private TagRepository tagRepository;

    @Test
    public void getAllGamesTest() {

        HttpEntity<?> entity = new HttpEntity<>(null);

        // SHOULD PASS if games are found
        sendRequestAndTestResponse(
                "/api/catalogue/games",
                entity,
                HttpMethod.GET,
                Game[].class,
                HttpStatus.OK,
                null
        );

    }

    @Test
    public void getGameDetailsTest() {

        HttpEntity<?> entity = new HttpEntity<>(null);

        // SHOULD PASS if game is found
        sendRequestAndTestResponse(
                "/api/catalogue/details?gameId=2",
                entity,
                HttpMethod.GET,
                Game.class,
                HttpStatus.OK,
                null
        );

        // SHOULD PASS if game not found
        sendRequestAndTestResponse(
                "/api/catalogue/details?gameId=15",
                entity,
                HttpMethod.GET,
                String.class,
                HttpStatus.NOT_FOUND,
                "Game not found"
        );
    }

    @Test
    public void searchGameTest() {

        HttpEntity<?> entity = new HttpEntity<>(null);

        // SHOULD PASS if games are found
        sendRequestAndTestResponse(
                "/api/catalogue/search?title=test&tags=1",
                entity,
                HttpMethod.GET,
                Game[].class,
                HttpStatus.OK,
                null
        );

        // SHOULD PASS if games are NOT found
        sendRequestAndTestResponse(
                "/api/catalogue/search?title=super&tags=5",
                entity,
                HttpMethod.GET,
                String.class,
                HttpStatus.NOT_FOUND,
                "No matches for searched criteria"
        );

    }

    @Test
    public void purchaseGameTest() {

        // authentication
        HttpHeaders headers = authenticate();

        // SHOULD PASS if games purchase is successful
        sendRequestAndTestResponse(
                "/api/catalogue/purchase?gameId=1",
                new HttpEntity<>(headers),
                HttpMethod.POST,
                String.class,
                HttpStatus.OK,
                "Successfully purchased the game"
        );

        // SHOULD PASS if games purchase is fail: game not found
        sendRequestAndTestResponse(
                "/api/catalogue/purchase?gameId=15",
                new HttpEntity<>(headers),
                HttpMethod.POST,
                String.class,
                HttpStatus.NOT_FOUND,
                "Game not found"
        );

    }

    @Test
    public void gameManagerDeleteGameTest(){

        // authentication
        HttpHeaders headers = authenticate();

        // SHOULD PASS if game is removed
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager?gameId=1",
                new HttpEntity<>(headers),
                HttpMethod.DELETE,
                String.class,
                HttpStatus.OK,
                "Game removed"
        );

        // SHOULD PASS if game not found
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager?gameId=15",
                new HttpEntity<>(headers),
                HttpMethod.DELETE,
                String.class,
                HttpStatus.NOT_FOUND,
                "Game not found"
        );

        // SHOULD PASS if gameID param is empty
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager?gameId=",
                new HttpEntity<>(headers),
                HttpMethod.DELETE,
                String.class,
                HttpStatus.BAD_REQUEST,
                "gameId param is empty"
        );
    }

    @Test
    public void gameManagerSaveNewGameTest() {

        // authentication
        HttpHeaders headers = authenticate();

        List<Tag> tagList = tagRepository.findAll();
        Game newGame = Game.builder()
                .description("This is a game created with a test")
                .price(49.99)
                .title("gameManagerSaveNewGameTest")
                .releaseDate(LocalDate.of(2023, 4, 18))
                .tags(Set.of(tagList.get(2)))
                .build();

        HttpEntity<Game> entityGame = new HttpEntity<>(newGame, headers);

        // SHOULD PASS if game is created
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager",
                entityGame,
                HttpMethod.POST,
                String.class,
                HttpStatus.CREATED,
                "Game added"
        );

        // send post request AGAIN
        // SHOULD PASS if game already exist error
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager",
                entityGame,
                HttpMethod.POST,
                String.class,
                HttpStatus.BAD_REQUEST,
                "Game already exists"
        );

    }

    @Test
    public void gameManagerUpdateGameTest() {

        // authentication
        HttpHeaders headers = authenticate();

        List<Tag> tagList = tagRepository.findAll();

        // SHOULD PASS if game is updated
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager?gameId=1",
                new HttpEntity<>(
                        Game.builder()
                                .title("Edit Title Test")
                                .tags(Set.of(tagList.get(0), tagList.get(1)))
                                .build(),
                        headers
                ),
                HttpMethod.PUT,
                String.class,
                HttpStatus.OK,
                "Game updated"
        );

        // SHOULD PASS if tags for the game are missing
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager?gameId=1",
                new HttpEntity<>(Game.builder()
                        .title("Edit Title Test")
                        .build(), headers
                ),
                HttpMethod.PUT,
                String.class,
                HttpStatus.BAD_REQUEST,
                "Tags are missing"
        );

        // SHOULD PASS if game is not found
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager?gameId=15",
                new HttpEntity<>(Game.builder()
                        .title("Edit Title Test")
                        .build(), headers
                ),
                HttpMethod.PUT,
                String.class,
                HttpStatus.NOT_FOUND,
                "Game not found"
        );

        // SHOULD PASS if gameId param is empty
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager?gameId=",
                new HttpEntity<>(Game.builder()
                        .title("Edit Title Test")
                        .build(), headers
                ),
                HttpMethod.PUT,
                String.class,
                HttpStatus.BAD_REQUEST,
                "gameId param is empty"
        );

        // SHOULD PASS if request body is empty
        sendRequestAndTestResponse(
                "/api/catalogue/gameManager?gameId=1",
                new HttpEntity<>(null, headers),
                HttpMethod.PUT,
                String.class,
                HttpStatus.BAD_REQUEST,
                "Request body is empty"
        );
    }

    private HttpHeaders authenticate() {
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("admin@admin.com")
                .password("admin")
                .build();

        ResponseEntity<AuthenticationResponse> responseAuthentication =
                restTemplate.postForEntity(
                        "/api/auth/authenticate", authenticationRequest, AuthenticationResponse.class
                );

        // check response status for authentication
        assertEquals(HttpStatus.OK, responseAuthentication.getStatusCode());
        // check body
        AuthenticationResponse authenticationResponse = responseAuthentication.getBody();
        assertNotNull(authenticationResponse);

        String token = responseAuthentication.getBody().getMessage();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }


    private void sendRequestAndTestResponse(
            String url,
            HttpEntity<?> entity,
            HttpMethod method,
            Class<?> responseType,
            HttpStatus httpStatusExpected,
            String responseBodyExpected
    ) {

        ResponseEntity<?> responseEntity =
                restTemplate.exchange(url, method, entity, responseType);

        // check status code
        assertEquals(httpStatusExpected, responseEntity.getStatusCode());

        // check body is not null
        assertNotNull(responseEntity.getBody());

        // if responseEntity.getBody() is not a String just use null
        if (responseBodyExpected != null) {
            assertEquals(responseBodyExpected, responseEntity.getBody());
        }

    }
}
