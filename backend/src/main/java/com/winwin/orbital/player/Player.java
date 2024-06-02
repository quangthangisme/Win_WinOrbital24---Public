package com.winwin.orbital.player;

import com.winwin.orbital.team.Team;
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
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String firstName;

    @NotBlank
    private String lastName;

    @ManyToMany(mappedBy = "players")
    @EqualsAndHashCode.Exclude
    private Set<Team> teams = new HashSet<>();

}
