package com.winwin.orbital.league;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class JoinLeagueRequestDto {
    private String code;
    private String teamName;
}

