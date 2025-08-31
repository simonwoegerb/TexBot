package eu.simonw.texbot.commands;

import eu.simonw.texbot.EmbedCreator;
import eu.simonw.texbot.TexBot;
import eu.simonw.texbot.tex.TexHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MathsSlashCommand extends ListenerAdapter {
    private final TexHandler texHandler;
    private final EmbedCreator embedCreator;
    public MathsSlashCommand(JDA jda, TexHandler texHandler) {
        this.texHandler = texHandler;
        embedCreator = new EmbedCreator();
        jda.updateCommands().addCommands(
                Commands.slash("maths", "Repeats messages back to you.")
                        .addOption(OptionType.STRING, "latex", "The latex code to be rendered", true, true)
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("maths")) {
            TexBot.LOGGER.error("CMD");
            String code = event.getOption("latex", OptionMapping::getAsString);
        /*if (!texHandler.isSafeLatex(code)) {
            event.reply("Your code is unsafe.").queue();
            return;
        }*/
            event.deferReply(false).queue();
            CompletableFuture<Path> py = texHandler.convert(code, TexHandler.ConversionType.TexToPng);
            TexBot.LOGGER.error("start");
            assert py != null;
            py.thenAccept(pa -> {
                TexBot.LOGGER.info("{}", pa.toAbsolutePath().toString());
                String uuid = UUID.randomUUID().toString();
                User user = event.getInteraction().getUser();
                MessageEmbed embed = embedCreator.
                        createDefaultEmbed()
                        .addField("Your LaTeX code has been converted to the following:", "", false)
                        .setImage("attachment://" + uuid + ".png")
                        .setFooter("Requested by: " + user.getEffectiveName(), user.getEffectiveAvatarUrl())
                        .setTitle("TexBot Maths")
                        .build();
                event.getHook().sendMessageEmbeds(embed).addFiles(FileUpload.fromData(pa, uuid + ".png", StandardOpenOption.READ)).queue();
            });
            TexBot.LOGGER.error("isdone");

        }
    }
}
