package Client;

import DrawingObject.*;
import DrawingObject.Rectangle;

import javax.swing.*;
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

    private JPanel subToolBarShape, subToolBarEraser;
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

        // Tool buttons
        setupToolBar();
        setupsubToolBarShape();

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
                            Eraser eraser = new Eraser(oldX, oldY, currentX, currentY, Color.WHITE, 10);
                            eraser.execute(g2d);
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

    private void setupToolBar() {
        JPanel toolBar = new JPanel();
        toolBar.setLayout(new GridLayout(7, 1));
        toolBar.setBackground(Color.LIGHT_GRAY);

        // Define tool icons
        String[] iconPaths = {
                "src/icons/freeLine.png", "src/icons/line.png", "src/icons/circle.png",
                "src/icons/rectangle.png", "src/icons/oval.png", "src/icons/eraser.png",
                "src/icons/text.png"
        };
        String[] toolNames = {
                "FreeLine", "Line", "Circle", "Rectangle", "Oval", "Eraser", "Text"
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
            updatesubToolBarShapeVisibility();
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
        getContentPane().add(subToolBarShape, BorderLayout.NORTH);

        // Listener to update the stroke based on spinner value change
        strokeSizeSpinner.addChangeListener(e -> {
            int strokeSize = (int) ((JSpinner) e.getSource()).getValue();
            g2d.setStroke(new BasicStroke(strokeSize));
        });
    }

    private void updatesubToolBarShapeVisibility() {
        // Update visibility based on the selected tool
        boolean isShape = Arrays.asList("FreeLine", "Line", "Circle", "Rectangle", "Oval").contains(currentTool);
        subToolBarShape.setVisible(isShape);

    }

    public void updateShape(DrawingShape shape, int x1, int y1, int x2, int y2) {
        if (shape instanceof Circle) {
            Circle circle = (Circle) shape;
            int radius = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            circle.update(x1,y1, radius, radius);
        } else if (shape instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) shape;
            int width = Math.abs(x2 - x1);
            int height = Math.abs(y2 - y1);
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            rectangle.update(minX, minY, width, height);
        }
    }


    private DrawingShape createShape(int x1, int y1, int x2, int y2, Color color) {
        switch (currentTool) {
            case "FreeLine":
                return new FreeLine(x1, y1, x2, y2, color, ((BasicStroke)g2d.getStroke()).getLineWidth());
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
//                return new Oval(x1, y1, ovalWidth, ovalHeight, g2d.getColor());
            default:
                return null;
        }
    }

    private void initCanvas() {
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
