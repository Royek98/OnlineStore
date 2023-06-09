package com.rojek.onlinestore.library;

import com.rojek.onlinestore.game.Game;
import com.rojek.onlinestore.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryRepository libraryRepository;

    @Transactional
    public void saveToLibrary(Game game, User user) {

        // check if user already own the game
        if (libraryRepository.findByUserAndGame(user, game).isPresent()) {
            throw new RuntimeException("Already have this game");
        }

        libraryRepository.save(Library.builder()
                .game(game)
                .user(user)
                .date(LocalDate.now())
                .keyCode(UUID.randomUUID())
                .build());
    }

    public List<Library> getAllGamesByUser(User user) {
        return libraryRepository.findAllByUser(user).get();
    }

    public Library getDetails(Game game, User user) {

        // check if user don't own the game
        Optional<Library> details = libraryRepository.findByUserAndGame(user, game);
        if (!details.isPresent()) {
            throw new RuntimeException("You dont own this game");
        }

        return details.get();
    }

    public List<Library> findByGame(Game game) {
        return libraryRepository.findAllByGame(game);
    }

    @Transactional
    public void removeLibrary(Library library) {
        libraryRepository.delete(library);
    }
}
