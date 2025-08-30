package eu.simonw.texbot.commands;

import eu.simonw.texbot.TexBot;
import eu.simonw.texbot.tex.TexHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MathsSlashCommand extends ListenerAdapter {
    private final TexHandler handler;
    public MathsSlashCommand(JDA jda, TexHandler handler) {
        this.handler = handler;
        jda.updateCommands().addCommands(
                Commands.slash("maths", "Repeats messages back to you.")
                        .addOption(OptionType.STRING, "latex", "The latex code to be rendered", true, true)
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        TexBot.LOGGER.error("CMD");
        event.deferReply(false).queue();
        CompletableFuture<Path> py = handler.convert("$\\Pi=3$", TexHandler.ConversionType.TexToPng);
        TexBot.LOGGER.error("start");
        assert py != null;
        py.thenAccept(pa-> {
            TexBot.LOGGER.info("{}", pa.toAbsolutePath().toString());
            MessageCreateData msg = new MessageCreateBuilder().addContent("Your compiled Pdf:").addFiles(FileUpload.fromData(pa, StandardOpenOption.READ)).build();
            event.getHook().sendMessage(msg).queue();
        });
        TexBot.LOGGER.error("isdone");

    }
}
