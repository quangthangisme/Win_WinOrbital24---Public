package com.winwin.orbital.registration;

import com.winwin.orbital.user.UserDto;
import com.winwin.orbital.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> registerUserAccount(@Valid @RequestBody UserDto userDto) {
        userService.registerNewUserAccount(userDto);
        return ResponseEntity.ok().build();
    }

}
