package org.kivislime.weatherapp.session.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpFormDto {
    @Pattern(
            regexp = "^[A-Za-z0-9]{3,20}$",
            message = "Login must be 3–20 characters, only Latin letters and digits"
    )
    private String login;

    @NotBlank(message = "The password must not be empty")
    @Size(min = 4, max = 20, message = "The password must contain from {min} to {max} characters")
    private String password;

    @NotBlank(message = "The password confirmation cannot be empty")
    @Size(min = 4, max = 20, message = "The password confirmation must contain from {min} to {max} characters")
    private String confirmPassword;
}
