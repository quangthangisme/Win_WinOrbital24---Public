package com.winwin.orbital.league;

import com.winwin.orbital.scoringrule.ScoringRuleDto;
import com.winwin.orbital.team.TeamDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/league")
public class LeagueController {

    private final LeagueService leagueService;

    @Autowired
    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @GetMapping("/name/{league_id}")
    public ResponseEntity<?> getLeagueName(@PathVariable("league_id") Long leagueId,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        String leagueName = leagueService.getLeagueName(leagueId, userDetails);
        return ResponseEntity.ok(leagueName);
    }

    @GetMapping("/scoring_rule/{league_id}")
    public ResponseEntity<?> getLeagueScoringRule(@PathVariable("league_id") Long leagueId,
                                           @AuthenticationPrincipal UserDetails userDetails) {

        ScoringRuleDto scoringRuleDto = leagueService.getScoringRule(leagueId, userDetails);
        return ResponseEntity.ok(scoringRuleDto);
    }

    @GetMapping("/powerups/{league_id}")
    public ResponseEntity<?> getLeaguePowerups(@PathVariable("league_id") Long leagueId,
                                                  @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Integer> powerUps = leagueService.getPowerUps(leagueId, userDetails);
        return ResponseEntity.ok(powerUps);
    }

    @GetMapping("/draft_info/{league_id}")
    public ResponseEntity<?> getDraftInfo(@PathVariable("league_id") Long leagueId,
                                               @AuthenticationPrincipal UserDetails userDetails) {

        DraftInfoDto draftInfo = leagueService.getDraftInfo(leagueId, userDetails);
        return ResponseEntity.ok(draftInfo);
    }

    @GetMapping("/max_players_same_club/{league_id}")
    public ResponseEntity<?> getMaxPlayersSameClub(@PathVariable("league_id") Long leagueId,
                                               @AuthenticationPrincipal UserDetails userDetails) {

        long maxPlayersSameClub = leagueService.getMaxPlayersSameClub(leagueId, userDetails);
        return ResponseEntity.ok(maxPlayersSameClub);
    }

    @GetMapping("/teams/{league_id}")
    public ResponseEntity<?> getTeamsInLeague(@PathVariable("league_id") Long leagueId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {

        List<TeamDto> managerIdsInLeague = leagueService.getTeamsInLeague(leagueId, userDetails);
        return ResponseEntity.ok(managerIdsInLeague);
    }

    @GetMapping("/status/{league_id}")
    public ResponseEntity<?> getStatus(@PathVariable("league_id") Long leagueId,
                                              @AuthenticationPrincipal UserDetails userDetails) {

        LeagueStatusDto managerIdsInLeague = leagueService.getStatus(leagueId, userDetails);
        return ResponseEntity.ok(managerIdsInLeague);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLeague(
            @RequestBody LeagueCreationRequestDto leagueCreationRequestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        League league = leagueService.createLeague(leagueCreationRequestDto, userDetails);
        return ResponseEntity.ok(new LeagueDto(league));
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinLeague(@RequestBody JoinLeagueRequestDto request, @AuthenticationPrincipal UserDetails userDetails) {
        League league = leagueService.joinLeague(request.getCode(), request.getTeamName(), userDetails);
        return ResponseEntity.ok(new LeagueDto(league));
    }

    @PostMapping("/submit_draft_settings/{league_id}")
    public ResponseEntity<?> submitDraftSettings(@PathVariable("league_id") Long leagueId,
                                                 @RequestBody DraftSettingsDto draftSettings,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        leagueService.submitDraftSettings(leagueId, draftSettings, userDetails);
        return ResponseEntity.ok("Draft settings submitted successfully.");
    }

}

