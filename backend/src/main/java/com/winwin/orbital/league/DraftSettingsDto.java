package com.winwin.orbital.league;

import lombok.*;

import java.time.LocalDateTime;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DraftSettingsDto {
    private LocalDateTime draftStartTime;
    private long turnDuration;
}
