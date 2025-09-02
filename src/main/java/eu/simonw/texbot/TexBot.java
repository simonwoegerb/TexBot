package eu.simonw.texbot;

import eu.simonw.texbot.commands.Commands;
import eu.simonw.texbot.commands.MathsSlashCommand;
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

    public static void main(String[] args) {
        final String TOKEN = System.getenv("TOKEN");
        if (TOKEN == null || TOKEN.trim().equalsIgnoreCase("")) {
            LOGGER.error("Token is invalid or empty: {}", TOKEN);
            return;
        }
        LOGGER.info("Token set with last 4 characters: {}", TOKEN.substring(TOKEN.length() - 4));


        final List<GatewayIntent> intents = List.of(
                GatewayIntent.MESSAGE_CONTENT);
        JDA jda = JDABuilder.
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
        TexHandler handler = new TexHandler();
        PasteManager pasteManager = new PasteManager(Map.of(PasteType.LUCKO, new LuckoPaste(), PasteType.PASTEBIN, new PastebinPaste()));
        LOGGER.info("{}", handler.setup_file("tex/template_standalone.tex"));

        var mathscommand = new MathsSlashCommand(handler, pasteManager);
        jda.upsertCommand(mathscommand.getInstance()).queue(s -> LOGGER.info("Command loaded {}", s.getName()));
        jda.addEventListener(
                mathscommand,
                new MathsChatListener(handler));
    }
}
