package Server;

import DrawingObject.DeleteAll;
import DrawingObject.DrawingShape;
import DrawingObject.TextOnBoard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

public class RequestHandler implements Runnable {
    private final Logger logger = Logger.getLogger(RequestHandler.class.getName());
    private final Socket clientSocket;
    private final WhiteBoardServer whiteBoardServer;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    public RequestHandler(Socket clientSocket, WhiteBoardServer whiteBoardServer) {
        this.clientSocket = clientSocket;
        this.whiteBoardServer = whiteBoardServer;
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
                    whiteBoardServer.updateState((DrawingShape) clientInput);
                    broadcastUpdate((DrawingShape) clientInput);
                } else if (clientInput instanceof DeleteAll) {
                    broadcastDeleteAll((DeleteAll) clientInput);
                }else if (clientInput instanceof TextOnBoard){
                    System.out.println("Received : " + ((TextOnBoard)clientInput ).getText() );
                    whiteBoardServer.updateTextOnBoard((TextOnBoard)clientInput);
                    broadcastUpdate(clientInput);
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

    private void broadcastDeleteAll(DeleteAll deleteAll) {
        for (RequestHandler requestHandler : whiteBoardServer.getRequestHandler()) {
            whiteBoardServer.deleteAllOperations();
            if (!requestHandler.equals(this)) { // Avoid sending the command back to the sender
                logger.info("broadcast to all client: "+ requestHandler.clientSocket.getRemoteSocketAddress());
                requestHandler.sendUpdate(deleteAll);
            }
        }
    }

    private void broadcastUpdate(Object update) {
        for (RequestHandler requestHandler : whiteBoardServer.getRequestHandler()) {
            if (!requestHandler.equals(this)) { // Avoid sending the command back to the sender
                logger.info("broadcast to all client: "+ requestHandler.clientSocket.getRemoteSocketAddress());
                requestHandler.sendUpdate(update);
            }
        }
    }

    public void sendUpdate(Object update) {
        try {
            output.writeObject(update);
            output.flush();
        } catch (IOException e) {
            logger.severe("Failed to send update to client: " + e.getMessage());
        }
    }

    public void sendInitialState(List<Object> currentState, List<TextOnBoard> textOnBoardList) {
        currentState.forEach(state -> {
            try {
                output.writeObject(state);
                output.flush();
            } catch (IOException e) {
                logger.severe("Failed to send update to client: " + e.getMessage());
            }
        });


        textOnBoardList.forEach(state -> {
            try {
                output.writeObject(state);
                output.flush();
            } catch (IOException e) {
                logger.severe("Failed to send update to client: " + e.getMessage());
            }
        });
    }
}
