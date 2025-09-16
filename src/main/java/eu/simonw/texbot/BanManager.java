package eu.simonw.texbot;

import net.dv8tion.jda.api.entities.User;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BanManager {
    private final Map<Long, Long> bannedUserIds;
    public final long DEFAULT_BAN_DURATION_IN_SEC = 3600L * 24L; // one day
    private final boolean banFromServer;
    public BanManager(boolean banFromServer) {
        this.banFromServer = banFromServer;
        bannedUserIds = Map.of();

    }
    //bans for a day
    public void ban(Long id) {
        ban(id, DEFAULT_BAN_DURATION_IN_SEC);
    }
    public void ban(Long id, Long duration_in_sec) {

        bannedUserIds.put(id, Instant.now().plus(duration_in_sec, ChronoUnit.SECONDS).toEpochMilli());

    }
    public boolean isBanned(long id) {
        Long banned_until_raw = bannedUserIds.get(id);
        if (banned_until_raw == null) return false;
        if (!Instant.ofEpochMilli(banned_until_raw).isAfter(Instant.now())) {
            return true;
        }
        bannedUserIds.remove(id);
        return false;

    }
}
