package ShakeHands.Util;

import java.util.logging.Logger;

public class ConnectUtil {
    public static String message;
    public static final String CREATE_COMMAND = "CreateWhiteBoard";
    public static final String JOIN_COMMAND = "JoinWhiteBoard";
    public static final int AcceptCreate = 200;
    public static final int AcceptJoin = 201;
    public static final int UserNameDuplicate = 401;
    public static final int CreateFailed = 400;
    public static final int JoinFailed = 402;

    public static boolean isValidCommand(String command) {
        return CREATE_COMMAND.equalsIgnoreCase(command) || JOIN_COMMAND.equalsIgnoreCase(command);
    }

    public static void printErrorStatusInfo (int statusCode, Logger logger) {
        if (statusCode == CreateFailed) {
            message = "Can not create white board, board already existed";
            logger.severe(message);
        } else if (statusCode == JoinFailed) {
            message = "Can not join, whiteboard has not yet been created";
            logger.severe(message);
        } else if (statusCode == UserNameDuplicate) {
            message=" UserName Duplicate , please choose another name";
            logger.severe(message);

        }
    }
}
