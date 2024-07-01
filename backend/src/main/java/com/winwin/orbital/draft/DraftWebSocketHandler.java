package com.winwin.orbital.draft;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class DraftWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    public DraftWebSocketHandler(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendDraftComplete(long leagueId) {
        messagingTemplate.convertAndSend("/topic/draft/" + leagueId + "/complete", "Draft Complete");
    }

    public void sendDraftStateUpdate(long leagueId, DraftState draftState) {
        messagingTemplate.convertAndSend("/topic/draft/" + leagueId, draftState);
    }

    public void sendDraftStateUpdateToUser(long leagueId, DraftState draftState, String username) {
        messagingTemplate.convertAndSendToUser(username, "/topic/draft/" + leagueId, draftState);
    }

    public void sendDraftStartSoonToUser(long leagueId, long millisecondsToDraftStart, String username) {
        messagingTemplate.convertAndSendToUser(username, "/topic/draft/" + leagueId + "/start", millisecondsToDraftStart);
    }
}
