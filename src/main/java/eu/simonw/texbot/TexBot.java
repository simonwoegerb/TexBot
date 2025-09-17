package eu.simonw.texbot;

import eu.simonw.texbot.commands.Commands;
import eu.simonw.texbot.commands.MathsSlashCommand;
import eu.simonw.texbot.data.DatabaseHandler;
import eu.simonw.texbot.data.ServerConfigDAO;
import eu.simonw.texbot.data.ServerConfigRetriever;
import eu.simonw.texbot.events.MathsChatListener;
import eu.simonw.texbot.paste.LuckoPaste;
import eu.simonw.texbot.paste.PasteManager;
import eu.simonw.texbot.paste.PasteType;
import eu.simonw.texbot.paste.PastebinPaste;
import eu.simonw.texbot.tex.TexHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TexBot extends ListenerAdapter {
    public static final Logger LOGGER = LoggerFactory.getLogger(TexBot.class);
    private static Commands commands;
    private static JDA jda;
    private static DatabaseHandler databaseHandler;
    private static ServerConfigRetriever serverConfigRetriever;
    public static void main(String[] args) {
        databaseHandler = new DatabaseHandler();
        databaseHandler.connect(System.getenv("POSTGRES_USER"),System.getenv("POSTGRES_PASSWORD")).setup();
        serverConfigRetriever = new ServerConfigRetriever(databaseHandler.jdbi().onDemand(ServerConfigDAO.class));
        setupJDA();


        TexHandler handler = new TexHandler();
        PasteManager pasteManager = new PasteManager(Map.of(PasteType.LUCKO, new LuckoPaste(), PasteType.PASTEBIN, new PastebinPaste()));
        LOGGER.info("{}", handler.setup_file("tex/template_standalone.tex"));
        LOGGER.info("{}", handler.setup_file("tex/template_empty.tex"));

        var mathscommand = new MathsSlashCommand(handler, pasteManager, serverConfigRetriever);
        jda.upsertCommand(mathscommand.getInstance()).queue(s -> LOGGER.info("Command loaded {}", s.getName()));
        jda.addEventListener(
                mathscommand,
                new MathsChatListener(handler, serverConfigRetriever));
    }
    private static void setupJDA() {
        final String TOKEN = System.getenv("TOKEN");
        if (TOKEN == null || TOKEN.trim().equalsIgnoreCase("")) {
            LOGGER.error("Token is invalid or empty: {}", TOKEN);
            return;
        }
        LOGGER.info("Token set with last 4 characters: {}", TOKEN.substring(TOKEN.length() - 4));


        final List<GatewayIntent> intents = List.of(
                GatewayIntent.MESSAGE_CONTENT);
        jda = JDABuilder.
                createLight(TOKEN).
                enableIntents(intents)
                .setAutoReconnect(true)
                .setActivity(Activity.watching("You do maths"))
                .setCompression(Compression.ZLIB)
                .setLargeThreshold(50)
                .setStatus(OnlineStatus.ONLINE)
                .setBulkDeleteSplittingEnabled(false)
                .setChunkingFilter(ChunkingFilter.NONE)
                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOJI,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.STICKER,
                        CacheFlag.VOICE_STATE
                )
                .build();
    }
}
