package server;

import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/*
CE303
Mason Knott - 1801459
 */

public class GameController {

    private static AtomicInteger currentClientID;
    private final List<PlayerObject> playerList;
    private final List<PlayerObject> rejoinList;
    private final CountDownLatch latch;

    public GameController(CountDownLatch latch) {
        currentClientID = new AtomicInteger(1);
        // atomically incremented integer when client joins server
        playerList = Collections.synchronizedList(new ArrayList<>());
        rejoinList = Collections.synchronizedList(new ArrayList<>());
        // thread safe lists

        this.latch = latch;
    }

    public void processGameStateArgs(String gameState) {

        String[] gameStateArgs = gameState.split(" ");
        String[] currentPlayers = gameStateArgs[0].split("/");
        int playerWithBall = Integer.parseInt(gameStateArgs[1]);
        // first arg are player ids e.g. "1/2/3", second arg is player id with ball

        for (String playerID : currentPlayers) {
            int playerIDInt = Integer.parseInt(playerID);

            if (playerIDInt >= currentClientID.get()) {
                currentClientID.set(playerIDInt + 1);
            }

            PlayerObject playerObject = new PlayerObject(playerIDInt, playerIDInt == playerWithBall, null, null);
            // create new player instance
            rejoinList.add(playerObject);
            latch.countDown();
            System.out.println("### Launching game with this game state: " + gameState + " ###");

            // time out TimerTask to time 2 seconds - reader/writer instances are checked. if not set
            // then the player will be removed from the game.
            TimerTask timeOut = new TimerTask() {
                @Override
                public void run() {
                    if (playerObject.getWriter() == null || playerObject.getScanner() == null) {
                        rejoinList.remove(playerObject);
                        // remove player from thread safe list
                        System.out.println("### Player ID:  " + playerObject.getID() + " did not reconnect in time. ###");
                        sendMsgToEveryClient("(player_left) " + playerObject.getID());
                        if (playerObject.holdsBall()) {
                            if (!playerList.isEmpty()) {
                                // if current player holds the ball and other players exist
                                giveBall(playerObject, playerList.get(0), false);
                                // give the ball to first player in list
                            }
                        }
                    }
                }
            };

            new Timer("timer").schedule(timeOut, 2000L);
        }
    }

    private void sendMsgToEveryClient(String msg) {
        synchronized (playerList) {
            for (PlayerObject playerObject : playerList) {
                playerObject.getWriter().println(msg);
            }
        }
    }

    private void sendErrorMsgToClient(String errorMessage, PrintWriter writer) {
        writer.println("(error) " + errorMessage);
    }

    private void newPlayerMessage(PlayerObject newPlayerObject) {
        synchronized (playerList) {
            for (PlayerObject otherPlayerObject : playerList) {
                if (!otherPlayerObject.idEquals(newPlayerObject)) {
                    // select all players that aren't the new player
                    otherPlayerObject.getWriter().println("(player_join) " + newPlayerObject.getID());
                    // grab writer instance and write out to clients of new join alert
                }
            }
        }
    }

    public PlayerObject getPlayer(int id) {
        // as there are two lists of players, have to iterate through both to find unique player
        synchronized (rejoinList) {
            synchronized (playerList) {
                for (PlayerObject playerObject : rejoinList) {
                    if (playerObject.getID() == id) {
                        return playerObject;
                    }
                }
                for (PlayerObject playerObject : playerList) {
                    if (playerObject.getID() == id) {
                        return playerObject;
                    }
                }
                return null;
            }
        }
    }

    public void giveBall(PlayerObject from, PlayerObject to, boolean printMessage) {
        // this method gets triggered when (send_ball) is is called client side
        if (!from.holdsBall()) {
            // if player doesnt have the ball
            sendErrorMsgToClient("Can't give the ball since you do not have it, player " + getPlayerHoldingBall().getID() + " has the ball!)", from.getWriter());
        } else if (!playerList.contains(to)) {
            // if player sending to is no longer active in the game
            from.setHoldsBall(true);
            sendErrorMsgToClient("This player has left the game, can't give the ball!", from.getWriter());
        } else {
            from.setHoldsBall(false);
            to.setHoldsBall(true);
            sendMsgToEveryClient("(give_ball) " + to.getID());
            if (printMessage) {
                System.out.println("### Ball passed: player " + from.getID() + " to player " + to.getID() + " ###");
            }
        }
    }

    public PlayerObject getPlayerHoldingBall() {
        synchronized (rejoinList) {
            synchronized (playerList) {
                for (PlayerObject playerObject : playerList) {
                    if (playerObject.holdsBall()) {
                        return playerObject;
                    }
                }

                for (PlayerObject playerObject : rejoinList) {
                    if (playerObject.holdsBall()) {
                        return playerObject;
                    }
                }
                return null;
            }
        }
    }


    public void addPlayer(PlayerObject playerObject) {
        if (!playerList.contains(playerObject)) {
            playerList.add(playerObject);
            newPlayerMessage(playerObject);
            showPlayers();
        }
    }

    public void removePlayer(PlayerObject leavingPlayerObject) {
        synchronized (playerList) {
            playerList.remove(leavingPlayerObject);
            sendMsgToEveryClient("(player_left) " + leavingPlayerObject.getID());
            if (leavingPlayerObject.holdsBall() && !isEmpty()) {
                //if the player who left had the ball give it to someone else
                PlayerObject newBallHolder = playerList.get(0);
                System.out.println("### " + leavingPlayerObject.getID() + " left with ball.\n### Ball now passed to: " + newBallHolder.getID() + " ###");
                giveBall(leavingPlayerObject, newBallHolder, false);
            } else if (leavingPlayerObject.holdsBall()) {
                System.out.println("### Last player has left the game. Awaiting connections. ###");
            }
            if (!isEmpty()) {
                showPlayers();
            }
        }
    }

    public String getPlayersString() {
        if (isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        synchronized (playerList) {
            for (PlayerObject playerObject : playerList) {
                builder.append(playerObject.getID());
                builder.append("-");
            }
        }
        // builds string for output purposes, deletes last comma appended
        return builder.deleteCharAt(builder.lastIndexOf("-")).toString();
    }

    public void showPlayers() {
        System.out.println("### Current players: " + getPlayersString() + " ###");
    }

    public boolean isEmpty() {
        return playerList.isEmpty();
    }

    public int getNewClientID() {
        return currentClientID.getAndIncrement();
    }


}
