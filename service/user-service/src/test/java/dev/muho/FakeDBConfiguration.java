package dev.muho;

import dev.muho.user.domain.auth.FakeAuthRepository;
import dev.muho.user.domain.user.FakeUserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakeDBConfiguration {

    @Bean
    public FakeUserRepository userRepository() {
        return new FakeUserRepository();
    }

    @Bean
    public FakeAuthRepository authRepository() {
        return new FakeAuthRepository();
    }
}
