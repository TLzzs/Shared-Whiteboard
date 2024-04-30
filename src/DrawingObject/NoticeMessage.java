package DrawingObject;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class NoticeMessage extends JDialog {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 100;
    private static final int DISPLAY_TIME_MS = 2000; // Display time in milliseconds

    private JLabel messageLabel;

    public NoticeMessage(JFrame parent, String message) {
        super(parent, true);
        setUndecorated(true);
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(parent);

        // Create a rounded border with modern styling
        setShape(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, 20, 20));
        getContentPane().setBackground(new Color(50, 50, 50, 200)); // Semi-transparent dark background

        // Message label with modern styling
        messageLabel = new JLabel(message);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setVerticalAlignment(SwingConstants.CENTER);
        getContentPane().add(messageLabel);

        // Close the notice after a delay
        Timer timer = new Timer(DISPLAY_TIME_MS, e -> close());
        timer.setRepeats(false);
        timer.start();
    }

    private void close() {
        setVisible(false);
        dispose();
    }
}

