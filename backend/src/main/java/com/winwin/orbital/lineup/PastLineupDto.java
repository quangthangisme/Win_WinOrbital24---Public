package com.winwin.orbital.lineup;

import com.winwin.orbital.player.PlayerDto;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class PastLineupDto {
    private long id;
    private long teamId;
    private int gameweek;
    private String season;
    private Set<PlayerDto> startingPlayers;
    private long captainId;
    private long viceCaptainId;
    private Map<Integer, PlayerDto> substitutes;
    private String powerup;
    private long points;
    private Map<Long, Integer> playerPoints;
    private Map<Long, Boolean> playerToPlayedOrNot;
}
