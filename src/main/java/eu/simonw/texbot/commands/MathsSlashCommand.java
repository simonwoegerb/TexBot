package eu.simonw.texbot.commands;

import eu.simonw.texbot.EmbedCreator;
import eu.simonw.texbot.data.ServerConfigRetriever;
import eu.simonw.texbot.paste.PasteHandler;
import eu.simonw.texbot.paste.PasteManager;
import eu.simonw.texbot.tex.TexHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static eu.simonw.texbot.TexBot.LOGGER;

public class MathsSlashCommand extends ListenerAdapter {
    private final TexHandler texHandler;
    private final EmbedCreator embedCreator;
    private final SlashCommandData INSTANCE;
    private final PasteManager pasteManager;
    private final ServerConfigRetriever serverConfigRetriever;

    public MathsSlashCommand(TexHandler texHandler, PasteManager pasteManager, ServerConfigRetriever serverConfigRetriever) {
        this.texHandler = texHandler;
        this.serverConfigRetriever = serverConfigRetriever;
        this.pasteManager = pasteManager;
        embedCreator = new EmbedCreator();
        INSTANCE = Commands.slash("maths", "Repeats messages back to you.")
                .addSubcommands(
                        new SubcommandData("inline", "Renders inline code given by argument")
                                .addOption(OptionType.STRING, "code", "LaTeX code to render", true))
                .addSubcommands(
                        new SubcommandData("pastelink", "Renders code given by a pastebin link")
                                .addOption(OptionType.STRING, "url", "Paste URL", true)
                                .addOptions(new OptionData(OptionType.STRING, "type", "Type of Paste link", true, false)
                                        .addChoice("lucko", "lucko")
                                        .addChoice("pastebin", "pastebin")
                                        .addChoice("privatebin", "privatebin")
                                        .addChoice("hastebin", "hastebin"))
                                .addOptions(new OptionData(OptionType.STRING, "conversion_target", "Resulting File", false, false)
                                        .addChoice("pdf", "pdf")
                                        .addChoice("png", "png")

                ));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        final String command = event.getFullCommandName();
        //If command matches: immediately deferReply
        if (command.startsWith("maths")) event.deferReply(false).queue();
        else return;
        CompletableFuture<String> codeFuture = null;

        if (command.equalsIgnoreCase("maths inline")) {

            //Inline branch
            codeFuture = CompletableFuture.completedFuture(event.getOption("code", OptionMapping::getAsString));
        } else if (command.equalsIgnoreCase("maths pastelink")) {

            // Pastebin branch
            final String link = event.getOption("url", OptionMapping::getAsString);
            final PasteHandler pasteHandler = event.getOption("type", pasteManager::getFromOption);
            if (pasteHandler == null) {
                LOGGER.error("Pastehandler not identified/null");
                return;
            }

            LOGGER.info("Link {}: {}", link, pasteHandler.getClass());

            codeFuture = pasteHandler.get(link);
        }
        if (codeFuture == null) {
            LOGGER.error("Code cannot be retrieved");
            return;
        }
        AtomicBoolean hasError = new AtomicBoolean(false);
        TexHandler.ConversionType conversionType = event.getOption("conversion_target",
                TexHandler.ConversionType.TexToPng, TexHandler.ConversionType::getFromOption);
        codeFuture.thenCompose(texHandler::isSafeLatexAsync)
                .exceptionally(s -> {
                    LOGGER.error("Code was unsafe: {}", s.getMessage());
                    sendErrorEmbed(event.getHook(), s.getMessage());
                    hasError.set(true);
                    return "";
                })
                .thenCompose(s -> {
                    if (s.equalsIgnoreCase("")) return CompletableFuture.completedFuture(null);
                    return texHandler.convert(s, conversionType);
                })

            .thenAccept(path -> {
                if (path == null )  {
                    if (!hasError.get()) sendErrorEmbed(event.getHook(), "Document could not be compiled.");
                    return;
                }
                LOGGER.info("{}", path.toAbsolutePath());
                sendEmbed(event.getHook(), conversionType, event.getUser(), path);


            });
    }

    public SlashCommandData getInstance() {
        return INSTANCE;
    }
    public void sendErrorEmbed(InteractionHook hook, String errorMessage) {
        hook.sendMessageEmbeds(
                embedCreator.createDefaultEmbed()
                        .addField("Command failed:", errorMessage, false)
                        .build()).queue();
    }
    public void sendEmbed(InteractionHook hook, TexHandler.ConversionType conversionType, User user, Path path) {
        String uuid = UUID.randomUUID().toString();


        switch (conversionType) {

            case TexToPng -> {
                MessageEmbed embed = embedCreator.
                        createDefaultEmbed()
                        .addField("Your LaTeX code has been converted to the following:", "", false)
                        .setImage("attachment://" + uuid + ".png")
                        .setFooter("Requested by: " + user.getEffectiveName(), user.getEffectiveAvatarUrl())
                        .build();
                hook.sendMessageEmbeds(embed).addFiles(FileUpload.fromData(path, uuid + ".png", StandardOpenOption.READ)).queue();

            }
            case TexToPdf -> {
                MessageEmbed embed = embedCreator.
                        createDefaultEmbed()
                        .addField("Your LaTeX code has been converted to the following:", "", false)
                        .setFooter("Requested by: " + user.getEffectiveName(), user.getEffectiveAvatarUrl())
                        .build();
                hook.sendMessageEmbeds(embed).addFiles(FileUpload.fromData(path, uuid + ".pdf", StandardOpenOption.READ)).queue();


            }
        }

    }
}
