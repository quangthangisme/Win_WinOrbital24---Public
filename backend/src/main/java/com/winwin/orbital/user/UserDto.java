package com.winwin.orbital.user;

import com.winwin.orbital.validator.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatches
public class UserDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
    private String matchingPassword;

    @Email
    @NotBlank
    private String email;

}
