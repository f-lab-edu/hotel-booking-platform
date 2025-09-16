package dev.muho.user.dto.api;

import dev.muho.user.dto.command.UserInfoResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private String role;   // enum -> String 노출
    private String status; // enum -> String 노출

    public static UserResponse from(UserInfoResult userInfoResult) {
        if (userInfoResult == null) {
            return null;
        }
        return new UserResponse(
                userInfoResult.id(),
                userInfoResult.email(),
                userInfoResult.name(),
                userInfoResult.phone(),
                userInfoResult.role().name(),
                userInfoResult.status().name()
        );
    }
}
