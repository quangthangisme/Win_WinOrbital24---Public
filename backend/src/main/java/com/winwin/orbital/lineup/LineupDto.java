package com.winwin.orbital.lineup;

import com.winwin.orbital.player.PlayerDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LineupDto {

    private long id;
    private int gameweek;
    private String season;
    private LocalDateTime submittedAt;
    private Set<Long> startingPlayerIds;
    private long captainId;
    private long viceCaptainId;
    private Map<Integer, Long> substitutes;
    private String powerup;
    private long teamId;

}
