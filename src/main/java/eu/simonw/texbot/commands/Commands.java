package eu.simonw.texbot.commands;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.HashMap;
import java.util.HashSet;

public class Commands {
    private HashMap<Class<? extends SlashCommandData>, SlashCommandData> commands;

    public Commands(SlashCommandData... slashCommandDatas) {
        for (var cmd : slashCommandDatas) {
            commands.put(cmd.getClass(),cmd);
        }
    }
}
