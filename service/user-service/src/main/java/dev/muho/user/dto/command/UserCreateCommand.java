package dev.muho.user.dto.command;

import dev.muho.user.dto.api.UserSignupRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateCommand(
    @NotBlank
    @Size(max = 100)
    @Email
    String email,

    @NotBlank
    @Size(min = 8)
    String rawPassword,

    @NotBlank
    @Size(max = 50)
    String name,

    @NotBlank
    @Size(max = 13)
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$")
    String phone
) {
    public static UserCreateCommand from(UserSignupRequest request) {
        return new UserCreateCommand(
                request.getEmail(),
                request.getPassword(),
                request.getName(),
                request.getPhone()
        );
    }
}
