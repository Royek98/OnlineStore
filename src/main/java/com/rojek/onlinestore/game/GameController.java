package com.rojek.onlinestore.game;

import com.rojek.onlinestore.user.Role;
import com.rojek.onlinestore.user.User;
import com.rojek.onlinestore.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/catalogue")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final UserService userService;

    @GetMapping("/games")
    public ResponseEntity<List<Game>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/details")
    public ResponseEntity<?> getGameDetails(@RequestParam("gameId") int gameId) {

        // check if game exists
        Optional<Game> game = gameService.findGameById(gameId);
        if (!game.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        return ResponseEntity.ok(game.get());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchGame(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tags", required = false) List<Integer> tags
    ) {
        // filter list by title and tags
        List<Game> games = gameService.searchGames(title, tags);

        if (games.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No matches for searched criteria");
        }

        return ResponseEntity.ok().body(games);
    }

    @PostMapping("/purchase")
    public ResponseEntity<String> buyGame(
            @RequestHeader(name = "Authorization") String bearer,
            @RequestParam("gameId") int gameId) {

        // check user
        Optional<User> user = userService.findUserByBearer(bearer);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // check game
        Optional<Game> game = gameService.findGameById(gameId);
        if (!game.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Game not found");
        }

        try {
            gameService.buyGame(game.get(), user.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        return ResponseEntity.ok().body("Successfully purchased the game");
    }

    @RequestMapping(method = {RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT}, value = "gameManager")
    public ResponseEntity<?> gameManager(
            @RequestHeader(name = "Authorization") String bearer,
            @RequestParam(value = "gameId", required = false) Integer gameId,
            @RequestBody(required = false) Game gameRequest,
            HttpServletRequest request
    ) {
        // check role
        try {
            authenticateAndAuthorize(bearer, Role.ADMIN);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

        // check request method
        String method = request.getMethod();
        if (method.equals("DELETE")) {
            GameResponse response = gameService.deleteGame(gameId);
            return ResponseEntity.status(response.getStatus()).body(response.getMessage());
        }

        if (method.equals("POST")) {
            GameResponse response = gameService.saveGame(gameRequest);
            return ResponseEntity.status(response.getStatus()).body(response.getMessage());
        }

        if (method.equals("PUT")) {
            GameResponse response = gameService.updateGame(gameRequest, gameId);
            return ResponseEntity.status(response.getStatus()).body(response.getMessage());
        }

        return ResponseEntity.badRequest().body("Something went wrong");
    }

    private User authenticateAndAuthorize(String bearer, Role requiredRole) {

        // find user by bearer and check his authority
        User user = userService.findUserByBearer(bearer).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getAuthorities().contains(new SimpleGrantedAuthority(requiredRole.name()))) {
            throw new AccessDeniedException("User does not have the required authority");
        }
        return user;
    }

}
