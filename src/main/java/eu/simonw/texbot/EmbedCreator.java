package eu.simonw.texbot;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class EmbedCreator {
    public final String BOT_ICON_URL = "https://github.com/simonwoegerb/TexBot/blob/main/icon.png?raw=true";
    public final String BOT_URL = "https://github.com/simonwoegerb/TexBot";

    public EmbedBuilder createDefaultEmbed() {
        return new EmbedBuilder()
                .setAuthor("TexBot", BOT_URL, BOT_ICON_URL)
                .setColor(Color.CYAN)
                ;
    }
}
