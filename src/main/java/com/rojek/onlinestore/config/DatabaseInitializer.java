package com.rojek.onlinestore.config;

import com.rojek.onlinestore.game.Game;
import com.rojek.onlinestore.game.GameRepository;
import com.rojek.onlinestore.user.Role;
import com.rojek.onlinestore.user.User;
import com.rojek.onlinestore.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class DatabaseInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GameRepository gameRepository;
    @Bean
    InitializingBean init() {
        return () -> {
          if (userRepository.findAll().isEmpty()) {
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

          if (gameRepository.findAll().isEmpty()) {
              gameRepository.save(Game.builder()
                      .description("Testing game")
                      .price(49.99)
                      .title("The Big Test")
                      .releaseDate(LocalDate.of(2023, 4, 18))
                      .build());

              gameRepository.save(Game.builder()
                      .description("Testing2 game")
                      .price(39.99)
                      .title("The Big Test2")
                      .releaseDate(LocalDate.of(2020, 4, 12))
                      .build());
          }
        };
    }

}
