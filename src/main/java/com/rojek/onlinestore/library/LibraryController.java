package com.rojek.onlinestore.library;

import com.rojek.onlinestore.game.Game;
import com.rojek.onlinestore.game.GameService;
import com.rojek.onlinestore.user.User;
import com.rojek.onlinestore.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final UserService userService;
    private final LibraryService libraryService;
    private final GameService gameService;

    @GetMapping()
    public ResponseEntity<?> getAll(
            @RequestHeader(name = "Authorization") String bearer
    ) {

        // find user by bearer
        User user = userService.findUserByBearer(bearer)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // get user's library
        var list = libraryService.getAllGamesByUser(user);
        if (list == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Library is empty");
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping("/details")
    public ResponseEntity<?> getDetails(
            @RequestHeader(name = "Authorization") String bearer,
            @RequestParam("gameId") int gameId
    ) {

        // find user by bearer
        User user = userService.findUserByBearer(bearer)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // find the game in db
        Game game = gameService.findGameById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // get details of game in library
        try {
            return ResponseEntity.ok(libraryService.getDetails(game, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
