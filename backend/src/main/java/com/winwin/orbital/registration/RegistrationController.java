package com.winwin.orbital.registration;

import com.winwin.orbital.exception.RegistrationException;
import com.winwin.orbital.exception.UserAlreadyExistException;
import com.winwin.orbital.user.User;
import com.winwin.orbital.user.UserDto;
import com.winwin.orbital.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api")
public class RegistrationController {

    @Autowired
    private UserService userService;

    @PostMapping("/registration")
    public ResponseEntity<?> registerUserAccount(@Valid @RequestBody UserDto userDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        try {
            User registered = userService.registerNewUserAccount(userDto);
            return ResponseEntity.ok().build();
        } catch (UserAlreadyExistException uaeEx) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(uaeEx.getMessage());
        } catch (RegistrationException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

}
