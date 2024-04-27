package Server;

import DrawingObject.DrawingShape;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class WhiteBoardServer {
    private final int port;
    private final Logger logger;
    private final ExecutorService threadPool;
    private final WhiteBoardState sharedWhiteBoard;

    public WhiteBoardServer(int port, Logger logger) {
        this.port = port;
        this.logger = logger;
        this.sharedWhiteBoard = new WhiteBoardState();
        threadPool = Executors.newFixedThreadPool(10);

        startServer();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server stated and listening on port: " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                    RequestHandler requestHandler = new RequestHandler(clientSocket, this);
                    if (!sharedWhiteBoard.isFirstClient()) {
                        requestHandler.sendInitialState(sharedWhiteBoard.getCurrentState());
                    }
                    sharedWhiteBoard.addClientHandler(requestHandler);
                    threadPool.submit(requestHandler);
                } catch (IOException e) {
                    logger.severe("Exception accepting connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            threadPool.shutdown();
        }
    }

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(WhiteBoardServer.class.getName());

        if (args.length < 1) {
            logger.severe("Usage: java WhiteBoardServer <port number>");
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch  (NumberFormatException e) {
            logger.severe("Error: Invalid port number provided.");
            return;
        }

        new WhiteBoardServer(port, logger);
    }

    public void updateState(DrawingShape clientInput) {
        sharedWhiteBoard.updateState(clientInput);
    }

    public List<RequestHandler> getRequestHandler () {
        return sharedWhiteBoard.getRequestHandler();
    }

    public void deleteAllOperations() {
        sharedWhiteBoard.deleteAllOperations();
    }
}
