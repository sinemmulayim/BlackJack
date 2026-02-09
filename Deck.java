package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private static final int NUM_DECKS = 4;

    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        reset();
        shuffle();
    }

    public void reset() {
        cards.clear();

        for (int d = 0; d < NUM_DECKS; d++) {          // 🔥 4 deste
            for (Card.Suit s : Card.Suit.values()) {
                for (Card.Rank r : Card.Rank.values()) {
                    cards.add(new Card(s, r));
                }
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public int remaining() {
        return cards.size();
    }

    public Card draw() {
        if (cards.isEmpty())
            throw new IllegalStateException("Deck is empty");
        return cards.remove(cards.size() - 1);
    }
}

