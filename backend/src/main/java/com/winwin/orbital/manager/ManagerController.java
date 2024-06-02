package com.winwin.orbital.manager;

import com.winwin.orbital.exception.UserNotFoundException;
import com.winwin.orbital.league.LeagueDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final ManagerService managerService;

    @Autowired
    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("/myleagues")
    public ResponseEntity<?> getCurrentUserLeagues(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<LeagueDto> leagues = managerService.getCurrentUserLeagues(userDetails);
            return ResponseEntity.ok(leagues);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

}
