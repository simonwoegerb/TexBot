package eu.simonw.texbot.events;

import eu.simonw.texbot.EmbedCreator;
import eu.simonw.texbot.TexBot;
import eu.simonw.texbot.data.ServerConfigRetriever;
import eu.simonw.texbot.tex.TexHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;

public class MathsChatListener extends ListenerAdapter {
    private final TexHandler texHandler;
    private final ServerConfigRetriever serverConfigRetriever;
    private final EmbedCreator embedCreator;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public MathsChatListener(TexHandler texHandler, ServerConfigRetriever retriever) {
        this.texHandler = texHandler;
        this.serverConfigRetriever = retriever;
        this.embedCreator = new EmbedCreator();
    }
    private static final ScheduledExecutorService typingExecutor =
            Executors.newScheduledThreadPool(5);

    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor()==event.getJDA().getSelfUser()) return;
        String rawMsg = event.getMessage().getContentRaw().trim();
        long guildId = event.getGuild().getIdLong();

        serverConfigRetriever.getServerConfig(guildId).thenCompose(config -> {
            if (!config.isReadMessages()) return CompletableFuture.completedFuture(null);
            if (!rawMsg.startsWith("$$") || !rawMsg.endsWith("$$")) return CompletableFuture.completedFuture(null);
            if (!texHandler.isSafeLatex(rawMsg)) {
                event.getMessage().reply("Your code is unsafe!").mentionRepliedUser(false).queue();
                return CompletableFuture.completedFuture(null);
            }

            // Start sending typing indicator every 5 seconds
            ScheduledFuture<?> typingTask = typingExecutor.scheduleAtFixedRate(() -> {
                event.getChannel().sendTyping().queue();
            }, 0, 5, TimeUnit.SECONDS);


            return texHandler.convert(rawMsg, TexHandler.ConversionType.TexToPng)
                    .whenComplete((_, _) -> typingTask.cancel(true)); // Stop typing once done
        }).thenAccept(path -> {
            if (path == null) return;

            String uuid = UUID.randomUUID().toString();
            User user = event.getAuthor();

            MessageEmbed embed = embedCreator.createDefaultEmbed()
                    .addField("Your LaTeX code has been converted to the following:", "", false)
                    .setImage("attachment://" + uuid + ".png")
                    .setFooter("Requested by: " + user.getEffectiveName(), user.getEffectiveAvatarUrl())
                    .build();

            event.getMessage()
                    .replyEmbeds(embed)
                    .addFiles(FileUpload.fromData(path, uuid + ".png", StandardOpenOption.READ))
                    .queue();
        }).exceptionally(e -> {
            LOGGER.error("Error during LaTeX handling", e);
            event.getMessage().reply("An error occurred while processing your LaTeX code.").queue();
            return null;
        });
    }



}
