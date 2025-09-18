package dev.muho.user.redis;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@RedisHash(value = "refreshToken")
@RequiredArgsConstructor
public class RefreshToken {

    @Id
    private final String refreshToken;

    private final Long userId;

    @TimeToLive
    private final Long ttl;
}
