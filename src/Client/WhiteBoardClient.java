package Client;

import ShakeHands.InitialCommunication;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import static ShakeHands.ConnectUtil.isValidCommand;

public class WhiteBoardClient {
    private Socket socket;
    private final String serverAddress;
    private final int port;
    private final InitialCommunication initialCommunication;
    private final Logger logger;

    public WhiteBoardClient(String serverAddress, int port, InitialCommunication initialCommunication, Logger logger) {
        this.serverAddress = serverAddress;
        this.port = port;
        this.initialCommunication = initialCommunication;
        this.logger = logger;
    }

    public void connectToServer() {
        try {
            socket = new Socket(serverAddress, port);
            logger.info("Successfully connected to server at " + serverAddress + ":" + port);
            ClientSideHandler handler = new ClientSideHandler(socket, logger, this);
            handler.startWhiteBoard();
            handler.startCommunication(initialCommunication);
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
        Logger logger = Logger.getLogger(WhiteBoardClient.class.getName());
        try {
            if (args.length != 4) {
                throw new IllegalArgumentException("Usage: java WhiteBoardClient <server address> <port number> <command> <username>");
            }

            String serverAddress = args[1];
            int port = Integer.parseInt(args[2]);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Error: Port number must be between 1 and 65535.");
            }

            String command = args[0];
            if (!isValidCommand(command)) {
                throw new IllegalArgumentException("Error: Invalid command. Please provide a valid command.");
            }

            WhiteBoardClient client = new WhiteBoardClient(serverAddress, port, new InitialCommunication(args[0], args[3]), logger);
            client.connectToServer();
        } catch (NumberFormatException e) {
            logger.severe("Error: Invalid port number. Port number must be a numeric value.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            logger.severe("Error: Invalid command. Please provide a valid command action.");
            System.exit(1);
        } catch (Exception e) {
            logger.severe("An unexpected error occurred: " + e.getMessage());
            System.exit(1);
        }
    }


}
