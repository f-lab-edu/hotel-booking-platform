package dev.muho.user.dto.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailExistResponse {
    private String email;
    private boolean exists;

    public static EmailExistResponse of(String email, boolean exists) {
        return new EmailExistResponse(email, exists);
    }
}

