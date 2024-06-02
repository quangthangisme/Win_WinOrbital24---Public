package com.winwin.orbital.team;

import com.winwin.orbital.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;
import java.util.stream.Collectors;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class TeamDto {

    private String teamName;
    private long managerId;
    private String managerUsername;
    private long leagueId;
    private String leagueName;
    private Set<PlayerDto> players;
    private int points;

    public TeamDto(Team team) {
        this.teamName = team.getName();
        this.managerId = team.getManager().getId();
        this.managerUsername = team.getManager().getUsername();
        this.leagueId = team.getLeague().getId();
        this.leagueName = team.getLeague().getName();
        this.players = team.getPlayers().stream().map(PlayerDto::new).collect(Collectors.toSet());
        this.points = team.getPoints();
    }

}
