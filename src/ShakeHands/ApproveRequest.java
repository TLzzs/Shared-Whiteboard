package ShakeHands;

import java.io.Serializable;

public class ApproveRequest implements Serializable {
    private Boolean isApprove;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ApproveRequest(String username) {
        this.userName = username;
    }

    public Boolean isApprove() {
        return isApprove;
    }

    public void setApprove(boolean approve) {
        isApprove = approve;
    }
}
