package com.winwin.orbital.draft;

import com.winwin.orbital.manager.ManagerDto;
import com.winwin.orbital.player.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@ToString(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class DraftState {
    private final ManagerDto currentManager;
    private final ManagerDto nextManager;
    private final Map<ManagerDto, List<PlayerDto>> selectedPlayers;
    private final List<PlayerDto> draftPool;
    private final String lastPickMessage;
    @ToString.Include
    private final long remainingTime;
}
