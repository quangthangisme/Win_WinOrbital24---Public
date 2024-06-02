package com.winwin.orbital.league;

import com.winwin.orbital.exception.LeagueNotFoundException;
import com.winwin.orbital.exception.ManagerAlreadyInLeagueException;
import com.winwin.orbital.exception.UserNotFoundException;
import com.winwin.orbital.team.TeamDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/league")
public class LeagueController {

    private final LeagueService leagueService;

    @Autowired
    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping("/{league_id}")
    public ResponseEntity<?> getTeamsInLeague(@PathVariable("league_id") Long leagueId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<TeamDto> managerIdsInLeague =
                    leagueService.getTeamsInLeague(leagueId, userDetails);
            return ResponseEntity.ok(managerIdsInLeague);
        } catch (UserNotFoundException | LeagueNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLeague(@RequestParam String name, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            League league = leagueService.createLeague(name, userDetails);
            return ResponseEntity.ok(new LeagueDto(league));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinLeague(@RequestParam String code, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            League league = leagueService.joinLeague(code, userDetails);
            return ResponseEntity.ok(new LeagueDto(league));
        } catch (UserNotFoundException | LeagueNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (ManagerAlreadyInLeagueException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

}

