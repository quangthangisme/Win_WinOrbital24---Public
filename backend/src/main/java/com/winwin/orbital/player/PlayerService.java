package com.winwin.orbital.player;

import com.winwin.orbital.exception.PlayerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public PlayerDto getPlayerById(long playerId) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (playerOpt.isPresent()) {
            return new PlayerDto(playerOpt.get());
        } else {
            throw new PlayerNotFoundException("Player with id " + playerId + " not found");
        }
    }

}
