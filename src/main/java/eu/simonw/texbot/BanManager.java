package eu.simonw.texbot;

import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BanManager {
    private final Map<Long, Long> bannedUserIds;

    public BanManager() {
        bannedUserIds = Map.of();

    }
    public void ban(Long id, Long duration_in_sec) {
        bannedUserIds.put(id, Instant.now().plus(duration_in_sec, ChronoUnit.SECONDS).toEpochMilli());

    }
    public void isBanned(long id) {
        Instant banned_until = Instant.ofEpochMilli(bannedUserIds.get(id));
        if (banned_until.isAfter(Instant.now())) {
            bannedUserIds.remove(id);
        }
    }
}
