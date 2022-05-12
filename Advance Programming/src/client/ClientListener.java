package client;

import java.util.NoSuchElementException;
import java.util.Scanner;

/*
CE303
Mason Knott - 1801459
 */

public class ClientListener implements Runnable {

    private final ClientController clientController;

    public ClientListener(ClientController clientController) {
        this.clientController = clientController;
    }


    @Override
    public void run() {
        Scanner reader = clientController.getReader();

        while (true) {
            try {
                String line = reader.nextLine();
                String[] substrings = line.split(" ");
                String command = substrings[0];
                String firstParameter = substrings[1];

                switch (command) {
                    case "(game_state)" -> {
                        String players = substrings[1];
                        String playerWithBall = substrings[2];
                        clientController.setGameState(players, playerWithBall);
                    }
                    case "(give_ball)" -> clientController.newBallPosition(Integer.parseInt(firstParameter));
                    case "(player_join)" -> clientController.addNewPlayer(firstParameter);
                    case "(player_left)" -> clientController.removePlayer(firstParameter);
                    case "(error)" -> clientController.showErrorToClient(line);
                }
            }
            // thrown when connection drops out
            catch (NoSuchElementException e) {
                System.out.println("--- Server disconnected. ---");
                return;
            }
        }
    }
}
