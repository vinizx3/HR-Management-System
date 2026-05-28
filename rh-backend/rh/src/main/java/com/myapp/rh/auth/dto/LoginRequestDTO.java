package com.myapp.rh.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
