package com.rojek.onlinestore.game;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findGameById(int id);
    Optional<Game> findGameByTitle(String title);
}
