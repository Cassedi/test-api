package org.example.testapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Field 'email' is required")
    @Email(message = "Field 'email' must be a valid email address")
    private String email;

    @NotBlank(message = "Field 'password' is required")
    private String password;
}
