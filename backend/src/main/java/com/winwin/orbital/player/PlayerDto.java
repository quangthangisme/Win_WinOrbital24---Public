package com.winwin.orbital.player;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class PlayerDto {

    private long id;
    private String firstName;
    private String lastName;
    private String position;
    private String clubName;
    private String clubShortName;


    public PlayerDto(Player player) {
        this.id = player.getId();
        this.firstName = player.getFirstName();
        this.lastName = player.getLastName();
        this.position = player.getPosition();
        this.clubName = player.getClub().getName();
        this.clubShortName = player.getClub().getShortName();
    }

}
