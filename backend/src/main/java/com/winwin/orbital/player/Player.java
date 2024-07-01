package com.winwin.orbital.player;

import com.winwin.orbital.club.Club;
import com.winwin.orbital.playerperformance.PlayerPerformance;
import com.winwin.orbital.team.Team;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String firstName;

    private String lastName;

    private String position;

    @EqualsAndHashCode.Exclude
    private boolean isAvailable;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Set<PlayerPerformance> performanceHistory;

    @ManyToMany(mappedBy = "currentPlayers", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Set<Team> teams;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @EqualsAndHashCode.Exclude
    private Club club;

    public Player(String firstName, String lastName, String position) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
    }
}
