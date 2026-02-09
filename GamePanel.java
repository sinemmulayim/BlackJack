package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import controller.GameController;

public class GamePanel extends JPanel {

    private final GameController controller;

    // Header/status components (top of the screen)
    private final JLabel topStatus = new JLabel();
    private final JLabel score = new JLabel();
    private final JLabel turn = new JLabel();

    // ✅ Winner label (bigger, bold)
    private final JLabel winnerLabel = new JLabel("", JLabel.CENTER);

    // Small summary text (e.g., last round result)
    private final JTextArea summaryArea = new JTextArea();

    // Card display panels (we show card images instead of plain text)
    private final JPanel playerCardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel dealerCardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

    // Control buttons
    private final JButton hitBtn = new JButton("Hit");
    private final JButton standBtn = new JButton("Stand");
    private final JButton menuBtn = new JButton("Main Menu");

    public GamePanel(GameController controller) {
        this.controller = controller;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header area: status lines + winner + summary
        JPanel header = new JPanel(new GridLayout(5, 1, 0, 6));

        topStatus.setFont(topStatus.getFont().deriveFont(Font.BOLD, 16f));
        score.setFont(score.getFont().deriveFont(Font.BOLD, 16f));
        turn.setFont(turn.getFont().deriveFont(Font.PLAIN, 14f));

        // ✅ Winner label style
        winnerLabel.setFont(winnerLabel.getFont().deriveFont(Font.BOLD, 18f));

        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setOpaque(false);
        summaryArea.setMargin(new Insets(0, 0, 0, 0));
        summaryArea.setFont(summaryArea.getFont().deriveFont(Font.PLAIN, 13f));

        header.add(topStatus);
        header.add(score);
        header.add(turn);
        header.add(winnerLabel);
        header.add(summaryArea);

        add(header, BorderLayout.NORTH);

        playerCardsPanel.setBorder(BorderFactory.createTitledBorder("PLAYER"));
        dealerCardsPanel.setBorder(BorderFactory.createTitledBorder("DEALER"));

        JScrollPane playerScroll = new JScrollPane(playerCardsPanel);
        JScrollPane dealerScroll = new JScrollPane(dealerCardsPanel);

        playerScroll.setBorder(BorderFactory.createEmptyBorder());
        dealerScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel center = new JPanel(new GridLayout(1, 2, 10, 10));
        center.add(playerScroll);
        center.add(dealerScroll);
        add(center, BorderLayout.CENTER);

        JPanel controls = new JPanel();

        hitBtn.addActionListener(e -> controller.playerHit());
        standBtn.addActionListener(e -> controller.playerStand());
        menuBtn.addActionListener(e -> controller.showMenu());

        controls.add(hitBtn);
        controls.add(standBtn);
        controls.add(menuBtn);
        add(controls, BorderLayout.SOUTH);

        bindKeys();
        refresh();
    }

    private void bindKeys() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "HIT");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "STAND");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "MENU");

        am.put("HIT", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                controller.playerHit();
            }
        });

        am.put("STAND", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                controller.playerStand();
            }
        });

        am.put("MENU", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                controller.showMenu();
            }
        });
    }

    private boolean hideDealerSecondCard = true;

    private void renderCards(JPanel target, List<String> resourcePaths) {
        target.removeAll();

        if (resourcePaths == null || resourcePaths.isEmpty()) {
            target.add(new JLabel("(no cards)"));
        } else {

            boolean isDealerPanel = (target == dealerCardsPanel);

            for (int i = 0; i < resourcePaths.size(); i++) {

                String path = resourcePaths.get(i);

                // 🔒 Dealer'ın 2. kartı gizli
                if (isDealerPanel && i == 1 && hideDealerSecondCard) {
                    path = "/images/cards/back.jpg";
                }

                JLabel cardLabel = new JLabel(loadIcon(path));
                cardLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                target.add(cardLabel);
            }
        }

        target.revalidate();
        target.repaint();
    }


 

    private ImageIcon loadIcon(String resourcePath) {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            return new ImageIcon(new byte[0]);
        }

        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) {
            return new ImageIcon(new byte[0]);
        }
        return new ImageIcon(url);
    }
    

    public void refresh() {
        topStatus.setText(controller.topStatusText());
        score.setText(controller.scoreText());
        turn.setText(controller.turnText());

        String summary = controller.lastRoundSummary();
        summaryArea.setText(summary == null ? "" : summary);

        if ("Round finished".equals(controller.turnText()) && summary != null && !summary.trim().isEmpty()) {
            winnerLabel.setText(summary);
        } else {
            winnerLabel.setText("");
        }
        hideDealerSecondCard = controller.canPlayerAct();

        renderCards(playerCardsPanel, controller.playerCardImagePaths());
        renderCards(dealerCardsPanel, controller.dealerCardImagePaths());

        boolean canAct = controller.canPlayerAct();
        hitBtn.setEnabled(canAct);
        standBtn.setEnabled(canAct);
    }

}
