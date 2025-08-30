package eu.simonw.texbot;

import eu.simonw.texbot.commands.MathsSlashCommand;
import eu.simonw.texbot.tex.TexHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TexBot extends ListenerAdapter {
public static final Logger LOGGER = LoggerFactory.getLogger(TexBot.class);
    public static void main(String[] args) {
        final String TOKEN = System.getenv("TOKEN");
        if (TOKEN == null || TOKEN.trim().equalsIgnoreCase("")) {
            LOGGER.error("Token is invalid {}", TOKEN);
            LOGGER.error("Killing bot");
            return;
        }
        LOGGER.info("Token set with last 4 characters: {}", TOKEN.substring(TOKEN.length() - 4));
        TexHandler handler = new TexHandler();
        LOGGER.info("{}", handler.setup_file("tex/template_standalone.tex"));
        JDA jda = JDABuilder.createLight(TOKEN).enableIntents(List.of(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)).build();
        jda.addEventListener(new MathsSlashCommand(jda, handler));
    }
}
