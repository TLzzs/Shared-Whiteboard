package DrawingObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PopupWindow extends JFrame {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 150;
    private String message;
    private Runnable onCloseAction;

    public PopupWindow(String message, Runnable onCloseAction) {
        super("Alert");
        this.message = message;
        this.onCloseAction = onCloseAction;
    }

    public void adminClose () {

        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        JButton okButton = new JButton("OK");

        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCloseAction.run();
                dispose();
            }
        });

        panel.add(messageLabel, BorderLayout.CENTER);
        panel.add(okButton, BorderLayout.SOUTH);
        getContentPane().add(panel);

        setVisible(true);
    }
}
