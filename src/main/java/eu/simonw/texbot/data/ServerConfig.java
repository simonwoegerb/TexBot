package eu.simonw.texbot.data;

public class ServerConfig {
    private long uuid;
    private boolean read_messages;
    public ServerConfig(long uuid, boolean read_messages) {
        this.uuid = uuid;
        this.read_messages = read_messages;
    }
    public ServerConfig() {}




    public long getUuid() {
        return uuid;
    }

    public void setUuid(long uuid) {
        this.uuid = uuid;
    }

    public boolean isReadMessages() {
        return read_messages;
    }

    public void setReadMessages(boolean read_messages) {
        this.read_messages = read_messages;
    }
}
