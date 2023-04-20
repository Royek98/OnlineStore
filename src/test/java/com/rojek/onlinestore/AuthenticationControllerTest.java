package com.rojek.onlinestore;

import com.rojek.onlinestore.auth.AuthenticationRequest;
import com.rojek.onlinestore.auth.AuthenticationResponse;
import com.rojek.onlinestore.auth.AuthenticationService;
import com.rojek.onlinestore.auth.RegisterRequest;
import com.rojek.onlinestore.config.JwtService;
import com.rojek.onlinestore.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthenticationControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    public void registerTest() {
        // create a register request object
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("testuser@test.com")
                .password("testpassword")
                .build();

        // send the request
        ResponseEntity<AuthenticationResponse> responseEntity =
                restTemplate.postForEntity("/api/auth/register", registerRequest, AuthenticationResponse.class);

        // check the response status
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // check the response body
        AuthenticationResponse authenticationResponse = responseEntity.getBody();
        assertNotNull(authenticationResponse);
        assertEquals(HttpStatus.OK, authenticationResponse.getStatus());

        // check that the user has been saved to the database
        Optional<User> optionalUser = authenticationService.findByEmail("testuser@test.com");
        assertTrue(optionalUser.isPresent());
    }

    @Test
    // test passed if an email is taken
    public void registerEmailTakenTest() {
        // create a register request object
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("admin@admin.com")
                .password("admin")
                .build();

        // send the request
        ResponseEntity<AuthenticationResponse> responseEntity =
                restTemplate.postForEntity("/api/auth/register", registerRequest, AuthenticationResponse.class);

        // check the response status
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        // check the response body status
        AuthenticationResponse authenticationResponse = responseEntity.getBody();
        assertNotNull(authenticationResponse);
        assertEquals(HttpStatus.BAD_REQUEST, authenticationResponse.getStatus());

        // test should pass if email is taken
        assertEquals("Email taken", authenticationResponse.getMessage());

    }

    @Test
    public void authenticateTest() {

        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("admin@admin.com")
                .password("admin")
                .build();

        // send request
        ResponseEntity<AuthenticationResponse> responseEntity =
                restTemplate.postForEntity(
                        "/api/auth/authenticate", authenticationRequest, AuthenticationResponse.class
                );

        // check response status
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        // check body
        AuthenticationResponse authenticationResponse = responseEntity.getBody();
        assertNotNull(authenticationResponse);
        assertEquals(HttpStatus.OK, authenticationResponse.getStatus());

       // check token for a user
        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        boolean isTokenValid = jwtService.isTokenValid(authenticationResponse.getMessage(), userDetails);

        // test should pass if token for a user is valid
        assertTrue(isTokenValid);

    }

    @Test
    // test should pass if email or password incorrect
    public void authenticateUserNotFoundTest() {
        // test if email incorrect
        AuthenticationRequest authenticationRequest = AuthenticationRequest.builder()
                .email("admin2@admin.com")
                .password("admin") // password correct
                .build();

        ResponseEntity<AuthenticationResponse> responseEntity =
                restTemplate.postForEntity(
                        "/api/auth/authenticate", authenticationRequest, AuthenticationResponse.class
                );

        // check response status
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        // check status in body
        AuthenticationResponse authenticationResponse = responseEntity.getBody();
        assertNotNull(authenticationResponse);
        assertEquals(HttpStatus.NOT_FOUND, authenticationResponse.getStatus());

        // should be true if email is incorrect
        assertEquals("Bad credentials", authenticationResponse.getMessage());

        // test if password incorrect
        AuthenticationRequest authenticationRequestBadPassword = AuthenticationRequest.builder()
                .email("admin@admin.com") // email correct
                .password("admin2")
                .build();

        // send request
        ResponseEntity<AuthenticationResponse> responseEntityBadPassword =
                restTemplate.postForEntity(
                        "/api/auth/authenticate", authenticationRequestBadPassword, AuthenticationResponse.class
                );

        // check response status
        assertEquals(HttpStatus.NOT_FOUND, responseEntityBadPassword.getStatusCode());

        // check status in body
        AuthenticationResponse authenticationResponseBadPassword = responseEntity.getBody();
        assertNotNull(authenticationResponseBadPassword);
        assertEquals(HttpStatus.NOT_FOUND, authenticationResponseBadPassword.getStatus());

        // should be true if password is incorrect
        assertEquals("Bad credentials", authenticationResponseBadPassword.getMessage());

    }
}
