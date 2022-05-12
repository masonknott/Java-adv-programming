package client;

import java.util.List;

/*
CE303
Mason Knott - 1801459
 */

public class GameState {
    private final List<Integer> players;
    private final int playerWithBall;

    public GameState(List<Integer> players, int playerWithBall) {
        this.players = players;
        this.playerWithBall = playerWithBall;
    }

    public List<Integer> getPlayers() {
        return players;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Integer playerID : players) {
            builder.append(playerID);
            builder.append("-");
        }
        builder.deleteCharAt(builder.lastIndexOf("-"));

        builder.append(" ");
        builder.append(playerWithBall);
        return builder.toString();
    }
}