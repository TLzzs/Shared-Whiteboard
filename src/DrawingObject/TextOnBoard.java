package DrawingObject;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TextOnBoard implements Serializable {
    private UUID uuid;
    private Border border;
    private boolean isOpaque;
    private int x, y;

    public UUID getUuid() {
        return uuid;
    }

    private Dimension size;
    private Font font;
    private Color color;
    private String text;

    public Border getBorder() {
        return border;
    }

    public boolean isOpaque() {
        return isOpaque;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Dimension getSize() {
        return size;
    }

    public Font getFont() {
        return font;
    }

    public Color getColor() {
        return color;
    }

    public TextOnBoard(JTextField textField) {
        this.uuid = UUID.randomUUID();
        this.border = textField.getBorder();
        this.isOpaque = textField.isOpaque();
        this.x = textField.getLocation().x;
        this.y = textField.getLocation().y;
        this.size = textField.getSize();
        this.font = textField.getFont();
        this.color = textField.getForeground();
    }

    public String getText() {
        return text;
    }

    public void update(JTextField textField) {
        this.border = textField.getBorder();
        this.isOpaque = textField.isOpaque();
        this.x = textField.getLocation().x;
        this.y = textField.getLocation().y;
        this.size = textField.getSize();
        this.font = textField.getFont();
        this.color = textField.getForeground();
        this.text = textField.getText();
    }

    public JTextField createJTextField() {
        JTextField jTextField = new JTextField();
        jTextField.setText(text);
        jTextField.setFont(font);
        jTextField.setForeground(color);
        jTextField.setOpaque(isOpaque);
        jTextField.setBorder(border);
        jTextField.setLocation(x, y);
        jTextField.setSize(size);
//        jTextField.requestFocusInWindow();
        return jTextField;
    }

    public boolean hasDiff(JTextField textField) {
        return !Objects.equals(textField.getText(), text) && textField.isOpaque() == isOpaque && textField.getFont() == font
                && textField.getForeground().equals(color) && textField.getBorder().equals(border) && textField.getLocation().x == x
                && textField.getLocation().y == y && textField.getSize().equals(size);
    }
}
