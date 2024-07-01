package com.winwin.orbital.league;

import com.winwin.orbital.exception.LeagueNotFoundException;
import com.winwin.orbital.exception.ManagerAlreadyInLeagueException;
import com.winwin.orbital.exception.UnauthorizedException;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.manager.ManagerRepository;
import com.winwin.orbital.player.PlayerRepository;
import com.winwin.orbital.scoringrule.ScoringRuleDto;
import com.winwin.orbital.team.Team;
import com.winwin.orbital.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeagueServiceTest {

    @Mock
    private ManagerRepository managerRepository;

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private LeagueService leagueService;


    @Test
    public void testCreateLeague() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        User user = new User();
        user.setUsername("testUser");
        Manager manager = new Manager();
        manager.setUser(user);
        when(managerRepository.findByUserUsername("testUser")).thenReturn(Optional.of(manager));

        LeagueCreationRequestDto requestDto = new LeagueCreationRequestDto();
        requestDto.setName("Test League");
        requestDto.setPowerUps(Map.of("bboost", 3, "cx3", 2));
        requestDto.setMaxPlayersFromSameClub(2);
        requestDto.setTeamName("Test Team");

        ScoringRuleDto scoringRuleDto = new ScoringRuleDto();
        scoringRuleDto.setFor60Mins(1);
        scoringRuleDto.setForOver60Mins(2);
        scoringRuleDto.setForGkDfGoal(3);
        scoringRuleDto.setForMdGoal(4);
        scoringRuleDto.setForFwGoal(5);
        scoringRuleDto.setForAssist(6);
        scoringRuleDto.setForGkDfCleanSheet(7);
        scoringRuleDto.setForMdCleanSheet(8);
        scoringRuleDto.setFor3GkSaves(9);
        scoringRuleDto.setForPkSaved(10);
        scoringRuleDto.setForPkMissed(-1);
        scoringRuleDto.setFor2GoalsConceded(-2);
        scoringRuleDto.setForYellowCard(-3);
        scoringRuleDto.setForRedCard(-4);
        scoringRuleDto.setForOwnGoal(-5);

        requestDto.setScoringRule(scoringRuleDto);

        when(leagueRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        League createdLeague = leagueService.createLeague(requestDto, userDetails);

        assertNotNull(createdLeague);
        assertEquals("Test League", createdLeague.getName());
        assertEquals(2, createdLeague.getPowerUps().size());
        assertEquals("Test Team", createdLeague.getTeams().iterator().next().getName());
        assertEquals(manager, createdLeague.getAdmin());

        assertNotNull(createdLeague.getScoringRule());
        assertEquals(1, createdLeague.getScoringRule().getFor60Mins());
        assertEquals(2, createdLeague.getScoringRule().getForOver60Mins());
        assertEquals(3, createdLeague.getScoringRule().getForGkDfGoal());
        assertEquals(4, createdLeague.getScoringRule().getForMdGoal());
        assertEquals(5, createdLeague.getScoringRule().getForFwGoal());
        assertEquals(6, createdLeague.getScoringRule().getForAssist());
        assertEquals(7, createdLeague.getScoringRule().getForGkDfCleanSheet());
        assertEquals(8, createdLeague.getScoringRule().getForMdCleanSheet());
        assertEquals(9, createdLeague.getScoringRule().getFor3GkSaves());
        assertEquals(10, createdLeague.getScoringRule().getForPkSaved());
        assertEquals(-1, createdLeague.getScoringRule().getForPkMissed());
        assertEquals(-2, createdLeague.getScoringRule().getFor2GoalsConceded());
        assertEquals(-3, createdLeague.getScoringRule().getForYellowCard());
        assertEquals(-4, createdLeague.getScoringRule().getForRedCard());
        assertEquals(-5, createdLeague.getScoringRule().getForOwnGoal());
    }

    @Test
    public void testCreateLeague_WithBlankTeamName() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        User user = new User();
        user.setUsername("testUser");
        Manager manager = new Manager();
        manager.setUser(user);
        when(managerRepository.findByUserUsername("testUser")).thenReturn(Optional.of(manager));

        LeagueCreationRequestDto requestDto = new LeagueCreationRequestDto();
        requestDto.setName("Test League");
        requestDto.setPowerUps(Map.of("bboost", 3, "cx3", 2));
        requestDto.setMaxPlayersFromSameClub(2);
        requestDto.setTeamName("");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> leagueService.createLeague(requestDto, userDetails));

        assertEquals("Team name cannot be blank.", exception.getMessage());
    }

    @Test
    public void testJoinLeague_LeagueNotFound() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        User user = new User();
        user.setUsername("testUser");
        Manager manager = new Manager();
        manager.setUser(user);
        when(managerRepository.findByUserUsername("testUser")).thenReturn(Optional.of(manager));

        when(leagueRepository.findByCode("testCode")).thenReturn(Optional.empty());

        LeagueNotFoundException exception = assertThrows(LeagueNotFoundException.class,
                () -> leagueService.joinLeague("testCode", "Test Team", userDetails));

        assertEquals("League not found.", exception.getMessage());
    }

    @Test
    public void testJoinLeague_ManagerAlreadyInLeague() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        User user = new User();
        user.setUsername("testUser");
        Manager manager = new Manager();
        manager.setUser(user);
        when(managerRepository.findByUserUsername("testUser")).thenReturn(Optional.of(manager));

        League league = new League();
        league.setCode("testCode");
        league.setStatus("created");

        Team team = new Team();
        team.setName("Existing Team");
        team.setLeague(league);
        team.setManager(manager);
        league.getTeams().add(team);
        manager.getTeams().add(team);

        when(leagueRepository.findByCode("testCode")).thenReturn(Optional.of(league));

        ManagerAlreadyInLeagueException exception = assertThrows(ManagerAlreadyInLeagueException.class,
                () -> leagueService.joinLeague("testCode", "Test Team", userDetails));

        assertEquals("Manager is already part of this league.", exception.getMessage());
    }

    @Test
    public void testSubmitDraftSettings_Unauthorized() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        User user = new User();
        user.setUsername("testUser");
        Manager manager = new Manager();
        manager.setUser(user);
        when(managerRepository.findByUserUsername("testUser")).thenReturn(Optional.of(manager));

        User difUser = new User();
        user.setUsername("testDifUser");
        Manager difManager = new Manager();
        manager.setUser(difUser);

        League league = new League();
        league.setId(1L);
        league.setAdmin(difManager);
        league.setStatus("created");
        when(leagueRepository.findById(1L)).thenReturn(Optional.of(league));

        DraftSettingsDto draftSettingsDto = new DraftSettingsDto();
        draftSettingsDto.setDraftStartTime(LocalDateTime.now().plusDays(1));
        draftSettingsDto.setTurnDuration(10);

        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> leagueService.submitDraftSettings(1L, draftSettingsDto, userDetails));

        assertEquals("You are not authorized to submit draft settings for this league.", exception.getMessage());
    }
}
