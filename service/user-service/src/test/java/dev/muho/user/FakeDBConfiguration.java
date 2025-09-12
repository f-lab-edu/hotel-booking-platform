package dev.muho.user;

import dev.muho.user.domain.user.FakeUserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakeDBConfiguration {

    @Bean
    public FakeUserRepository userRepository() {
        return new FakeUserRepository();
    }
}
