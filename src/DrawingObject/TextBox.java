package DrawingObject;

import javax.swing.*;
import java.awt.*;

public class TextBox {
    private int x, y;
    private JTextField textField;

    public TextBox(int x, int y, int width, int height, Color color, Font font) {
        this.x = x;
        this.y = y;
        this.textField = new JTextField();
        this.textField.setBounds(x, y, width, height);
        this.textField.setFont(font);
        this.textField.setForeground(color);
        this.textField.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Visible border for clarity
        this.textField.setOpaque(true);
        this.textField.setBackground(Color.WHITE); // Ensure visibility
    }

    public JTextField getTextField() {
        return textField;
    }

    public void update(int newX, int newY, int width, int height) {
        this.textField.setBounds(newX, newY, width, height);
    }
}
