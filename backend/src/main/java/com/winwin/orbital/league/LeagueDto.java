package com.winwin.orbital.league;

import lombok.*;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class LeagueDto {

    private long id;
    private String name;
    private String code;

    public LeagueDto(League league) {
        this.id = league.getId();
        this.name = league.getName();
        this.code = league.getCode();
    }

}