package dev.muho.auth;

import dev.muho.auth.domain.auth.FakeAuthRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakeDBConfiguration {

    @Bean
    public FakeAuthRepository authRepository() {
        return new FakeAuthRepository();
    }
}
