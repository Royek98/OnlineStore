package com.rojek.onlinestore.game;

import com.rojek.onlinestore.library.Library;
import com.rojek.onlinestore.library.LibraryService;
import com.rojek.onlinestore.tag.Tag;
import com.rojek.onlinestore.tag.TagService;
import com.rojek.onlinestore.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final LibraryService libraryService;
    private final TagService tagService;

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

    public GameResponse saveGame(Game gameRequest) {
        if (gameRequest == null) {
            return GameResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Request body is empty")
                    .build();
        }

        if (gameRequest.getTags().isEmpty()) {
            return GameResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Tags are missing")
                    .build();
        }

        if (gameRepository.findGameByTitle(gameRequest.getTitle()).isPresent()) {
            return GameResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Game already exists")
                    .build();
        }

        // check if tags exist in db before saving a new game to db
        try {
            tagService.findByIdInList(gameRequest.getTags());
        } catch (RuntimeException e) {
            return GameResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message(e.getMessage())
                    .build();
        }

        saveGameToDb(gameRequest);
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

        if (gameRequest.getTags().isEmpty()) {
            return GameResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Tags are missing")
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

        // check if tags exist in db before updating a game details in db
        try {
            tagService.findByIdInList(gameRequest.getTags());
        } catch (RuntimeException e) {
            return GameResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message(e.getMessage())
                    .build();
        }
        gameDb.setTags(gameRequest.getTags());


        saveGameToDb(gameDb);

        return GameResponse.builder()
                .status(HttpStatus.OK)
                .message("Game updated")
                .build();
    }

    public List<Game> searchGames(String title, List<Integer> tags) {
        List<Game> games = getAllGames();

        if (title != null) {
            games = games.stream()
                    .filter(game -> {
                                String gameTitle = normalizeString(game.getTitle());
                                String searchTitle = normalizeString(title);
                                return gameTitle.contains(searchTitle);
                            }
                    ).toList();
        }

        if (tags != null && !tags.isEmpty()) {
            games = games.stream()
                    .filter(game -> new HashSet<>(game.getTags().stream()
                            .map(Tag::getId).collect(Collectors.toList())).containsAll(tags)
            ).toList();
        }

        return games;
    }

    private String normalizeString(String before) {
        return before.toLowerCase().replaceAll(" ", "");
    }
}
