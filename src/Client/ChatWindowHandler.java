package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ChatWindowHandler {
    public JPanel setUpChatWindow() {
        // Chat panel setup
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(300, 600));
        chatPanel.setBackground(Color.WHITE); // Set a white background for a modern look
        chatPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(200, 200, 200))); // 1-pixel wide light gray border

        // Chat messages area setup
        JTextArea chatMessagesArea = new JTextArea();
        chatMessagesArea.setEditable(false);
        chatMessagesArea.setLineWrap(true);
        chatMessagesArea.setWrapStyleWord(true);
        chatMessagesArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatMessagesArea.setForeground(new Color(33, 33, 33)); // Use a dark gray for text for better readability
        chatMessagesArea.setBackground(new Color(250, 250, 250)); // A slightly off-white background for the chat area

        // Padding around the chat messages area
        JPanel messagesContainer = new JPanel(new BorderLayout());
        messagesContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messagesContainer.add(chatMessagesArea);
        messagesContainer.setBackground(chatPanel.getBackground());

        // Scroll pane for the chat messages area
        JScrollPane scrollPane = new JScrollPane(messagesContainer);
        scrollPane.setBorder(null); // Remove the border for a cleaner look
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(5, Integer.MAX_VALUE)); // Thinner scrollbar
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        // Message input field setup
        JTextField messageInputField = new JTextField();
        messageInputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageInputField.setForeground(new Color(33, 33, 33));
        messageInputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1), // Light gray border
                BorderFactory.createEmptyBorder(5, 5, 5, 5))); // Padding
        messageInputField.setBackground(Color.WHITE);

        // Send button setup
        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setContentAreaFilled(false);
        sendButton.setOpaque(true);
        sendButton.setBackground(new Color(30, 136, 229)); // A vibrant blue
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor changes to hand on hover

        // Input panel setup
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Padding around the input panel
        inputPanel.add(messageInputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setBackground(chatPanel.getBackground());

        // Add the input panel to the chat panel
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED); // Red text to signify an error or important message
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Small, modern font
        inputPanel.add(statusLabel, BorderLayout.NORTH);

        // Add action listeners for sending messages
        ActionListener sendMessageListener = e -> {
            String message = messageInputField.getText().trim();
            messageInputField.setText("");
            if (!message.isEmpty() ) {
                chatMessagesArea.append("You: " + message + "\n");
                chatMessagesArea.setCaretPosition(chatMessagesArea.getDocument().getLength()); // Auto-scroll to the latest message
            } else {
                statusLabel.setText("！The message cannot be empty.");
                Timer timer = new Timer(3000, event -> {
                    statusLabel.setText(" "); // Clear the message after 3 seconds
                });
                timer.setRepeats(false); // Ensure the timer only runs once
                timer.start();            }
        };

        sendButton.addActionListener(sendMessageListener);
        messageInputField.addActionListener(sendMessageListener);

        return chatPanel;
    }


}
