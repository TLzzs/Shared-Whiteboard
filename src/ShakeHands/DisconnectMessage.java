package ShakeHands;

import java.io.Serializable;

public class DisconnectMessage implements Serializable {
    String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public DisconnectMessage(String userName) {
        this.userName = userName;
    }
}
