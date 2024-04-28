package Client;

import DrawingObject.TextOnBoard;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TextFieldUtility {
    public static void addTextFieldListener(JTextField textField, TextOnBoard textOnBoard, JToggleButton textButton, WhiteBoardGUI whiteBoardGUI, JPanel drawingPanel) {
        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (textButton != null && !textButton.isSelected()) {
                    textButton.doClick();
                }
            }
        });

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
//                updateText();
            }
            public void removeUpdate(DocumentEvent e) {
                updateText();
            }
            public void insertUpdate(DocumentEvent e) {
                updateText();
            }

            private void updateText() {
                String text = textField.getText();
                if (textField.isFocusOwner()) {
                    FontMetrics metrics = textField.getFontMetrics(textField.getFont());
                    int textWidth = metrics.stringWidth(text) + 10; // Extra padding
                    int newWidth = Math.max(textWidth, 200); // Ensures that the text field does not shrink below the initial size
                    textField.setSize(newWidth, textField.getHeight());
                    textOnBoard.update(textField);
                    drawingPanel.revalidate();
                    drawingPanel.repaint();
                    whiteBoardGUI.sendUpdateToServer(textOnBoard);
                }
            }

        });
    }


}
