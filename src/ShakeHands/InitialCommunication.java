package ShakeHands;

import java.io.Serializable;

public class InitialCommunication implements Serializable {
    private final String command;
    private final String username;

    public InitialCommunication(String command, String username) {
        this.command = command;
        this.username = username;
    }

    public String getCommand() {
        return command;
    }

    public String getUsername() {
        return username;
    }
}
