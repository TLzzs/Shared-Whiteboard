package Client;

import DrawingObject.DeleteAll;
import DrawingObject.DrawingShape;
import DrawingObject.TextOnBoard;
import ShakeHands.InitialCommunication;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import static ShakeHands.ConnectUtil.*;

public class ClientSideHandler {
    private final Socket socket;
    private final Logger logger;
    private final WhiteBoardClient client;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private WhiteBoardGUI wb;

    public ClientSideHandler(Socket socket, Logger logger, WhiteBoardClient client) {
        this.socket = socket;
        this.logger = logger;
        this.client = client;
    }

    public void startCommunication(InitialCommunication initialCommunication) {
        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            output.writeObject(initialCommunication);
            output.flush();

            int statusCode =  waitTilServerReply();

            actionOnStatusCode(statusCode);
            // Start a thread to listen for server updates
            new Thread(this::listenForServerUpdates).start();
        } catch (IOException e) {
            logger.severe("Error initializing communication streams: " + e.getMessage());
        }
    }

    private void actionOnStatusCode(int statusCode) {
        logger.info("received status code: "+ statusCode);
        if (statusCode == Accept) {
            return;
        }
        printErrorStatusInfo(statusCode, logger);
        closeConnection();
        System.exit(1);
    }

    private int waitTilServerReply() throws IOException {
        while (true) {
            try {
                return (int) input.readObject();
            } catch (ClassNotFoundException | IOException e) {
                logger.severe("Error reading object from server: " + e.getMessage());
                throw new IOException("Class not found: " + e.getMessage()); // Rethrow as IOException
            }
        }
    }

    private void listenForServerUpdates() {
        try {
            Object update;
            while ((update = input.readObject()) != null) {
                handleServerUpdate(update);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.severe("Error reading from server from listener: " + e.getMessage());
            closeConnection();
        }
    }

    private void handleServerUpdate(Object update) {
        if (update instanceof DrawingShape) {  // Assuming 'Shape' is a class for graphical objects
            DrawingShape shape = (DrawingShape) update;
            SwingUtilities.invokeLater(() -> {
                wb.updateDrawing(shape);
            });
        } else if (update instanceof DeleteAll) {
            System.out.println("receive delete");
            wb.deleteAll();
        } else if (update instanceof TextOnBoard){
            TextOnBoard textOnBoard = (TextOnBoard) update;
            SwingUtilities.invokeLater(() -> {
                wb.updateTextFields(textOnBoard);
            });
        }
    }

    public void sendUpdateToServer(Object update) {
        try {
            if (output != null) {
                output.reset();
                output.writeObject(update);
                output.flush();
            }
        } catch (IOException e) {
            logger.severe("Error sending update to server: " + e.getMessage());
            closeConnection();
        }
    }

    public void startWhiteBoard() {
        wb = new WhiteBoardGUI(this);
        wb.display();
    }

    private void closeConnection() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            logger.severe("Error closing network resources: " + e.getMessage());
        }
    }

}
