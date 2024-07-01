package com.winwin.orbital.draft;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DraftScheduler {

    private final DraftService draftService;

    @Autowired
    public DraftScheduler(DraftService draftService) {
        this.draftService = draftService;
    }

    @Scheduled(fixedRate = 10000)
    public void checkAndStartDrafts() {
        draftService.startScheduledDrafts();
    }
}
