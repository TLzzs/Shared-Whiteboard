package Client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class WhiteBoardClient {
    private Socket socket;
    private final String serverAddress;
    private final int port;
    private final Logger logger = Logger.getLogger(WhiteBoardClient.class.getName());

    public WhiteBoardClient(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public void connectToServer() {
        try {
            socket = new Socket(serverAddress, port);
            logger.info("Successfully connected to server at " + serverAddress + ":" + port);
            ClientSideHandler handler = new ClientSideHandler(socket, logger, this);
            handler.startWhiteBoard();
            handler.startCommunication();
        } catch (UnknownHostException e) {
            logger.severe("Host could not be determined: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (IOException e) {
            logger.severe("Error: Unable to establish connection to " + serverAddress + ":" + port + ". " + e.getMessage());
            // Consider reconnection logic here
        } catch (IllegalArgumentException e) {
            logger.severe("Error: Port parameter is outside the specified range of valid port values.");
            throw new RuntimeException(e);
        }
    }



    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java WhiteBoardClient <server address> <port number>");
            return;
        }
        String serverAddress = args[0];
        int port = Integer.parseInt(args[1]);

        WhiteBoardClient client = new WhiteBoardClient(serverAddress, port);
        client.connectToServer();
    }

}
