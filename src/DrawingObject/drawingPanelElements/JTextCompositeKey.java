package DrawingObject.drawingPanelElements;

import javax.swing.*;

public class JTextCompositeKey {
    private TextOnBoard textOnBoard;
    private JTextField jTextField;

    public TextOnBoard getTextOnBoard() {
        return textOnBoard;
    }

    public JTextField getjTextField() {
        return jTextField;
    }

    public JTextCompositeKey(TextOnBoard textOnBoard, JTextField jTextField) {
        this.textOnBoard = textOnBoard;
        this.jTextField = jTextField;
    }
}
