package com.winwin.orbital.authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
