package com.winwin.orbital.lineup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lineup")
public class LineupController {

    private final LineupService lineupService;
    private final LineupConverter lineupConverter;

    @Autowired
    public LineupController(LineupService lineupService, LineupConverter lineupConverter) {
        this.lineupService = lineupService;
        this.lineupConverter = lineupConverter;
    }

    @GetMapping("/history")
    public ResponseEntity<?> getLineupHistory(@RequestParam("league_id") Long leagueId,
                                              @RequestParam("gameweek") int gameweek,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        LineupDto lineup = lineupService.getMyLineupHistory(leagueId, gameweek, userDetails);
        return ResponseEntity.ok(lineup);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentLineup(@RequestParam("league_id") Long leagueId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        LineupDto lineup = lineupService.getCurrentLineup(leagueId, userDetails);
        return ResponseEntity.ok(lineup);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitLineup(@RequestParam("league_id") Long leagueId,
                                          @RequestBody LineupDto lineupDto,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        Lineup lineup = lineupService.submitLineup(leagueId, lineupDto, userDetails);
        return ResponseEntity.ok(lineupConverter.toDto(lineup));
    }

}
