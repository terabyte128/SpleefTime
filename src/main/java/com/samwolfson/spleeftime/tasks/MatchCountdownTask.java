package com.samwolfson.spleeftime.tasks;

import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.config.Match;

import java.util.Map;

public class MatchCountdownTask implements Runnable {
    private static int task = -1;

    @Override
    public void run() {
        Map<String, Match> matches = SpleefTime.getInstance().getConfigData().getMatches();

        int countingMatches = 0;

        for (Match match : matches.values()) {
            if (match.isCountingDown()) {
                match.updateCountdown();
                countingMatches++;
            }
        }

        if (countingMatches == 0) {
            SpleefTime.getInstance().getServer().getScheduler().cancelTask(task);
            task = -1;
        }
    }

    public static void scheduleCountdownTask() {
        if (task == -1) {
            task = SpleefTime.getInstance().getServer().getScheduler().scheduleSyncRepeatingTask(SpleefTime.getInstance(), new MatchCountdownTask(), 0L, 30L);
        }
    }


}
