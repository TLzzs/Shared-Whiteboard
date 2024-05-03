package Client;

import DrawingObject.drawingPanelElements.DeleteAll;
import DrawingObject.Shape.DrawingShape;
import DrawingObject.InitWindow.PopupWindow;
import DrawingObject.drawingPanelElements.ExistingCanvas;
import DrawingObject.drawingPanelElements.SavedCanvas;
import DrawingObject.drawingPanelElements.TextOnBoard;
import ShakeHands.*;
import ShakeHands.ChatWindow.Message;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import static ShakeHands.Util.ConnectUtil.*;

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

            actionOnStatusCode(statusCode, initialCommunication);

            if (!wb.isAdmin() && statusCode ==AcceptJoin) {
                System.out.println("reach here ");
                sendUpdateToServer(new ApproveRequest(wb.getUserName()));
                wb.drawWaitingWindow();
                waitTilAdminReply();
                sendUpdateToServer(new SyncNotificatioon());
            }

            // Start a thread to listen for server updates
            new Thread(this::listenForServerUpdates).start();
//            sendUpdateToServer(new SyncNotificatioon());
        } catch (IOException e) {
            logger.severe("Error initializing communication streams: " + e.getMessage());
        }
    }

    private void waitTilAdminReply() {
        try {
            while (true) {
                Object serverMessage = input.readObject();
                System.out.println("server message" + serverMessage.getClass());
                if (serverMessage instanceof ApproveRequest) {
                    SwingUtilities.invokeLater(() -> {
                        closeWaitingWindow((ApproveRequest) serverMessage);
                    });
                    return;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeWaitingWindow(ApproveRequest serverMessage) {
        if (serverMessage.isApprove()) {
            wb.closeWindow();
        } else {
            SwingUtilities.invokeLater(() -> {
                PopupWindow popup = new PopupWindow("Admin reject your request , closing...", () -> {
                    closeConnection();
                    System.exit(1);
                });
                popup.adminClose();
            });
        }
    }

    private void actionOnStatusCode(int statusCode, InitialCommunication initialCommunication) {
        logger.info("received status code: "+ statusCode);
        if (statusCode == AcceptCreate) {
            wb.setAdmin(true);
            wb.toggleFileButtonVisibility();
            wb.setUserName(initialCommunication.getUsername());
            return;
        }else if (statusCode == AcceptJoin) {
            wb.setUserName(initialCommunication.getUsername());
            wb.setAdmin(false);
            return;
        }
        printErrorStatusInfo(statusCode, logger);
        SwingUtilities.invokeLater(() -> {
            PopupWindow popup = new PopupWindow(message, () -> {
                closeConnection();
                System.exit(1);
            });
            popup.adminClose();
        });
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
                System.out.println("receive: "+ update.getClass());
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
        } else if (update instanceof CloseMessage) {
            SwingUtilities.invokeLater(() -> {
                PopupWindow popup = new PopupWindow("Admin has closed the whiteboard, logging you out", () -> {
                    closeConnection();
                    System.exit(1);
                });
                popup.adminClose();
            });
        } else if (update instanceof Notice) {
            Notice notice = (Notice) update;
            System.out.println("received notice: " + notice.getUsername() + notice.isLeaving());

            SwingUtilities.invokeLater(() -> {
                wb.showNotice(notice);
            });

            if (wb.isAdmin() && notice.isLeaving()) {
                wb.getUserList().remove(notice.getUsername());
                wb.repaintUserList();
            }
        } else if (update instanceof Message) {
            Message msg = (Message) update;
            wb.updateChatWindow(msg);
        } else if (update instanceof ExistingCanvas) {
            wb.showChoice((ExistingCanvas)update);
        } else if (update instanceof SavedCanvas) {
            System.out.println("received");
            wb.syncBufferedImage((SavedCanvas)update);
        } else if (update instanceof ApproveRequest) {
            System.out.println("show window");
            wb.drawRequestWindow((ApproveRequest) update);
        } else if (update instanceof DisconnectMessage) {
            SwingUtilities.invokeLater(() -> {
                PopupWindow popup = new PopupWindow("You have been kicked out", () -> {
                    closeConnection();
                    System.exit(1);
                });
                popup.adminClose();
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
            closeConnection();
        }
    }

    public void startWhiteBoard() {
        wb = new WhiteBoardGUI(this);
        wb.display();
    }

    private void closeConnection() {
        try {
//            sendUpdateToServer();
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            logger.severe("Error closing network resources: " + e.getMessage());
        }
    }

}
