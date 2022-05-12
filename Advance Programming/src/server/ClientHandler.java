package server;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/*
CE303
Mason Knott - 1801459
 */

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameController gameController;

    public ClientHandler(Socket socket, GameController gameController) {
        this.socket = socket;
        this.gameController = gameController;

    }

    @Override
    public void run() {
        try {
            PlayerObject playerObject;
            Scanner s = new Scanner(socket.getInputStream());
            PrintWriter w = new PrintWriter(socket.getOutputStream(), true);

            // otherwise create a NEW player instance with new ID
            playerObject = new PlayerObject(gameController.getNewClientID(), gameController.isEmpty(), s, w);
            gameController.addPlayer(playerObject);

            if (gameController.isEmpty()) {
                System.out.println("### Client connected ID: " + playerObject.getID() + ", ball has been passed ###");
            } else {
                System.out.println("### Client connected ID: " + playerObject.getID() + " ###");
            }


            w.println("(assign_id) " + playerObject.getID());
            w.println("(game_state) " + gameController.getPlayersString() + " " + gameController.getPlayerHoldingBall().getID());
            // write to client their assigned ID and game state

            while (true) {
                try {
                    String line = s.nextLine();
                    String[] substrings = line.split(" ");

                    String command = substrings[0];

                    if ("(give_ball)".equals(command)) {
                        PlayerObject playerObjectGettingBall = gameController.getPlayer(Integer.parseInt(substrings[1]));

                        if (playerObjectGettingBall != null) {
                            gameController.giveBall(playerObject, playerObjectGettingBall, true);
                        }
                    }
                } catch (NoSuchElementException e) {
                    // when client dc's this is thrown
                    System.out.println("### Client ID: " + playerObject.getID() + " has disconnected ###");
                    gameController.removePlayer(playerObject);
                    socket.close();
                    break;
                }
            }
        } catch (IOException ignored) {

        }
    }
}
