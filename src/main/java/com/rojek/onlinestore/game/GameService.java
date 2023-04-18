package com.rojek.onlinestore.game;

import com.rojek.onlinestore.library.Library;
import com.rojek.onlinestore.library.LibraryService;
import com.rojek.onlinestore.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final LibraryService libraryService;

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> findGameById(int id) {
        return gameRepository.findGameById(id);
    }

    public void buyGame(Game game, User user) {
        libraryService.saveToLibrary(game, user);
    }

    @Transactional
    public void removeGameFromDB(Game game) {
        List<Library> libraries = libraryService.findByGame(game);
        for (Library library : libraries) {
            libraryService.removeLibrary(library);
        }

        gameRepository.delete(game);
    }

    @Transactional
    public void saveGameToDb(Game game) {
        gameRepository.save(game);
    }

    public GameResponse deleteGame(Integer gameId) {
        if (gameId == null) {
            return GameResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("gameId param is empty")
                    .build();
        }

        Optional<Game> gameDb = findGameById(gameId);
        if (!gameDb.isPresent()) {
            return GameResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Game not found")
                    .build();
        }
        removeGameFromDB(gameDb.get());

        return GameResponse.builder()
                .status(HttpStatus.OK)
                .message("Game removed")
                .build();
    }

    public GameResponse saveGame(Game game) {
        if (game == null) {
            return GameResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Request body is empty")
                    .build();
        }

        // todo check if game exists in db
        if (gameRepository.findGameByTitle(game.getTitle()).isPresent()) {
            return GameResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Game already exists")
                    .build();
        }

        saveGameToDb(game);
        return GameResponse.builder()
                .status(HttpStatus.OK)
                .message("Game added")
                .build();
    }

    public GameResponse updateGame(Game gameRequest, Integer gameId) {
        if (gameId == null) {
            return GameResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("gameId param is empty")
                    .build();
        }

        if (gameRequest == null) {
            return GameResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Request body is empty")
                    .build();
        }

        Optional<Game> game = findGameById(gameId);
        if (!game.isPresent()) {
            return GameResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Game not found")
                    .build();
        }

        Game gameDb = game.get();
        // toDo better validation
        if (gameRequest.getDescription() != null) {
            gameDb.setDescription(gameRequest.getDescription());
        }

        if (gameDb.getPrice() != gameRequest.getPrice() && gameRequest.getPrice() > 0) {
            gameDb.setPrice(gameRequest.getPrice());
        }

        if (gameRequest.getTitle() != null) {
            gameDb.setTitle(gameRequest.getTitle());
        }

        if (gameDb.getReleaseDate() != gameRequest.getReleaseDate() && gameRequest.getReleaseDate() != null) {
            gameDb.setReleaseDate(gameRequest.getReleaseDate());
        }

        saveGameToDb(gameDb);

        return GameResponse.builder()
                .status(HttpStatus.OK)
                .message("Game updated")
                .build();
    }

}
