package eu.simonw.texbot.paste;

public enum PasteType {
    LUCKO("lucko"), PASTEBIN("pastebin");
    private final String name;

    PasteType(String name) {
        this.name = name;
    }


    public static PasteType getFromName(String name) {
        for (PasteType pt : PasteType.values()) {
            if (pt.name.startsWith(name)) {
                return pt;
            }
        }
        return null;
    }
}
