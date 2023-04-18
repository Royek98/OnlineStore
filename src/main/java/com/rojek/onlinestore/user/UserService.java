package com.rojek.onlinestore.user;

import com.rojek.onlinestore.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public Optional<User> findUserByBearer(String bearer) {
        String userName = jwtService.extractUsernameFromBearer(bearer);
        return userRepository.findByEmail(userName);
    }

}
