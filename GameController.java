package controller;

import java.util.ArrayList;
import java.util.List;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import model.Card;
import model.Deck;
import model.HumanPlayer;
import model.ComputerPlayer;
import ui.MenuPanel;
import ui.GamePanel;
import ui.GameOverPanel;

public class GameController {

    public static final int MAX_ROUNDS = 10;

    private final Deck deck = new Deck();
    private final HumanPlayer human = new HumanPlayer("Player");
    private final ComputerPlayer computer = new ComputerPlayer("Dealer");

    private int roundNumber;
    private boolean playerTurn;
    private boolean roundOver;
    private boolean hideDealerFirstCard;

    private String lastRoundSummary = "";
    private String finalSummary = "";

    private JFrame frame;
    private JPanel root;
    private CardLayout layout;

    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private GameOverPanel gameOverPanel;

    private Timer dealerTimer;
    private Timer nextRoundTimer;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new GameController().start());
    }

    public void start() {
        frame = new JFrame("Simplified Blackjack");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        layout = new CardLayout();
        root = new JPanel(layout);

        menuPanel = new MenuPanel(this);
        menuPanel.updateHighScoresText(highScoresText());

        gamePanel = new GamePanel(this);
        gameOverPanel = new GameOverPanel(this);

        root.add(menuPanel, "MENU");
        root.add(gamePanel, "GAME");
        root.add(gameOverPanel, "OVER");

        frame.setContentPane(root);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        showMenu();
    }

    /* ================= MENU ================= */

    public void showMenu() {
        stopTimers();
        menuPanel.updateHighScoresText(highScoresText());
        layout.show(root, "MENU");
    }

    public void startNewGame() {
        stopTimers();

        String name = JOptionPane.showInputDialog(
                frame,
                "Enter your name:",
                human.name()
        );

        if (name == null || name.trim().isEmpty()) {
            showMenu();
            return;
        }

        human.setName(name.trim());

        deck.reset();
        deck.shuffle();

        human.resetGameScore();
        computer.resetGameScore();

        roundNumber = 0;
        finalSummary = "";

        startNextRound();
    }

    /* ================= ROUNDS ================= */

    private void startNextRound() {
        stopTimers();

        if (roundNumber >= MAX_ROUNDS) {
            endGame();
            return;
        }

        roundNumber++;
        roundOver = false;
        playerTurn = true;
        hideDealerFirstCard = true;
        lastRoundSummary = "";

        human.resetHand();
        computer.resetHand();

        dealInitialCards();

        layout.show(root, "GAME");
        gamePanel.refresh();
    }

    private void dealInitialCards() {
        ensureCardsAvailable(4);
        human.addCard(deck.draw());
        computer.addCard(deck.draw());
        human.addCard(deck.draw());
        computer.addCard(deck.draw());
    }

    private void ensureCardsAvailable(int needed) {
        if (deck.remaining() < needed) {
            deck.reset();
            deck.shuffle();
        }
    }

    /* ================= PLAYER ACTIONS ================= */

    public void playerHit() {
        if (!playerTurn || roundOver) return;

        ensureCardsAvailable(1);
        human.addCard(deck.draw());
        gamePanel.refresh();

        if (human.isBust()) {
            lastRoundSummary = human.name() + " BUST! Dealer wins the round.";
            computer.addGameScore(1);
            finishRound();
        }
    }

    public void playerStand() {
        if (!playerTurn || roundOver) return;

        playerTurn = false;
        hideDealerFirstCard = false;
        gamePanel.refresh();

        startDealerTurnWithTimer();
    }

    /* ================= DEALER LOGIC ================= */

    private void startDealerTurnWithTimer() {
        stopDealerTimer();

        dealerTimer = new Timer(550, e -> {

            if (roundOver) {
                stopDealerTimer();
                return;
            }

            if (computer.isBust()) {
                lastRoundSummary = "Dealer BUST! " + human.name() + " wins the round.";
                human.addGameScore(1);
                stopDealerTimer();
                finishRound();
                return;
            }

            if (computer.shouldHit()) {
                ensureCardsAvailable(1);
                computer.addCard(deck.draw());
                gamePanel.refresh();
            } else {
                stopDealerTimer();
                evaluateAndScoreRound();
                finishRound();
            }
        });

        dealerTimer.start();
    }

    private void evaluateAndScoreRound() {
        int p = human.bestTotal();
        int d = computer.bestTotal();

        if (p > d) {
            lastRoundSummary = human.name() + " wins the round (" + p + " vs " + d + ").";
            human.addGameScore(1);
        } else if (d > p) {
            lastRoundSummary = "Dealer wins the round (" + d + " vs " + p + ").";
            computer.addGameScore(1);
        } else {
            lastRoundSummary = "Draw (push) (" + p + " vs " + d + ").";
        }
    }

    private void finishRound() {
        roundOver = true;
        playerTurn = false;
        hideDealerFirstCard = false;
        gamePanel.refresh();

        nextRoundTimer = new Timer(900, e -> {
            stopNextRoundTimer();
            if (roundNumber >= MAX_ROUNDS) endGame();
            else startNextRound();
        });
        nextRoundTimer.setRepeats(false);
        nextRoundTimer.start();
    }

    /* ================= GAME OVER ================= */

    private void endGame() {
        stopTimers();

        String winner;
        if (human.gameScore() > computer.gameScore()) {
            winner = human.name() + " wins the game!";
        } else if (computer.gameScore() > human.gameScore()) {
            winner = "Dealer wins the game!";
        } else {
            winner = "Game is a draw!";
        }

        finalSummary = "Final Score\n"
                + human.name() + ": " + human.gameScore()
                + "\nDealer: " + computer.gameScore()
                + "\n\n" + winner;

        saveHighScore(human.name(), human.gameScore(), LocalDate.now().toString());
        menuPanel.updateHighScoresText(highScoresText());

        gameOverPanel.refresh();
        layout.show(root, "OVER");
    }

    public void restartFromGameOver() {
        startNewGame();
    }

    /* ================= UI HELPERS ================= */

    public String playerHandText() {
        return human.handText(false) + "\n\nTotal: " + human.bestTotal();
    }

    public String dealerHandText() {
        return computer.handText(hideDealerFirstCard) + "\n\nTotal: "
                + (hideDealerFirstCard ? "?" : computer.bestTotal());
    }

    public String topStatusText() {
        return "Round " + roundNumber + "/" + MAX_ROUNDS
                + "   Remaining cards: " + deck.remaining();
    }

    public String scoreText() {
        return human.name() + ": " + human.gameScore()
                + "    Dealer: " + computer.gameScore();
    }

    public String turnText() {
        if (roundOver) return "Round finished";
        return playerTurn ? "Your turn" : "Dealer turn";
    }

    public boolean canPlayerAct() {
        return playerTurn && !roundOver;
    }

    public String lastRoundSummary() {
        return lastRoundSummary;
    }

    public String finalSummary() {
        return finalSummary;
    }

    /* ================= RULES & SCORES ================= */

    public void showRulesDialog() {
        String text =
                "Simplified Blackjack Rules\n\n" +
                "- You and Dealer get 2 cards.\n" +
                "- You can Hit or Stand.\n" +
                "- If you go over 21: Bust.\n" +
                "- Dealer hits until reaching 17, then stands.\n" +
                "- Game ends after " + MAX_ROUNDS + " rounds.\n\n" +
                "Card values:\n" +
                "2-10 = face value\n" +
                "J/Q/K = 10\n" +
                "A = 1 or 11 (best for total <= 21)";

        JOptionPane.showMessageDialog(frame, text, "Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showHighScoresDialog() {
        JOptionPane.showMessageDialog(frame, highScoresText(), "High Scores", JOptionPane.INFORMATION_MESSAGE);
    }

    public String highScoresText() {
        List<String[]> rows = loadHighScores();
        rows.sort(
        	    Comparator.comparingInt((String[] r) -> parseIntSafe(r[1])).reversed()
        	);


        StringBuilder sb = new StringBuilder();
        sb.append("Name | Score | Date\n");
        sb.append("-------------------\n");

        int limit = Math.min(10, rows.size());
        for (int i = 0; i < limit; i++) {
            String[] r = rows.get(i);
            sb.append(r[0]).append(" | ").append(r[1]).append(" | ").append(r[2]).append("\n");
        }
        if (limit == 0) sb.append("(no records)\n");
        return sb.toString();
    }

    private void saveHighScore(String name, int score, String date) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscores.csv", true))) {
            bw.write(safe(name) + "," + score + "," + safe(date));
            bw.newLine();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Could not save high score.");
        }
    }

    private List<String[]> loadHighScores() {
        List<String[]> rows = new ArrayList<>();
        File f = new File("highscores.csv");
        if (!f.exists()) return rows;

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",", -1);
                if (p.length >= 3) rows.add(new String[]{p[0], p[1], p[2]});
            }
        } catch (Exception e) { }
        return rows;
    }

    private int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace(",", " ").trim();
    }

    public void exit() {
        System.exit(0);
    }

    private void stopTimers() {
        stopDealerTimer();
        stopNextRoundTimer();
    }

    private void stopDealerTimer() {
        if (dealerTimer != null) {
            dealerTimer.stop();
            dealerTimer = null;
        }
    }

    private void stopNextRoundTimer() {
        if (nextRoundTimer != null) {
            nextRoundTimer.stop();
            nextRoundTimer = null;
        }
    }

    public List<String> playerCardImagePaths() {
        return human.getCards().stream()
                .map(Card::resolvedImagePath)
                .toList();
    }
    public List<String> dealerCardImagePaths() {

        List<String> paths = new ArrayList<>();

        List<Card> cards = computer.getCards(); 

        for (int i = 0; i < cards.size(); i++) {

            // Oyuncu hala oynuyorsa → dealer 2. kart kapalı
            if (i == 1 && canPlayerAct()) {
                paths.add("/images/cards/back.jpg");
            } else {
                paths.add(cards.get(i).resolvedImagePath());
            }
        }

        return paths;
    }
}






