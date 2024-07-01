package com.winwin.orbital.team;

import com.winwin.orbital.player.PlayerDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/current_players/curr_manager")
    public ResponseEntity<?> getCurrentPlayers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("league_id") Long leagueId) {
        List<PlayerDto> playerDtos = teamService.getCurrentPlayers(userDetails, leagueId);
        return ResponseEntity.ok(playerDtos);
    }

    @GetMapping("/points")
    public ResponseEntity<?> getPoints(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("team_id") long teamId) {
        return ResponseEntity.ok(teamService.getPoints(teamId, userDetails));
    }

    @GetMapping("/remaining_powerups/curr_manager")
    public ResponseEntity<?> getRemainingPowerups(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("league_id") Long leagueId) {
        Map<String, Integer> remainingPowerups = teamService.getCurrentTeamRemainingPowerups(userDetails, leagueId);
        return ResponseEntity.ok(remainingPowerups);
    }

    @GetMapping("/team_data")
    public ResponseEntity<?> getAllTeamData(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("league_id") Long leagueId) {
        return ResponseEntity.ok(teamService.getAllTeamsData(leagueId, userDetails));
    }
}
