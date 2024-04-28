package Client;

import DrawingObject.*;
import DrawingObject.Rectangle;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.logging.Logger;

public class WhiteBoardGUI extends JFrame {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int currentX, currentY, oldX, oldY;
    private String currentTool = "FreeLine"; // Default tool
    private DrawingShape tempShape;
    private final Logger logger = Logger.getLogger(WhiteBoardGUI.class.getName());
    private final ClientSideHandler clientSideHandler;
    private JToggleButton textButton;

    private JPanel dynamicToolBar;
    private JPanel subToolBarShape, subToolBarText;
    public WhiteBoardGUI(ClientSideHandler clientSideHandler) {
        this.clientSideHandler = clientSideHandler;
        initUI();
//        initDrawing();
    }

    private void initUI() {
        setTitle("WhiteBoard Client");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setupToolBar();

        // Tool buttons
        dynamicToolBar = new JPanel(new CardLayout()); // Use CardLayout to switch between panels
        setupsubToolBarShape();
        setupSubToolBarText();
        getContentPane().add(dynamicToolBar, BorderLayout.NORTH);


        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (canvas == null) {
                    initCanvas();
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
                    switch (currentTool) {
                        case "Eraser" -> {
                            Color currentColor = g2d.getColor(); Stroke currentStroke = g2d.getStroke();
                            Eraser eraser = new Eraser(oldX, oldY, currentX, currentY, Color.WHITE, 10);
                            eraser.execute(g2d);
                            g2d.setColor(currentColor); g2d.setStroke(currentStroke);
                            clientSideHandler.sendUpdateToServer(eraser);
                            oldX = currentX;
                            oldY = currentY;
                        }
                        case "FreeLine" -> {
                            DrawingShape shape = createShape(oldX, oldY, currentX, currentY, g2d.getColor());
                            if (shape != null) {
                                shape.execute(g2d);
                                clientSideHandler.sendUpdateToServer(shape);
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

                if ("Text".equals(currentTool)) {
                    createAndAddTextBox(drawingPanel, e.getX(), e.getY());
                }
                tempShape = createShape(oldX, oldY, oldX, oldY, g2d.getColor());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (tempShape != null) {
                    currentX = e.getX();
                    currentY = e.getY();
                    updateShape(tempShape, oldX, oldY, currentX, currentY);
                    tempShape.execute(g2d);
                    clientSideHandler.sendUpdateToServer(tempShape);
                    tempShape = null;
                    repaint();
                }
            }
        });

        getContentPane().add(drawingPanel, BorderLayout.CENTER);
    }

    private void createAndAddTextBox(JPanel panel, int x, int y) {
        JTextField textField = new JTextField(10);
        textField.setBorder(new EmptyBorder(0, 0, 0, 0)); // No border
        textField.setOpaque(false); // Transparent background
        textField.setLocation(x, y);
        textField.setSize(textField.getPreferredSize());

        // Style the text field
        textField.setFont(g2d.getFont());
        textField.setForeground(g2d.getColor());

        panel.setLayout(null); // Set the layout to null for absolute positioning
        panel.add(textField);
        textField.requestFocusInWindow();

        // Handle the text input confirmation (e.g., Enter key)
        textField.addActionListener(e -> {
            g2d.drawString(textField.getText(), textField.getX(), textField.getY());
            panel.remove(textField);
            panel.revalidate();
            panel.repaint();
        });

        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (textButton != null && !textButton.isSelected()) {
                    textButton.doClick(); // Simulate clicking the "Text" button if not already selected
                }
            }
        });

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateText();
            }
            public void removeUpdate(DocumentEvent e) {
                updateText();
            }
            public void insertUpdate(DocumentEvent e) {
                updateText();
            }

            private void updateText() {
                String text = textField.getText();
                FontMetrics metrics = textField.getFontMetrics(textField.getFont());
                int textWidth = metrics.stringWidth(text) + 10; // Extra padding
                int newWidth = Math.max(textWidth, 200); // Ensures that the text field does not shrink below the initial size
                textField.setSize(newWidth, textField.getHeight());
                panel.revalidate();
                panel.repaint();
            }
        });
    }

    private void setupToolBar() {
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new GridLayout(8, 1));
        toolBar.setBackground(Color.LIGHT_GRAY);

        // Define tool icons
        String[] iconPaths = {
                "src/icons/freeLine.png", "src/icons/line.png", "src/icons/circle.png",
                "src/icons/rectangle.png", "src/icons/oval.png", "src/icons/eraser.png",
                "src/icons/text.png", "src/icons/bin.png"
        };
        String[] toolNames = {
                "FreeLine", "Line", "Circle", "Rectangle", "Oval", "Eraser", "Text", "Bin"
        };

        ButtonGroup toolsGroup = new ButtonGroup();
        JToggleButton[] buttons = new JToggleButton[toolNames.length];

        for (int i = 0; i < toolNames.length; i++) {
            buttons[i] = createToggleButton(iconPaths[i], toolNames[i]);
            toolsGroup.add(buttons[i]);
            toolBar.add(buttons[i]);
        }

        getContentPane().add(toolBar, BorderLayout.WEST);
    }

    private JToggleButton createToggleButton(String iconPath, String actionCommand) {
        Icon icon = new ImageIcon(iconPath);
        JToggleButton button = new JToggleButton(icon);
        button.addActionListener(e -> {
            currentTool = actionCommand;
            if ("Text".equals(actionCommand)) {
                textButton = button;
            }
            if ("Bin".equals(currentTool)) {
                initCanvas(); // Assuming you want to clear with white color
                clientSideHandler.sendUpdateToServer(new DeleteAll()); // Adjust this line based on how you handle clear actions on the server
            }
            updatesubToolBarVisibility();
        } );
        return button;
    }

    private void setupsubToolBarShape() {
        subToolBarShape = new JPanel();
        subToolBarShape.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Stroke size selection with icon
        Icon strokeIcon = new ImageIcon("src/icons/stroke.png");
        JLabel strokeLabel = new JLabel(strokeIcon);

        // Setup JSpinner for stroke size
        SpinnerModel model = new SpinnerNumberModel(2, 1, 10, 1); // initial value, min, max, step
        JSpinner strokeSizeSpinner = new JSpinner(model);
        strokeSizeSpinner.setPreferredSize(new Dimension(50, 20)); // Adjust size as needed

        JSpinner.NumberEditor editor = (JSpinner.NumberEditor)strokeSizeSpinner.getEditor();

        NumberFormatter formatter = new NumberFormatter();
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);
        formatter.setMaximum(20);
        formatter.setAllowsInvalid(false); // this is the key part
        formatter.setCommitsOnValidEdit(true);

        editor.getTextField().setFormatterFactory(new DefaultFormatterFactory(formatter));

        strokeSizeSpinner.setEditor(editor);
        Icon colorIcon = new ImageIcon("src/icons/color.png");
        JLabel colorLabel = new JLabel(colorIcon);
        JButton colorButton = new JButton("Choose");
        JColorChooser colorChooser = new JColorChooser();
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(null, "Choose a color", g2d.getColor());
            if (newColor != null) {
                g2d.setPaint(newColor);
            }
        });

        subToolBarShape.add(strokeLabel);
        subToolBarShape.add(strokeSizeSpinner);
        subToolBarShape.add(Box.createHorizontalStrut(20)); // Adds spacing
        subToolBarShape.add(colorLabel);
        subToolBarShape.add(colorButton);

        subToolBarShape.setVisible(true);  // Initially visible
        dynamicToolBar.add(subToolBarShape, "Shape");

        // Listener to update the stroke based on spinner value change
        strokeSizeSpinner.addChangeListener(e -> {
            int strokeSize = (int) ((JSpinner) e.getSource()).getValue();
            g2d.setStroke(new BasicStroke(strokeSize));
        });
    }

    private void setupSubToolBarText() {
        subToolBarText = new JPanel();
        subToolBarText.setLayout(new FlowLayout(FlowLayout.CENTER));

        // Font size selection
        JLabel fontSizeLabel = new JLabel(new ImageIcon("src/icons/fontSize.png"));  // Adjust icon path as needed
        SpinnerModel fontSizeModel = new SpinnerNumberModel(12, 8, 48, 1);  // Default 12, min 8, max 48, step 1
        JSpinner fontSizeSpinner = new JSpinner(fontSizeModel);
        fontSizeSpinner.setPreferredSize(new Dimension(50, 20));

        // Font family selection
        JLabel fontLabel = new JLabel(new ImageIcon("src/icons/font.png"));  // Adjust icon path as needed
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        JComboBox<String> fontComboBox = new JComboBox<>(availableFonts);
        fontComboBox.setSelectedItem("Arial");
        fontComboBox.setPreferredSize(new Dimension(150, 20));

        fontSizeSpinner.addChangeListener(e -> {
            int newSize = (int) ((JSpinner) e.getSource()).getValue();
            Font currentFont = g2d.getFont();
            g2d.setFont(new Font(currentFont.getFontName(), currentFont.getStyle(), newSize));
        });

        fontComboBox.addActionListener(e -> {
            String fontName = (String) ((JComboBox) e.getSource()).getSelectedItem();
            Font currentFont = g2d.getFont();
            g2d.setFont(new Font(fontName, currentFont.getStyle(), currentFont.getSize()));
        });

        // Font color chooser
        JLabel fontColorLabel = new JLabel(new ImageIcon("src/icons/color.png"));  // Adjust icon path as needed
        JButton fontColorButton = new JButton("Choose Color");
        fontColorButton.addActionListener(e -> {
            Color newFontColor = JColorChooser.showDialog(null, "Choose Font Color", g2d.getColor());
            if (newFontColor != null) {
                g2d.setColor(newFontColor);  // Update the current drawing color to the chosen color
            }
        });

        subToolBarText.add(fontSizeLabel);
        subToolBarText.add(fontSizeSpinner);
        subToolBarText.add(Box.createHorizontalStrut(20));
        subToolBarText.add(fontLabel);
        subToolBarText.add(fontComboBox);
        subToolBarText.add(Box.createHorizontalStrut(20));  // Adds spacing
        subToolBarText.add(fontColorLabel);
        subToolBarText.add(fontColorButton);

        subToolBarText.setVisible(false);  // Initially hidden until the Text tool is selected
        dynamicToolBar.add(subToolBarText, "Text");
    }

    private void updatesubToolBarVisibility() {
        CardLayout cl = (CardLayout)(dynamicToolBar.getLayout());
        if (Arrays.asList("FreeLine", "Line", "Circle", "Rectangle", "Oval").contains(currentTool)) {
            cl.show(dynamicToolBar, "Shape");
        } else if ("Text".equals(currentTool)) {
            cl.show(dynamicToolBar, "Text");
        } else {
            cl.show(dynamicToolBar, "Empty"); // You can add an empty panel to hide both toolbars if needed
        }
    }

    public void updateShape(DrawingShape shape, int x1, int y1, int x2, int y2) {
        if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            int centerX = x1 - radius;
            int centerY = y1 - radius;
            circle.update(centerX, centerY, 2 * radius, 2 * radius);
        } else if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            int width = Math.abs(x2 - x1);
            int height = Math.abs(y2 - y1);
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            rectangle.update(minX, minY, width, height);
        } else if (shape instanceof Line) {
            Line line = (Line) shape;
            line.update(x1, y1, x2, y2);
        } else if (shape instanceof Oval) {
            Oval oval = (Oval) shape;
            int ovalWidth = Math.abs(x2 - x1);
            int ovalHeight = Math.abs(y2 - y1);
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            oval.update(minX, minY, ovalWidth, ovalHeight);
        }
    }



    private DrawingShape createShape(int x1, int y1, int x2, int y2, Color color) {
        switch (currentTool) {
            case "FreeLine":
                return new FreeLine(x1, y1, x2, y2, color, ((BasicStroke)g2d.getStroke()).getLineWidth());
            case "Line":
                return new Line(x1, y1, x2, y2, color, ((BasicStroke)g2d.getStroke()).getLineWidth());
            case "Circle":
                int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                int topLeftX = x1 - radius;
                int topLeftY = y1 - radius;
                return new Circle(topLeftX, topLeftY, 0, 0, color, ((BasicStroke)g2d.getStroke()).getLineWidth());
            case "Rectangle":
                int width = Math.abs(x2 - x1);
                int height = Math.abs(y2 - y1);
                return new Rectangle(x1, y1, width, height, color, ((BasicStroke)g2d.getStroke()).getLineWidth());
            case "Oval":
                int ovalWidth = Math.abs(x2 - x1);
                int ovalHeight = Math.abs(y2 - y1);
                return new Oval(x1, y1, ovalWidth, ovalHeight, color, ((BasicStroke)g2d.getStroke()).getLineWidth());
            default:
                return null;
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
}
