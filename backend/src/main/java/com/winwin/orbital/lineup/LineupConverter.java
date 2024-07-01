package com.winwin.orbital.lineup;

import com.winwin.orbital.exception.PlayerNotFoundException;
import com.winwin.orbital.player.Player;
import com.winwin.orbital.player.PlayerRepository;
import com.winwin.orbital.team.Team;
import com.winwin.orbital.team.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class LineupConverter {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;

    @Autowired
    public LineupConverter(PlayerRepository playerRepository, TeamRepository teamRepository) {
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
    }

    public LineupDto toDto(Lineup lineup) {
        Set<Long> startingPlayerIds = lineup.getStartingPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toSet());

        Map<Integer, Long> substitutes = lineup.getSubstitutes();

        return new LineupDto(
                lineup.getId(),
                lineup.getGameweek(),
                lineup.getSeason(),
                lineup.getSubmittedAt(),
                startingPlayerIds,
                lineup.getCaptain().getId(),
                lineup.getViceCaptain().getId(),
                substitutes,
                lineup.getPowerup(),
                lineup.getTeam().getId()
        );
    }

    public Lineup toEntity(LineupDto lineupDto) {
        Lineup lineup = new Lineup();
        lineup.setStartingPlayers(lineupDto.getStartingPlayerIds().stream()
                .map(playerId -> playerRepository.findById(playerId)
                        .orElseThrow(() -> new PlayerNotFoundException("Player not found: " + playerId)))
                .collect(Collectors.toSet()));
        lineup.setCaptain(playerRepository.findById(lineupDto.getCaptainId())
                .orElseThrow(() -> new PlayerNotFoundException("Captain not found: " + lineupDto.getCaptainId())));

        lineup.setViceCaptain(playerRepository.findById(lineupDto.getViceCaptainId())
                .orElseThrow(() -> new PlayerNotFoundException("Vice captain not found: "
                        + lineupDto.getViceCaptainId())));

        lineup.setSubstitutes(lineupDto.getSubstitutes());
        lineup.setPowerup(lineupDto.getPowerup());
        return lineup;
    }

}
