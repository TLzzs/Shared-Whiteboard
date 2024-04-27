package Server;

import DrawingObject.DrawingShape;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private final Logger logger = Logger.getLogger(RequestHandler.class.getName());
    private final Socket clientSocket;
    private WhiteBoardState sharedWhiteBoard;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    public RequestHandler(Socket clientSocket, WhiteBoardState sharedWhiteBoard) {
        this.clientSocket = clientSocket;
        this.sharedWhiteBoard = sharedWhiteBoard;
        try {
            this.output = new ObjectOutputStream(clientSocket.getOutputStream());
            this.input = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            logger.severe("Error initializing streams: " + e.getMessage());
            try {
                clientSocket.close();
            } catch (IOException ex) {
                logger.severe("Error closing client socket during initialization: " + ex.getMessage());
            }
        }

    }

    @Override
    public void run() {
        try  {
            Object clientInput;
            while ((clientInput = input.readObject()) != null) {
                logger.info("received Object From Client: "+ clientInput.getClass());
                if (clientInput instanceof DrawingShape) {
                    sharedWhiteBoard.updateState((DrawingShape) clientInput);
                    broadcastUpdate((DrawingShape) clientInput);
                }
            }
        } catch (Exception e) {
            logger.severe("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.severe("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void broadcastUpdate(DrawingShape update) {
        // Implement the broadcast logic to all clients

        for (RequestHandler requestHandler : sharedWhiteBoard.getRequestHandler()) {

            if (!requestHandler.equals(this)) { // Avoid sending the command back to the sender
                logger.info("broadcast to all client: "+ requestHandler.clientSocket.getRemoteSocketAddress());
                requestHandler.sendUpdate(update);
            }
        }
    }

    public void sendUpdate(DrawingShape shape) {
        try {
            output.writeObject(shape);
            output.flush();
        } catch (IOException e) {
            logger.severe("Failed to send update to client: " + e.getMessage());
        }
    }

    public void sendInitialState(List<Object> currentState) {
        currentState.forEach(state -> {
            try {
                output.writeObject(state);
                output.flush();
            } catch (IOException e) {
                logger.severe("Failed to send update to client: " + e.getMessage());
            }
        });
    }
}
