package dev.muho.user.dto.command;

import dev.muho.user.dto.api.UserProfileUpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfileUpdateCommand(
    @NotBlank
    @Size(max = 50)
    String name,

    @NotBlank
    @Size(max = 13)
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$")
    String phone
) {
    public static UserProfileUpdateCommand from(UserProfileUpdateRequest request) {
        return new UserProfileUpdateCommand(
                request.getName(),
                request.getPhone()
        );
    }
}
