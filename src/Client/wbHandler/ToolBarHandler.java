package Client.wbHandler;

import Client.WhiteBoardGUI;
import DrawingObject.drawingPanelElements.DeleteAll;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.*;

public class ToolBarHandler {
    private String currentTool = "FreeLine"; // Default tool
    private JToggleButton textButton;
    private JPanel subToolBarShape, subToolBarText;
    private Graphics2D g2d;
    private WhiteBoardGUI whiteBoardGUI;

    public ToolBarHandler(Graphics2D g2d, WhiteBoardGUI whiteBoardGUI) {
        this.g2d = g2d;
        this.whiteBoardGUI = whiteBoardGUI;
    }

    public JPanel setupToolBar() {
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
        return toolBar;

    }

    public String getCurrentTool() {
        return currentTool;
    }

    public JToggleButton getTextButton() {
        return textButton;
    }

    public void setG2d(Graphics2D g2d) {
        this.g2d = g2d;
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
                whiteBoardGUI.deleteAll();
                whiteBoardGUI.sendUpdateToServer(new DeleteAll());
            }
            whiteBoardGUI.updatesubToolBarVisibility();
        } );
        return button;
    }


    public JPanel setupsubToolBarShape() {
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
                System.out.println("color is" + newColor);
                g2d.setPaint(newColor);
            }
        });

        subToolBarShape.add(strokeLabel);
        subToolBarShape.add(strokeSizeSpinner);
        subToolBarShape.add(Box.createHorizontalStrut(20)); // Adds spacing
        subToolBarShape.add(colorLabel);
        subToolBarShape.add(colorButton);

        subToolBarShape.setVisible(false);  // Initially visible


        // Listener to update the stroke based on spinner value change
        strokeSizeSpinner.addChangeListener(e -> {
            int strokeSize = (int) ((JSpinner) e.getSource()).getValue();
            g2d.setStroke(new BasicStroke(strokeSize));
        });
        return subToolBarShape;
    }

    public JPanel setupSubToolBarText() {
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
        return subToolBarText;
    }


}
