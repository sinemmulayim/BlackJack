package model;

public class ComputerPlayer extends Player {

    public ComputerPlayer(String name) {
        super(name);
    }

    /**
     * Standard Blackjack dealer rule:
     * - Hit if total < 17
     * - Stand if total >= 17
     */
    public boolean shouldHit() {
        return bestTotal() < 17;
    }

    @Override
    public boolean isComputer() {
        return true;
    }
}


