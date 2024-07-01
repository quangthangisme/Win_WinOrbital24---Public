package com.winwin.orbital.playerperformance;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class PlayerPerformanceDto {

    private long id;
    private long playerId;
    private int gameweek;
    private String season;

    public PlayerPerformanceDto(PlayerPerformance playerPerformance) {
        this.id = playerPerformance.getId();
        this.playerId = playerPerformance.getPlayer().getId();
        this.gameweek = playerPerformance.getGameweek();
        this.season = playerPerformance.getSeason();
    }

}
