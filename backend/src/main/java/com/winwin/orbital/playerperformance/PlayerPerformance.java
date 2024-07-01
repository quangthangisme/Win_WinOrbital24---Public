package com.winwin.orbital.playerperformance;

import com.winwin.orbital.player.Player;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PlayerPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    private int gameweek;
    private String season;

    private int minutesPlayed;
    private int goalsScored;
    private int assists;
    private int cleanSheet;
    private int saves;
    private int penaltiesSaved;
    private int penaltiesMissed;
    private int goalsConceded;
    private int yellowCards;
    private int redCards;
    private int ownGoals;

    public PlayerPerformance(Player player,
                             int gameweek,
                             String season,
                             int minutesPlayed,
                             int goalsScored,
                             int assists,
                             int cleanSheet,
                             int saves,
                             int penaltiesSaved,
                             int penaltiesMissed,
                             int goalsConceded,
                             int yellowCards,
                             int redCards,
                             int ownGoals) {
        this.player = player;
        this.gameweek = gameweek;
        this.season = season;
        this.minutesPlayed = minutesPlayed;
        this.goalsScored = goalsScored;
        this.assists = assists;
        this.cleanSheet = cleanSheet;
        this.saves = saves;
        this.penaltiesSaved = penaltiesSaved;
        this.penaltiesMissed = penaltiesMissed;
        this.goalsConceded = goalsConceded;
        this.yellowCards = yellowCards;
        this.redCards = redCards;
        this.ownGoals = ownGoals;
    }

}
