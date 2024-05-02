package Client;

import Client.wbHandler.ChatWindowHandler;
import Client.wbHandler.ToolBarHandler;
import DrawingObject.InitWindow.NoticeMessage;
import DrawingObject.Shape.DrawingShape;
import DrawingObject.Shape.Eraser;
import DrawingObject.drawingPanelElements.ExistingCanvas;
import DrawingObject.drawingPanelElements.JTextCompositeKey;
import DrawingObject.drawingPanelElements.SavedCanvas;
import DrawingObject.drawingPanelElements.TextOnBoard;
import ShakeHands.ApproveRequest;
import ShakeHands.ChatWindow.Message;
import ShakeHands.Notice;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static Client.Utility.TextFieldUtility.addTextFieldListener;
import static Client.Utility.shapeUtility.createShape;
import static Client.Utility.shapeUtility.updateShape;

public class WhiteBoardGUI extends JFrame {

    private boolean isAdmin;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    private BufferedImage canvas;
    private Graphics2D g2d;
    private int currentX, currentY, oldX, oldY;
    private DrawingShape tempShape;
    private final Logger logger = Logger.getLogger(WhiteBoardGUI.class.getName());
    private final ClientSideHandler clientSideHandler;
    private ToolBarHandler toolBarHandler;
    private ChatWindowHandler chatWindowHandler;
    private JPanel dynamicToolBar, drawingPanel;
    private Map<UUID, JTextCompositeKey> textFieldMap = new ConcurrentHashMap<>();

    public WhiteBoardGUI(ClientSideHandler clientSideHandler) {
        this.clientSideHandler = clientSideHandler;
        this.toolBarHandler = new ToolBarHandler(g2d, this, canvas);
        this.chatWindowHandler= new ChatWindowHandler();
        initUI();
    }

    private void initUI() {
        setTitle("WhiteBoard Client");
        setSize(1100, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //main tool bar
        JPanel toolBar = toolBarHandler.setupToolBar();
        getContentPane().add(toolBar, BorderLayout.WEST);

        JPanel chatPanel = chatWindowHandler.setUpChatWindow(this);
        getContentPane().add(chatPanel, BorderLayout.EAST);

        //dynamic sub tool bar
        dynamicToolBar = new JPanel(new CardLayout());
        JPanel subToolBarShape = toolBarHandler.setupsubToolBarShape();
        JPanel subToolBarText = toolBarHandler.setupSubToolBarText();
        dynamicToolBar.add(subToolBarShape, "Shape");
        dynamicToolBar.add(subToolBarText, "Text");
        getContentPane().add(dynamicToolBar, BorderLayout.NORTH);


        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (canvas == null) {
                    initCanvas();
                    toolBarHandler.setG2d(g2d);
                    toolBarHandler.setCanvas(canvas);
                }
                g.drawImage(canvas, 0, 0, null);
            }
        };

        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentX = e.getX();
                currentY = e.getY();

                if (g2d != null) {
                    switch (toolBarHandler.getCurrentTool()) {
                        case "Eraser" -> {
                            Color currentColor = g2d.getColor(); Stroke currentStroke = g2d.getStroke();
                            Eraser eraser = new Eraser(oldX, oldY, currentX, currentY, Color.WHITE, 10);
                            eraser.execute(g2d);
                            g2d.setColor(currentColor); g2d.setStroke(currentStroke);
                            sendUpdateToServer(eraser);
                            oldX = currentX;
                            oldY = currentY;
                        }
                        case "FreeLine" -> {
                            DrawingShape shape = createShape(oldX, oldY, currentX, currentY, g2d.getColor(), toolBarHandler.getCurrentTool(), ((BasicStroke)g2d.getStroke()).getLineWidth());
                            if (shape != null) {
                                shape.execute(g2d);
                                sendUpdateToServer(shape);
                            }
                            oldX = currentX;
                            oldY = currentY;
                        }
                        default -> {
                            if (tempShape != null) {
                                g2d.setXORMode(drawingPanel.getBackground());
                                tempShape.drawTemporary(g2d);
                                updateShape(tempShape, oldX, oldY, currentX, currentY);
                                tempShape.drawTemporary(g2d);
                            }
                        }
                    }
                }
                repaint();
            }
        });

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();

                if ("Text".equals(toolBarHandler.getCurrentTool())) {
                    createAndAddTextBox(drawingPanel, e.getX(), e.getY());
                }
                tempShape = createShape(oldX, oldY, oldX, oldY, g2d.getColor(), toolBarHandler.getCurrentTool(),
                        ((BasicStroke)g2d.getStroke()).getLineWidth());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (tempShape != null) {
                    currentX = e.getX();
                    currentY = e.getY();
                    updateShape(tempShape, oldX, oldY, currentX, currentY);
                    tempShape.execute(g2d);
                    sendUpdateToServer(tempShape);
                    tempShape = null;
                    repaint();
                }
            }
        });

        getContentPane().add(drawingPanel, BorderLayout.CENTER);
    }

    private void createAndAddTextBox(JPanel panel, int x, int y) {
        JTextField textField = new JTextField(10);
        textField.setBorder(new EmptyBorder(0, 0, 0, 0));
        textField.setOpaque(false);
        textField.setLocation(x, y);
        textField.setSize(textField.getPreferredSize());

        textField.setFont(g2d.getFont());
        textField.setForeground(g2d.getColor());

        panel.setLayout(null);
        panel.add(textField);
        textField.requestFocusInWindow();

        TextOnBoard textOnBoard = new TextOnBoard(textField);
        textFieldMap.put (textOnBoard.getUuid(), new JTextCompositeKey(textOnBoard, textField));

        addTextFieldListener(textField, textOnBoard, toolBarHandler.getTextButton(), this, drawingPanel);
        textField.addActionListener(e -> {
            g2d.drawString(textField.getText(), textField.getX(), textField.getY());
            panel.remove(textField);
            panel.revalidate();
            panel.repaint();
        });

    }

    public void updatesubToolBarVisibility() {
        CardLayout cl = (CardLayout)(dynamicToolBar.getLayout());
        if (Arrays.asList("FreeLine", "Line", "Circle", "Rectangle", "Oval").contains(toolBarHandler.getCurrentTool())) {
            cl.show(dynamicToolBar, "Shape");
        } else if ("Text".equals(toolBarHandler.getCurrentTool())) {
            cl.show(dynamicToolBar, "Text");
        } else {
            cl.show(dynamicToolBar, "Empty"); // You can add an empty panel to hide both toolbars if needed
        }
    }


    public void initCanvas() {
        canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Clear the canvas with a white background
        clearCanvas(Color.WHITE);

        setupDrawing();
    }

    private void setupDrawing() {
        // Set the stroke and color for drawing operations
        g2d.setStroke(new BasicStroke(2));
        g2d.setPaint(Color.BLACK);
    }

    private void clearCanvas(Color color) {
        g2d.setPaint(color);
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        repaint();
    }

    public void display() {
        setVisible(true);
    }

    public void updateDrawing(DrawingShape shape) {
        if (g2d != null) {
            shape.execute(g2d);
            repaint();
        }
    }

    public void updateTextFields(TextOnBoard textField) {
        JTextField jTextField;
        if (!textFieldMap.containsKey(textField.getUuid())) {
            jTextField = textField.createJTextField();
            textFieldMap.put(textField.getUuid(), new JTextCompositeKey(textField, jTextField));
            addTextFieldListener(jTextField, textField, toolBarHandler.getTextButton(), this, drawingPanel);
            drawingPanel.add(jTextField);
            drawingPanel.setLayout(null);
            jTextField.setLayout(null);
        } else {
            jTextField = textFieldMap.get(textField.getUuid()).getjTextField();
            jTextField.setText(textField.getText());
            jTextField.setFont(textField.getFont());
            jTextField.setForeground(textField.getColor());
            jTextField.setOpaque(textField.isOpaque());
            jTextField.setBorder(textField.getBorder());
            jTextField.setLocation(textField.getX(), textField.getY());
            jTextField.setSize(textField.getSize());
        }
        jTextField.addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                System.out.println("TextField moved to: " + jTextField.getLocation());
            }
        });

        drawingPanel.revalidate();
        drawingPanel.repaint();
    }

    public void deleteAll() {
        Color current = g2d.getColor();
        clearCanvas(Color.WHITE);
        g2d.setColor(current);
        textFieldMap.forEach((key, value) -> {
            JTextField jTextField = value.getjTextField();
            drawingPanel.remove(jTextField);
        });
        textFieldMap.clear();
        repaint();
    }

    public void sendUpdateToServer(Object update) {
        clientSideHandler.sendUpdateToServer(update);
    }

    public void showNotice(Notice notice) {
        String userName = notice.getUsername();
        boolean isLeaving = notice.isLeaving();

        // Construct the notice message based on whether the user is leaving or joining
        String noticeMessage;
        if (isLeaving) {
            noticeMessage = userName + " has left the whiteboard.";
        } else {
            noticeMessage = userName + " has joined the whiteboard.";
        }
        NoticeMessage noticeDialog = new NoticeMessage(this, noticeMessage);
        noticeDialog.setVisible(true);
    }


    public void sendChatMessage(String message) {
        Message msg = new Message(userName, message,new Timestamp(System.currentTimeMillis()));
        sendUpdateToServer(msg);
    }

    public void updateChatWindow(Message msg) {
        chatWindowHandler.updateChatWindow(msg);
    }


    public void toggleFileButtonVisibility() {
        toolBarHandler.toggleFileButtonVisibility(true);
    }

    public void showChoice(ExistingCanvas existingCanvas) {
        toolBarHandler.chooseCanvasForEditing(existingCanvas.getSavedCanvasList());
    }

    public void syncBufferedImage(SavedCanvas savedCanvas) {
        byte[] imageData = savedCanvas.getImageData();
        if (imageData != null && imageData.length > 0) {
            try {
                ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
                Color color = g2d.getColor();
                Stroke basicStroke = g2d.getStroke();
                Font font = g2d.getFont();
                canvas = ImageIO.read(bis);
                this.g2d = this.canvas.createGraphics();
                g2d.setColor(color);
                g2d.setStroke(basicStroke);
                g2d.setFont(font);

                toolBarHandler.setG2d(g2d);

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


                repaint();
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the exception as needed
            }
        }
    }

    private JFrame waitingFrame;

    public JFrame drawWaitingWindow() {
        waitingFrame = new JFrame("Waiting for Approval");
        waitingFrame.setLayout(new BorderLayout());
        waitingFrame.add(new JLabel("Please wait until an admin approves your join request.", SwingConstants.CENTER), BorderLayout.CENTER);
        waitingFrame.setSize(300, 200);
        waitingFrame.setLocationRelativeTo(null); // Center on screen
        waitingFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent closing
        waitingFrame.setResizable(false); // Prevent resizing
        waitingFrame.setVisible(true);

        return waitingFrame; // Return the frame for further use
    }

    public void drawRequestWindow(ApproveRequest request) {
        // Create the dialog and set its properties
        JDialog requestDialog = new JDialog(this, "Approve Join Request", true);
        requestDialog.setLayout(new BorderLayout());
        requestDialog.setSize(300, 150);
        requestDialog.setLocationRelativeTo(this);

        // Add a message to the dialog
        JLabel messageLabel = new JLabel("<html>User: <b>" + request.getUserName() + "</b> is requesting to join. Approve or Reject?</html>", SwingConstants.CENTER);
        requestDialog.add(messageLabel, BorderLayout.CENTER);

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);
        requestDialog.add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners to buttons
        approveButton.addActionListener(e -> {
            request.setApprove(true);
            sendUpdateToServer(request);
            requestDialog.dispose();
        });

        rejectButton.addActionListener(e -> {
            request.setApprove(false);
            sendUpdateToServer(request);
            requestDialog.dispose();
        });

        // Show the dialog
        requestDialog.setVisible(true);
    }


    public void closeWindow() {
        waitingFrame.dispose();
    }
}
