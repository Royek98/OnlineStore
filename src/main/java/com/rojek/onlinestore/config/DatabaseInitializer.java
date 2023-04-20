package com.rojek.onlinestore.config;

import com.rojek.onlinestore.game.Game;
import com.rojek.onlinestore.game.GameRepository;
import com.rojek.onlinestore.tag.Tag;
import com.rojek.onlinestore.tag.TagRepository;
import com.rojek.onlinestore.user.Role;
import com.rojek.onlinestore.user.User;
import com.rojek.onlinestore.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GameRepository gameRepository;
    private final TagRepository tagRepository;

    @Bean
    InitializingBean init() {
        return () -> {
          if (tagRepository.findAll().isEmpty()) {
            initTags();
          }

          if (userRepository.findAll().isEmpty()) {
              initUsers();
          }

          if (gameRepository.findAll().isEmpty()) {
              initGames();
          }
        };
    }

    public void initTags() {
        tagRepository.save(Tag.builder()
                .name("Horror")
                .build());

        tagRepository.save(Tag.builder()
                .name("Survival")
                .build());

        tagRepository.save(Tag.builder()
                .name("FPS")
                .build());

        tagRepository.save(Tag.builder()
                .name("RTS")
                .build());
    }

    public void initUsers() {
        userRepository.save(User.builder()
                .email("user@user.com")
                .password(passwordEncoder.encode("user"))
                .role(Role.USER)
                .build());

        userRepository.save(User.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("test"))
                .role(Role.USER)
                .build());

        userRepository.save(User.builder()
                .email("admin@admin.com")
                .password(passwordEncoder.encode("admin"))
                .role(Role.ADMIN)
                .build());
    }

    public void initGames() {
        List<Tag> tagList = tagRepository.findAll();

        Game game1 = Game.builder()
                .description("Testing game")
                .price(49.99)
                .title("The Big Test")
                .releaseDate(LocalDate.of(2023, 4, 18))
                .tags(Set.of(tagList.get(0), tagList.get(1)))
                .build();
        gameRepository.save(game1);

        Game game2 = Game.builder()
                .description("Testing game")
                .price(49.99)
                .title("The Big Test2")
                .releaseDate(LocalDate.of(2023, 4, 18))
                .tags(Set.of(tagList.get(0), tagList.get(1)))
                .build();
        gameRepository.save(game2);

        Game game3 = Game.builder()
                .description("Testing2 game")
                .price(39.99)
                .title("Super Game 2000")
                .releaseDate(LocalDate.of(2020, 4, 12))
                .tags(Set.of(tagList.get(3)))
                .build();
        gameRepository.save(game3);

    }

}
