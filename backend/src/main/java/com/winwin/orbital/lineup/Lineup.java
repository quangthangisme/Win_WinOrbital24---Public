package com.winwin.orbital.lineup;

import com.winwin.orbital.player.Player;
import com.winwin.orbital.team.Team;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.*;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Lineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int gameweek;

    private String season;

    private LocalDateTime submittedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "lineup_starting_player",
            joinColumns = @JoinColumn(name = "lineup_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    @EqualsAndHashCode.Exclude
    private Set<Player> startingPlayers = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id")
    @EqualsAndHashCode.Exclude
    private Player captain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vice_captain_id")
    @EqualsAndHashCode.Exclude
    private Player viceCaptain;

    @ElementCollection
    @CollectionTable(name = "lineup_substitutes", joinColumns = @JoinColumn(name = "lineup_id"))
    @MapKeyColumn(name = "substitution_order")
    @Column(name = "player_id")
    @EqualsAndHashCode.Exclude
    private Map<Integer, Long> substitutes;

    @EqualsAndHashCode.Exclude
    private String powerup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Lineup(int gameweek,
                  String season,
                  LocalDateTime submittedAt,
                  Set<Player> startingPlayers,
                  Player captain,
                  Player viceCaptain,
                  Map<Integer, Long> substitutes,
                  String powerup,
                  Team team) {
        this.gameweek = gameweek;
        this.season = season;
        this.submittedAt = submittedAt;
        this.startingPlayers = startingPlayers;
        this.captain = captain;
        this.viceCaptain = viceCaptain;
        this.substitutes = substitutes;
        this.powerup = powerup;
        this.team = team;
    }
}
