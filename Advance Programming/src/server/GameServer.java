package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/*
CE303
Mason Knott - 1801459
 */

public class GameServer {

    private static final CountDownLatch latch = new CountDownLatch(1);
    private static final GameController GAME_CONTROLLER = new GameController(latch);

    public static void main(String[] args) {
        runServer(args);
    }

    private static void runServer(String[] args) {
        ServerSocket serverSocket;
        if (args.length > 0) {
            GAME_CONTROLLER.processGameStateArgs(args[0]);
        } else {
            latch.countDown();
        }
        try {
            latch.await();
            // waits for latch to hit zero
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            serverSocket = new ServerSocket(ServerConstants.PORT);
            System.out.println("### Server has started. Waiting for connections ###");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new ClientHandler(socket, GAME_CONTROLLER)).start();
                // creates new thread with new client handler instance and calls start method when new client joins
            }
        } catch (IOException e) {
            System.out.println("### Port is being used. Please try another port ###");
            System.exit(1);
        }
    }
}
