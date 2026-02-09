package ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import controller.GameController;

public class MenuPanel extends JPanel {

    private final GameController controller;
    private final JTextArea topScoresArea = new JTextArea(8, 26);

    public MenuPanel(GameController controller) {
        this.controller = controller;

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        // ---- Buttons ----
        JButton startBtn = new JButton("Start New Game");
        JButton rulesBtn = new JButton("Rules / Instructions");
        JButton scoresBtn = new JButton("High Scores");
        JButton exitBtn = new JButton("Exit");

        startBtn.addActionListener(e -> controller.startNewGame());
        rulesBtn.addActionListener(e -> controller.showRulesDialog());
        scoresBtn.addActionListener(e -> controller.showHighScoresDialog());
        exitBtn.addActionListener(e -> controller.exit());

        // ---- Button layout ----
        c.gridy = 0;
        add(startBtn, c);

        c.gridy = 1;
        add(rulesBtn, c);

        c.gridy = 2;
        add(scoresBtn, c);

        c.gridy = 3;
        add(exitBtn, c);

        // ---- High scores area ----
        topScoresArea.setEditable(false);
        topScoresArea.setLineWrap(true);
        topScoresArea.setWrapStyleWord(true);
        topScoresArea.setBorder(
                BorderFactory.createTitledBorder("Top Scores")
        );

        JScrollPane scrollPane = new JScrollPane(topScoresArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        add(scrollPane, c);
    }

    public void updateHighScoresText(String text) {
        topScoresArea.setText(text == null ? "" : text);
        topScoresArea.setCaretPosition(0);
    }
}

