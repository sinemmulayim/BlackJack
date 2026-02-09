package ui;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controller.GameController;

public class GameOverPanel extends JPanel {

    private final GameController controller;

    private final JTextArea text = new JTextArea();
    private final JButton restart = new JButton("Restart");
    private final JButton menu = new JButton("Main Menu");

    public GameOverPanel(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));

        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        add(new JScrollPane(text), BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        restart.addActionListener(e -> controller.restartFromGameOver());
        menu.addActionListener(e -> controller.showMenu());
        buttons.add(restart);
        buttons.add(menu);

        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        text.setText(controller.finalSummary());
    }
}
