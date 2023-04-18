package com.rojek.onlinestore.library;

import com.rojek.onlinestore.game.Game;
import com.rojek.onlinestore.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Integer> {
    @Query("SELECT L FROM Library L WHERE L.user = :user AND L.game = :game")
    Optional<Library> findByUserAndGame(@Param("user") User user, @Param("game") Game game);
    Optional<List<Library>> findAllByUser(User user);

    List<Library> findAllByGame(Game game);
}
