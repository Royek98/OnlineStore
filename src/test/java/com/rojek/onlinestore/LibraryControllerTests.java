package com.rojek.onlinestore;

import com.rojek.onlinestore.auth.AuthenticationRequest;
import com.rojek.onlinestore.auth.AuthenticationResponse;
import com.rojek.onlinestore.game.Game;
import com.rojek.onlinestore.game.GameRepository;
import com.rojek.onlinestore.library.LibraryRepository;
import com.rojek.onlinestore.library.LibraryService;
import com.rojek.onlinestore.user.User;
import com.rojek.onlinestore.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryControllerTests {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private LibraryService libraryService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private LibraryRepository libraryRepository;

    @Test
    public void getAllTest() {

        User user = userRepository.findByEmail("admin@admin.com").orElse(null);
        assertNotNull(user);

        Game game = gameRepository.findGameById(1).orElse(null);
        assertNotNull(game);

        HttpHeaders headers = authenticate();

        // if detailsTest() run as first just remove entry from db
        if (!libraryService.findByGame(game).isEmpty()) {
            libraryRepository.delete(libraryService.findByGame(game).get(0));
        }

        // buying the game
        libraryService.saveToLibrary(game, user);

        // SHOULD PASS if games are found
        sendRequestAndTestResponse(
                "/api/library",
                new HttpEntity<>(headers),
                HttpMethod.GET,
                Game[].class, // Library throws errors with granted authorities //toDo make DTO of Library without User
                HttpStatus.OK,
                null
        );

    }

    @Test
    public void detailsTest() {

        User user = userRepository.findByEmail("admin@admin.com").orElse(null);
        assertNotNull(user);

        Game game = gameRepository.findGameById(1).orElse(null);
        assertNotNull(game);

        HttpHeaders headers = authenticate();

        // SHOULD PASS if user don't own the game
        sendRequestAndTestResponse(
                "/api/library/details?gameId=1",
                new HttpEntity<>(headers),
                HttpMethod.GET,
                String.class,
                HttpStatus.NOT_FOUND,
                "You dont own this game"
        );

        // buying the game
        libraryService.saveToLibrary(game, user);

        // SHOULD PASS if game is found
        sendRequestAndTestResponse(
                "/api/library/details?gameId=1",
                new HttpEntity<>(headers),
                HttpMethod.GET,
                Game.class,
                HttpStatus.OK,
                null
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
