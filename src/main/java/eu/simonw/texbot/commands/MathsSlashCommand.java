package eu.simonw.texbot.commands;

import eu.simonw.texbot.EmbedCreator;
import eu.simonw.texbot.TexBot;
import eu.simonw.texbot.paste.LuckoPaste;
import eu.simonw.texbot.tex.TexHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.utils.FileUpload;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MathsSlashCommand extends ListenerAdapter {
    private final TexHandler texHandler;
    private final EmbedCreator embedCreator;
    public MathsSlashCommand(TexHandler texHandler) {
        this.texHandler = texHandler;
        embedCreator = new EmbedCreator();
                 Commands.slash("maths", "Repeats messages back to you.")
                        .addSubcommands(
                                new SubcommandData("inline", "Renders inline code given by argument")
                                .addOption(OptionType.STRING, "code", "LaTeX code to render", true))
                        .addSubcommands(
                                new SubcommandData("pastelink", "Renders code given by a pastebin link")
                                        .addOption(OptionType.STRING, "url", "Paste URL", true)
                                        .addOptions(new OptionData(OptionType.STRING, "type", "Type of Paste link", true, true)
                                                .addChoice("lucko", "lucko")
                                                .addChoice("pastebin", "pastebin")
                                                .addChoice("privatebin", "privatebin")
                                                .addChoice("hastebin", "hastebin"))
                        );
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("maths")) {
            TexBot.LOGGER.error("CMD");
            final String code = event.getOption("latex", OptionMapping::getAsString);
            if (!texHandler.isSafeLatex(code)) {
                event.reply("Your code is unsafe.").queue();
                return;
            }
            event.deferReply(false).queue();
            LuckoPaste paste = new LuckoPaste();

            var x = paste.get("http://paste.simonw.eu/oFdPu");
            x.thenAccept(val -> {
                CompletableFuture<Path> py = texHandler.convert(val, TexHandler.ConversionType.TexToPng);
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
                            .build();
                    event.getHook().sendMessageEmbeds(embed).addFiles(FileUpload.fromData(pa, uuid + ".png", StandardOpenOption.READ)).queue();
                });
                TexBot.LOGGER.error("isdone");


            });

        }
    }
}
