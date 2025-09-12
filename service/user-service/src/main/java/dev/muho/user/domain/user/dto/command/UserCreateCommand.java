package dev.muho.user.domain.user.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateCommand(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상")
        String rawPassword,
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 50, message = "이름은 최대 50자")
        String name,
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "전화번호 형식 오류")
        String phone
) {
}
