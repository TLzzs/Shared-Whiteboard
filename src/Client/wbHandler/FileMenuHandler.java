package Client.wbHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileMenuHandler {
    private JMenuBar menuBar;
    private JFrame parentFrame;

    public FileMenuHandler(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.menuBar = new JMenuBar();
    }

    public JMenuBar createFileMenu() {
        JMenu fileMenu = new JMenu("File");

        // Menu items
        JMenuItem newItem = new JMenuItem("New");
        newItem.addActionListener(this::newAction);
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(this::openAction);
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(this::saveAction);
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As");
        saveAsItem.addActionListener(this::saveAsAction);
        fileMenu.add(saveAsItem);

        menuBar.add(fileMenu);

        return menuBar;
    }

    private void newAction(ActionEvent event) {
        // Implement what happens when "New" is clicked
        System.out.println("New action triggered");
    }

    private void openAction(ActionEvent event) {
        // Implement what happens when "Open" is clicked
        System.out.println("Open action triggered");
    }

    private void saveAction(ActionEvent event) {
        // Implement what happens when "Save" is clicked
        System.out.println("Save action triggered");
    }

    private void saveAsAction(ActionEvent event) {
        // Implement what happens when "Save As" is clicked
        System.out.println("Save As action triggered");
    }
}

