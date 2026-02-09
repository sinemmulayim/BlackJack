package model;

public class Card {

    // Suit of the card (4 standard suits)
    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES
    }

    // Rank of the card (A, 2..10, J, Q, K)
    public enum Rank {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN,
        JACK, QUEEN, KING
    }

    // Core attributes of a card
    private final Suit suit;
    private final Rank rank;

    // Optional fields to match the project requirement examples:
    // - imagePath: location of the card image file (if you want to display images)
    // - ability: special ability text/tag (optional, used in card battle style games)
    private final String imagePath;
    private final String ability;

    // Basic constructor (kept so existing Deck code still works without changes)
    public Card(Suit suit, Rank rank) {
        this(suit, rank, null, null);
    }

    // Extended constructor if you want to attach an image or ability to the card
    public Card(Suit suit, Rank rank, String imagePath, String ability) {
        if (suit == null || rank == null) {
            throw new IllegalArgumentException("Suit/Rank cannot be null");
        }
        this.suit = suit;
        this.rank = rank;
        this.imagePath = imagePath;
        this.ability = ability;
    }

    // Getter-like methods (simple and clean)
    public Suit suit() {
        return suit;
    }

    public Rank rank() {
        return rank;
    }

    public String imagePath() {
        return imagePath;
    }

    public String ability() {
        return ability;
    }

    // Returns true if this card is an Ace
    public boolean isAce() {
        return rank == Rank.ACE;
    }

    // Base Blackjack value:
    // - J/Q/K -> 10
    // - A -> 1  (Player.bestTotal() can treat it as 11 when possible)
    // - 2..10 -> face value
    public int baseValue() {
        switch (rank) {
            case JACK:
            case QUEEN:
            case KING:
                return 10;
            case ACE:
                return 1;
            case TWO:
                return 2;
            case THREE:
                return 3;
            case FOUR:
                return 4;
            case FIVE:
                return 5;
            case SIX:
                return 6;
            case SEVEN:
                return 7;
            case EIGHT:
                return 8;
            case NINE:
                return 9;
            case TEN:
                return 10;
            default:
                return 0;
        }
    }

    public String resolvedImagePath() {
        // If an explicit imagePath exists, use it
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            return imagePath;
        }

        // Build filename like: 8h.jpg, 10c.jpg, 11d.jpg ...
        int n = rankToNumber(rank);
        char s = suitToLetter(suit);

        return "/images/cards/" + n + s + ".jpg";
    }

    private int rankToNumber(Rank r) {
        switch (r) {
            case ACE:   return 1;
            case TWO:   return 2;
            case THREE: return 3;
            case FOUR:  return 4;
            case FIVE:  return 5;
            case SIX:   return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE:  return 9;
            case TEN:   return 10;
            case JACK:  return 11;
            case QUEEN: return 12;
            case KING:  return 13;
            default:    return 0;
        }
    }

    private char suitToLetter(Suit s) {
        switch (s) {
            case CLUBS:    return 'c';
            case DIAMONDS: return 'd';
            case HEARTS:   return 'h';
            case SPADES:   return 's';
            default:       return 'x';
        }
    }

    // String representation for debugging / text-based UI
    @Override
    public String toString() {
        return rank + " of " + suit;
    }
}


