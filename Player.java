package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Player {

    private String name;

    private final List<Card> hand = new ArrayList<>();
    private int gameScore = 0;

    public Player(String name) {
        this.name = (name == null || name.trim().isEmpty()) ? "Player" : name.trim();
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public int gameScore() {
        return gameScore;
    }

    public void addGameScore(int delta) {
        gameScore += delta;
    }

    public void resetGameScore() {
        gameScore = 0;
    }

    public void resetHand() {
        hand.clear();
    }

    public void addCard(Card c) {
        if (c == null) throw new IllegalArgumentException("Card cannot be null");
        hand.add(c);
    }

    // ✅ Needed for GamePanel image rendering
    public List<Card> getCards() {
        return Collections.unmodifiableList(hand);
    }

    public boolean isBust() {
        return minTotal() > 21;
    }

    // Best total <= 21 if possible; otherwise smallest total (> 21)
    public int bestTotal() {
        int best = -1;
        for (int t : allTotals()) {
            if (t <= 21 && t > best) best = t;
        }
        return (best != -1) ? best : minTotal();
    }

    // Soft 17 means: total = 17 and at least one Ace counted as 11
    public boolean isSoft17() {
        for (TotalInfo info : allTotalInfos()) {
            if (info.total == 17 && info.usedAceAsEleven) return true;
        }
        return false;
    }

    public String handText(boolean hideFirst) {
        if (hand.isEmpty()) return "(empty)";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            if (i == 0 && hideFirst) sb.append("[HIDDEN]");
            else sb.append(hand.get(i).toString());
            if (i < hand.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    // -------- Blackjack totals logic --------

    private int minTotal() {
        int sum = 0;
        for (Card c : hand) sum += minValue(c);
        return sum;
    }

    private List<Integer> allTotals() {
        List<Integer> totals = new ArrayList<>();
        totals.add(0);

        for (Card c : hand) {
            List<Integer> next = new ArrayList<>();
            int[] vals = values(c); // Ace => {1,11}, others => single value
            for (int t : totals) {
                for (int v : vals) next.add(t + v);
            }
            totals = next;
        }

        // Remove duplicates
        List<Integer> unique = new ArrayList<>();
        for (int t : totals) if (!unique.contains(t)) unique.add(t);
        return unique;
    }

    private List<TotalInfo> allTotalInfos() {
        List<TotalInfo> infos = new ArrayList<>();
        infos.add(new TotalInfo(0, false));

        for (Card c : hand) {
            List<TotalInfo> next = new ArrayList<>();
            int[] vals = values(c);

            for (TotalInfo info : infos) {
                for (int v : vals) {
                    boolean usedAce11 = info.usedAceAsEleven;
                    if (c.isAce() && v == 11) usedAce11 = true;
                    next.add(new TotalInfo(info.total + v, usedAce11));
                }
            }
            infos = next;
        }

        // Remove duplicates (total + ace11 flag)
        List<TotalInfo> unique = new ArrayList<>();
        for (TotalInfo t : infos) {
            boolean exists = false;
            for (TotalInfo u : unique) {
                if (u.total == t.total && u.usedAceAsEleven == t.usedAceAsEleven) {
                    exists = true;
                    break;
                }
            }
            if (!exists) unique.add(t);
        }
        return unique;
    }

    private int[] values(Card c) {
        if (c.isAce()) return new int[] {1, 11};
        return new int[] { c.baseValue() }; // J/Q/K => 10, 2..10 => value
    }

    private int minValue(Card c) {
        int[] v = values(c);
        int min = v[0];
        for (int x : v) if (x < min) min = x;
        return min;
    }

    public abstract boolean isComputer();

    private static final class TotalInfo {
        final int total;
        final boolean usedAceAsEleven;

        TotalInfo(int total, boolean usedAceAsEleven) {
            this.total = total;
            this.usedAceAsEleven = usedAceAsEleven;
        }
    }
}


