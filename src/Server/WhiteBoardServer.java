package Server;

import DrawingObject.Shape.DrawingShape;
import DrawingObject.drawingPanelElements.TextOnBoard;
import ShakeHands.InitialCommunication;
import ShakeHands.Notice;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static ShakeHands.Util.ConnectUtil.*;

public class WhiteBoardServer {
    private final int port;
    private final Logger logger;
    private final ExecutorService threadPool;
    private WhiteBoardState sharedWhiteBoard;
    private List<Socket> socketList;

    public WhiteBoardServer(int port, Logger logger) {
        this.port = port;
        this.logger = logger;
        this.sharedWhiteBoard = new WhiteBoardState();
        this.socketList = new ArrayList<>();
        threadPool = Executors.newFixedThreadPool(10);

        startServer();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server stated and listening on port: " + port);
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    socketList.add(clientSocket);
                    logger.info("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                    RequestHandler requestHandler = new RequestHandler(clientSocket, this);
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

    public void updateTextOnBoard(TextOnBoard clientInput) {
        if (!clientInput.isDeleting()) {
            sharedWhiteBoard.addTextOnBoard(clientInput);
        }
    }

    public int checkInitCommand(InitialCommunication clientInput, RequestHandler requestHandler){
        logger.info("Size: " + sharedWhiteBoard.getRequestHandler().size());
        boolean isCreate = clientInput.getCommand().equals(CREATE_COMMAND);
        boolean isFirstClient = sharedWhiteBoard.isFirstClient();
        boolean hasDuplicateUserName = sharedWhiteBoard.checkUserNameDuplicate(clientInput.getUsername());
        logger.info("after check duplicate Size: " + sharedWhiteBoard.getRequestHandler().size());
        if (hasDuplicateUserName) {
            sharedWhiteBoard.deleteRequestHandler(requestHandler);
            return UserNameDuplicate;
        }
        requestHandler.setUserName(clientInput.getUsername());
        requestHandler.setNotice(new Notice(clientInput.getUsername(), false));
        if ((isCreate && isFirstClient) ) {
            requestHandler.setAdmin(true);
            return AcceptCreate;
        } else if (!isCreate && !isFirstClient) {
            requestHandler.setAdmin(false);
            return AcceptJoin;
        } else if (!isCreate && isFirstClient) {
            return JoinFailed;
        }
        return CreateFailed;
    }

    public void removeRequestHandler(RequestHandler requestHandler) {
        sharedWhiteBoard.deleteRequestHandler(requestHandler);
    }

//    public void notifyAdminDisconnect() {
//        sharedWhiteBoard.getRequestHandler().forEach(RequestHandler::raiseEOF);
//    }

    public List<Object> getCurrentState() {
        return sharedWhiteBoard.getCurrentState();
    }

    public List<TextOnBoard> getTextOnBoardList() {
        return sharedWhiteBoard.getTextOnBoardList();
    }

    public void deleteAllTextOnBoard() {
        sharedWhiteBoard.deleteAllTextOnBoard();
    }

    public void cleanAllCache() {
        this.sharedWhiteBoard = new WhiteBoardState();
    }

}
