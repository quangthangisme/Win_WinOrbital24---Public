package com.winwin.orbital.draft;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@Controller
public class DraftController {

    private final DraftService draftService;

    @Autowired
    public DraftController(DraftService draftService) {
        this.draftService = draftService;
    }

    @MessageMapping("/pickPlayer")
    @SendTo("/topic/draft")
    public void pickPlayer(@Payload PlayerPickRequest playerPickRequest, Principal user) {
        UserDetails userDetails = (UserDetails) ((Authentication) user).getPrincipal();
        draftService.pickPlayer(playerPickRequest.getLeagueId(), playerPickRequest.getPlayerId(), userDetails);
    }

    @MessageMapping("/getDraftState")
    @SendTo("/topic/draft")
    public void subscribeToDraft(@Payload long leagueId, Principal user) {
        UserDetails userDetails = (UserDetails) ((Authentication) user).getPrincipal();
        draftService.sendDraftState(leagueId, userDetails);
    }
}
