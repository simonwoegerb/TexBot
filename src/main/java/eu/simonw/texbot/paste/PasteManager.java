package eu.simonw.texbot.paste;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record PasteManager(Map<PasteType, PasteHandler> pasteHandlerMap) {
    public PasteHandler get(PasteType type) {
        return pasteHandlerMap.get(type);

    }

    @Nullable
    public PasteHandler getFromName(String name) {
        return pasteHandlerMap.get(PasteType.getFromName(name));
    }

    public PasteHandler getFromOption(OptionMapping data) {
        return this.getFromName(data.getAsString());
    }
}
