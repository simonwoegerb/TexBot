package eu.simonw.texbot.events;

import eu.simonw.texbot.EmbedCreator;
import eu.simonw.texbot.TexBot;
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
import java.util.concurrent.CompletableFuture;

public class MathsChatListener extends ListenerAdapter {
    private final TexHandler texHandler;
    private final EmbedCreator embedCreator;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public MathsChatListener(TexHandler texHandler) {
        this.texHandler = texHandler;
        this.embedCreator = new EmbedCreator();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String rawMsg = event.getMessage().getContentRaw().trim();
        if (rawMsg.startsWith("$$") && rawMsg.endsWith("$$")) {
            if (!texHandler.isSafeLatex(rawMsg)) {
                event.getMessage().reply(new MessageCreateBuilder().setContent("Your code is unsafe!").build()).mentionRepliedUser(false).queue();
            }
            Thread thread = new Thread(() -> {
                boolean keep_on = true;
                while (keep_on) {
                    event.getChannel().sendTyping().queue();
                    try {
                        Thread.sleep(Duration.ofSeconds(5));
                    } catch (InterruptedException e) {
                        keep_on = false;
                    }

                }
            });
            thread.start();
            CompletableFuture<Path> py = texHandler.convert(rawMsg, TexHandler.ConversionType.TexToPng);
            if (py == null) {
                LOGGER.error("TexHandler returned null.");
                return;
            }
            py.thenAccept(pa -> {
                TexBot.LOGGER.info("{}", pa.toAbsolutePath());
                String uuid = UUID.randomUUID().toString();
                User user = event.getAuthor();
                MessageEmbed embed = embedCreator.
                        createDefaultEmbed()
                        .addField("Your LaTeX code has been converted to the following:", "", false)
                        .setImage("attachment://" + uuid + ".png")
                        .setFooter("Requested by: " + user.getEffectiveName(), user.getEffectiveAvatarUrl())
                        .build();
                event.getMessage().replyEmbeds(embed).addFiles(FileUpload.fromData(pa, uuid + ".png", StandardOpenOption.READ)).queue();
                thread.interrupt();
            });
        }
    }
}
