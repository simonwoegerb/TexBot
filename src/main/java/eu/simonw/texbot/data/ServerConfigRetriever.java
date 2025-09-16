package eu.simonw.texbot.data;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ServerConfigRetriever {
    private ServerConfigDAO dao;
    private AsyncLoadingCache<Long, ServerConfig> configCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .buildAsync((key, executor) -> CompletableFuture.completedFuture(dao.getServer(key)));
    public ServerConfigRetriever(ServerConfigDAO dao) {
        this.dao = dao;
    }
    public CompletableFuture<ServerConfig> getServerConfig(long id) {
        return configCache.get(id);
    }
    public void updateServerConfig(ServerConfig config) {
        dao.update(config);
        configCache.synchronous().invalidate(config.getUuid());
    }
}

