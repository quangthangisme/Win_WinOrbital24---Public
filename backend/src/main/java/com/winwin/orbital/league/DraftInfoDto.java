package com.winwin.orbital.league;

import com.winwin.orbital.manager.ManagerDto;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class DraftInfoDto {
    private String leagueStatus;
    private LocalDateTime draftStartTime;
    private ManagerDto leagueAdmin;
}
