package ShakeHands;

import java.io.Serializable;

public class Notice implements Serializable {
    private String username;
    private boolean isLeaving;

    public Notice(String username, boolean isLeaving) {
        this.username = username;
        this.isLeaving = isLeaving;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isLeaving() {
        return isLeaving;
    }

    public void setLeaving(boolean leaving) {
        isLeaving = leaving;
    }
}
