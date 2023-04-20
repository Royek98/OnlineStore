package com.rojek.onlinestore.library;

import com.rojek.onlinestore.game.Game;
import com.rojek.onlinestore.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Integer> {

    Optional<Library> findByUserAndGame(User user, Game game);

    Optional<List<Library>> findAllByUser(User user);

    List<Library> findAllByGame(Game game);
}
