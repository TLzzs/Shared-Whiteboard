package ShakeHands;

import java.util.logging.Logger;

public class ConnectUtil {
    public static final String CREATE_COMMAND = "CreateWhiteBoard";
    public static final String JOIN_COMMAND = "JoinWhiteBoard";
    public static final int Accept = 200;
    public static final int UserNameDuplicate = 401;
    public static final int CreateFailed = 400;
    public static final int JoinFailed = 402;

    public static boolean isValidCommand(String command) {
        return CREATE_COMMAND.equalsIgnoreCase(command) || JOIN_COMMAND.equalsIgnoreCase(command);
    }

    public static void printErrorStatusInfo (int statusCode, Logger logger) {
        if (statusCode == CreateFailed) {
            logger.severe("Can not create white board, board already existed");
        } else if (statusCode == JoinFailed) {
            logger.severe("Can not join the white board , whiteboard has not yet been created");
        } else if (statusCode == UserNameDuplicate) {
            logger.severe("UserName Duplicate , please choose another name");

        }
    }
}
