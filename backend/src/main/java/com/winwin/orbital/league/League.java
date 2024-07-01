package com.winwin.orbital.league;

import com.winwin.orbital.manager.Manager;
import com.winwin.orbital.scoringrule.ScoringRule;
import com.winwin.orbital.team.Team;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class League {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String name;

    @NotBlank
    @Column(unique = true)
    private String code;

    @NotBlank
    @EqualsAndHashCode.Exclude
    private String status; // created, waiting for draft, drafting, in season, or post-season

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    @EqualsAndHashCode.Exclude
    private Manager admin;

    @EqualsAndHashCode.Exclude
    private LocalDateTime draftStartTime;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private Set<Team> teams = new HashSet<>();

    @OneToOne(mappedBy = "league", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    private ScoringRule scoringRule;

    @ElementCollection
    @MapKeyColumn(name = "powerup")
    @Column(name = "count")
    @EqualsAndHashCode.Exclude
    private Map<String, Integer> powerUps;

    @EqualsAndHashCode.Exclude
    private long maxNumberOfPlayersFromAClub;

    @EqualsAndHashCode.Exclude
    private long draftTurnDurationMilliseconds;

    public League(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static String generateRandomCode() {
        int length = 8;
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder codeBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            codeBuilder.append(characters.charAt(random.nextInt(characters.length())));
        }
        return codeBuilder.toString();
    }

}
