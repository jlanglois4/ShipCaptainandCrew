package edu.wctc;

import java.util.*;
import java.util.stream.Collectors;

public class DiceGame {

    private final List<Player> players;
    private final List<Die> dice;
    private final int maxRolls;
    private Player currentPlayer;
    private int turnCounter = 0;


    /*Constructor that initializes all final instance fields.
    Creates the required number of Player objects and Die objects and adds them to the appropriate lists.
    If the number of players is less than 2, throws an IllegalArgumentException*/
    public DiceGame(int countPlayers, int countDice, int maxRolls) {
        players = new ArrayList<>();

        dice = new ArrayList<>();
        this.maxRolls = maxRolls;

        if (countPlayers < 2) {
            throw new IllegalArgumentException("Enter at least two players.");
        }

        for (int i = 0; i < countPlayers; i++) {
            players.add(new Player());
        }

        for (int i = 0; i < countDice; i++) {
            dice.add(new Die(6));
        }

        currentPlayer = players.get(0);
    }


    //return true of all dice are held, false otherwise
    private boolean allDiceHeld() {
        return dice.stream().allMatch(Die::isBeingHeld);
    }


    /*If there is no die with the given face value, return false.
    If there already is a die with the given face value that is held, just return true.
    If there is a die with the given face value that is unheld, hold it and return true. (If there are multiple matches, only hold one of them.)*/
    public boolean autoHold(int faceValue) {
        if (!(dice.stream().anyMatch(n -> n.getFaceValue() == faceValue))) {
            return false;
        }

        if (dice.stream().filter(n -> n.isBeingHeld()).anyMatch(n -> n.getDieNum() == faceValue)) {
            return true;
        }

        for (int i = 0; i < dice.size(); i++) {

            if (dice.get(i).getFaceValue() == faceValue) {
                dice.get(i).holdDie();
                return true;
            }
        }
        return false;
    }


    //Returns true if the current player has any rolls remaining and if not all dice are held.
    public boolean currentPlayerCanRoll() {
        return currentPlayer.getRollsUsed() < maxRolls;
    }


    //Returns current player's number
    public int getCurrentPlayerNumber() {
        return currentPlayer.getPlayerNumber();
    }


    //Returns current player's score
    public int getCurrentPlayerScore() {
        return currentPlayer.getScore();
    }


    //Resets a string composed by concatenating each Die's toString.
    public String getDiceResults() {
        return dice.stream().map(Die::toString).collect(Collectors.joining());
    }


    //Finds the player with the most wins and returns its toString.
    public String getFinalWinner() {
        return players.stream().max(Comparator.comparingInt(Player::getWins))
                .toString().replaceAll("Optional", "")
                .replaceAll("\\[", "").replaceAll("]", "");
    }


    /*Sorts the player list field by score, highest to lowest.
    Awards each player that earned the highest score a win and all others a loss.
    Returns a string composed by concatenating each Player's toString.*/
    public String getGameResults() {
        Comparator<Player> comparator = Comparator.comparingInt(Player::getScore).reversed();
        Collections.sort(players, comparator);

        if (players.get(0).getScore() == players.stream().mapToInt(Player::getScore).max().orElse(0)) {
            players.get(0).addWin();
        }

        players.stream().skip(1).forEach(player -> player.addLoss());

        return players.stream().map(Player::toString).collect(Collectors.joining("\n"));
    }


    //Returns true if there is any held die with a matching face value, false otherwise.
    private boolean isHoldingDie(int faceValue) {
        return dice.stream().anyMatch(die -> die.getFaceValue() == faceValue);
    }


    //If there are more players in the list after the current player, updates currentPlayer to be the next player and returns true. Otherwise, returns false.
    public boolean nextPlayer() {
        if (turnCounter + 1 < players.size()) {
            currentPlayer = players.get(turnCounter + 1);
            turnCounter++;
            return true;
        }
        return false;
    }


    //Finds the die with the given die number (NOT the face value) and holds it.
    public void playerHold(char dieNum) {
        dice.stream().filter(die -> die.getDieNum() == dieNum).toList().get(0).holdDie();
    }


    //Resets each die.
    public void resetDice() {
        dice.stream().forEach(die -> die.resetDie());
    }


    //Resets each player.
    public void resetPlayers() {
        players.stream().forEach(player -> player.resetPlayer());
    }


    //Logs the roll for the current player, then rolls each die.
    public void rollDice() {
        currentPlayer.roll();
        dice.forEach(Die::rollDie);
    }

    /* If there is currently a ship (6), captain (5), and crew (4) die held,
     adds the points for the remaining two dice (the cargo) to the current player's score.
     If there is not a 6, 5, and 4 held, assigns no points.*/
    public void scoreCurrentPlayer() {
        boolean six = dice.stream().anyMatch(die -> die.getFaceValue() == 6);
        boolean five = dice.stream().anyMatch(die -> die.getFaceValue() == 5);
        boolean four = dice.stream().anyMatch(die -> die.getFaceValue() == 4);

        if (six && five && four) {
            int score = dice.stream().mapToInt(Die::getFaceValue).sum();
            score = score - 15;
            currentPlayer.setScore(score);
        }
    }


    /*Assigns the first player in the list as the current player.
      (The list will still be sorted by score from the previous round, so winner will end up going first.)*/
    public void startNewGame() {
        currentPlayer = players.get(0);
        turnCounter = 0;
        resetPlayers();
    }
}
