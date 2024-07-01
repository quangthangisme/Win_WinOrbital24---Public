package com.winwin.orbital.gameweek;

import org.springframework.stereotype.Component;

@Component
public class GameweekComponent {

    public String getCurrentSeason() {
        return "24/25";
    }

    public int getCurrentGameweek() {
        return 5;
    }
}
