package com.winwin.orbital.draft;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DraftSessionManager {

    private final Map<Long, DraftSession> draftSessions = new HashMap<>();

    public DraftSession getDraftSession(long leagueId) {
        return draftSessions.get(leagueId);
    }

    public void addDraftSession(DraftSession draftSession) {
        draftSessions.put(draftSession.getLeague().getId(), draftSession);
    }

    public void removeDraftSession(long leagueId) {
        draftSessions.remove(leagueId);
    }

}