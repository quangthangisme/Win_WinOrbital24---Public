package com.winwin.orbital.team;

import com.winwin.orbital.league.League;
import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.player.Player;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Manager manager;

    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;

    @ManyToMany
    @JoinTable(
            name = "team_player",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    @EqualsAndHashCode.Exclude
    private Set<Player> players = new HashSet<>();

    private int points = 0;

    public Team(Manager manager, League league) {
        this.name = manager.getUser().getUsername() + " F.C.";
        this.manager = manager;
        this.league = league;
    }

}
