package erik.app;

public class AppData {

    private final String callback;
    private final String mode;
    private final String topic;
    private final String secret;
    private final String events;

    public AppData(String callback, String mode, String topic, String secret, String events) {
        this.callback = callback;
        this.mode = mode;
        this.topic = topic;
        this.secret = secret;
        this.events = events;
    }

    public String getCallback() {
        return this.callback;
    }

    public String getMode() {
        return this.mode;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getSecret() {
        return this.secret;
    }

    public String getEvents() {
        return this.events;
    }
}
