package com.winwin.orbital.team;

import com.winwin.orbital.lineup.PastLineupDto;
import com.winwin.orbital.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class TeamDataDto {
    private long id;
    private String teamName;
    private long managerId;
    private String managerUsername;
    private long leagueId;
    private String leagueName;
    private int points;
    private Set<PlayerDto> currentPlayers;
    private List<PastLineupDto> pastLineups;
}
