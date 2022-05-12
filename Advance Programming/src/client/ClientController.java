package client;

import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

/*
CE303
Mason Knott - 1801459
 */

public class ClientController implements AutoCloseable {
    private final List<Integer> players = Collections.synchronizedList(new ArrayList<>());
    private final CountDownLatch receivedLatch;
    private final int playerID;
    private final Scanner r;
    private final PrintWriter w;
    private UI ui;
    private int playerWithBall;

    public ClientController(CountDownLatch latch) throws IOException {

        Socket socket = new Socket(ClientConstants.DOMAIN, ClientConstants.PORT);
        r = new Scanner(socket.getInputStream());
        w = new PrintWriter(socket.getOutputStream(), true);

        //write a blank line to skip rejoin info on server
        w.println("");

        //Can get ID here as we know it is going to be the first message from the server.
        String connectionResponse = r.nextLine();
        String playerID = connectionResponse.split(" ")[1];

        this.playerID = Integer.parseInt(playerID);

        receivedLatch = latch;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public int getPlayerWithBall() {
        return playerWithBall;
    }

    public GameState getGameState() {
        return new GameState(players, playerWithBall);
    }

    public void setGameState(String playersString, String playerWithBall) {
        if (players.isEmpty()) {
            String[] players = playersString.split("-");
            // if players list is empty, using players string populate players thread safe array
            for (String s : players) {
                this.players.add(Integer.parseInt(s));
            }
            this.playerWithBall = Integer.parseInt(playerWithBall);
            receivedLatch.countDown();
            // continue
        }
    }

    public void setUI(UI ui) {
        this.ui = ui;
    }

    public boolean hasBall() {
        return playerID == playerWithBall;
    }

    private void sendMsgToServer(String message) {
        w.println(message);
    }

    public void giveBallTo(int playerID) {
        sendMsgToServer("(give_ball) " + playerID);
    }

    public void showErrorToClient(String error) {
        SwingUtilities.invokeLater(() -> ui.addEventText(error));
    }

    public void addNewPlayer(String playerIDString) {
        // when (player_join) is received
        int playerId = Integer.parseInt(playerIDString);

        if (!players.contains(playerId)) {
            String outputMessage = "--- New player: " + playerId + " ---";
            System.out.println(outputMessage);
            players.add(playerId);
            // adds player to this instance's array

            if (ui != null) {
                // updates the ui
                SwingUtilities.invokeLater(() -> {
                    ui.addPlayerPanel(playerId, playerId == playerWithBall);
                    ui.addEventText(outputMessage);
                });
            }
        }
    }

    public void removePlayer(String leavingPlayerIDString) {
        Integer playerId = Integer.parseInt(leavingPlayerIDString);

        if (players.contains(playerId)) {
            // if player exists
            SwingUtilities.invokeLater(() -> ui.removePlayerPanel(playerId));
            players.remove(playerId);
            // remove player
            ui.addEventText("--- Player " + playerId + " has left. ---");
        }
    }

    public void newBallPosition(int newBallID) {
        SwingUtilities.invokeLater(() -> ui.setPlayerWithBall(newBallID));
        // this will update the label on ui

        if (newBallID == this.playerID) {
            SwingUtilities.invokeLater(() -> {
                ui.setAllButtonsEnabled(true);
                ui.addEventText("--- You have the ball! ---");
            });
        } else {
            SwingUtilities.invokeLater(() -> {
                ui.setAllButtonsEnabled(false);
                ui.addEventText("--- Player " + newBallID + " has the ball. ---");
            });
        }
        playerWithBall = newBallID;
    }

    public Scanner getReader() {
        return this.r;
    }

    @Override
    public void close() {
        r.close();
        w.close();
    }
}
