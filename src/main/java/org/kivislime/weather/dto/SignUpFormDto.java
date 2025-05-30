package org.kivislime.weather.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpFormDto {
    @NotBlank(message = "The login must not be empty")
    @Size(min = 3, max = 20, message = "The login must contain from {min} to {max} characters")
    private String login;

    @NotBlank(message = "The password must not be empty")
    @Size(min = 4, max = 20, message = "The password must contain from {min} to {max} characters")
    private String password;

    @NotBlank(message = "The password confirmation cannot be empty")
    @Size(min = 4, max = 20, message = "The password confirmation must contain from {min} to {max} characters")
    private String confirmPassword;
}
