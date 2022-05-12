package client;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/*
CE303
Mason Knott - 1801459
 */

public class UI {
    private final ClientController clientController;
    private final JFrame jFrame;
    private final JPanel mainPanel;
    private final JPanel playersPanel;
    private final JLabel playerIDLabel;
    private final JTextArea gameStatus;

    public UI(ClientController clientController) {
        this.clientController = clientController;
        jFrame = new JFrame();
        mainPanel = new JPanel(new BorderLayout());
        playersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        String labelString = "<html><h1><font color=white>You are player </font><font color=orange>" + clientController.getPlayerID() + "</font></h1></html>";
        playerIDLabel = new JLabel(labelString, SwingConstants.CENTER);
        gameStatus = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(gameStatus);

        scrollPane.setPreferredSize(new Dimension(0, 100));

        DefaultCaret caret = (DefaultCaret) gameStatus.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        // this will enable auto scroll

        gameStatus.setEditable(false);

        mainPanel.setBackground(Color.black);
        playersPanel.setBackground(Color.black);
        gameStatus.setBackground(Color.black);
        gameStatus.setForeground(Color.white);

        jFrame.add(mainPanel);
        mainPanel.add(playersPanel, BorderLayout.CENTER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);
        mainPanel.add(playerIDLabel, BorderLayout.NORTH);

        jFrame.setSize(550, 660);
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);

        jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        GameState gamestate = clientController.getGameState();

        jFrame.setTitle("CE303 1801459 - Now playing as player " + clientController.getPlayerID());

        addEventText("--- Current players: " + gamestate.getPlayers().size() + " ---");

        for (Integer playerID : gamestate.getPlayers()) {
            boolean playerHasBall = clientController.getPlayerWithBall() == playerID;
            addPlayerPanel(playerID, playerHasBall);
        }

        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
                // terminates upon clicking close
            }
        });
    }

    public static void main(String[] args) {
        try {
            CountDownLatch latch = new CountDownLatch(1);
            // latch used as to wait for game state which is to be used by client listener

            ClientController clientController = new ClientController(latch);
            new Thread(new ClientListener(clientController)).start();
            // once latch is done, new instance of client listener
            try {
                latch.await();
            } catch (InterruptedException e) {
                System.out.println("--- Countdown latch interrupted ---");
            }
            SwingUtilities.invokeLater(() -> {
                UI UI = new UI(clientController);
                clientController.setUI(UI);
            });
        } catch (IOException e) {
            System.out.println("--- Couldn't connect to server ---");
        }
    }

    void addEventText(String text) {
        SwingUtilities.invokeLater(() -> gameStatus.append(text + "\n"));

    }

    void addPlayerPanel(int id, boolean hasBall) {
        if (!playerPanelExists(id)) {
            playersPanel.add(new PlayerPanel(id, hasBall, clientController));
            playersPanel.repaint();
            playersPanel.revalidate();
            // repaint and revalidate to update UI for user
        }
    }

    private boolean playerPanelExists(int id) {
        for (Component component : playersPanel.getComponents()) {
            if (component.getClass() == PlayerPanel.class) {
                PlayerPanel panel = (PlayerPanel) component;
                if (panel.getID() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    void removePlayerPanel(int id) {
        // simply remove panels with specified ids
        for (Component component : playersPanel.getComponents()) {
            if (component.getClass() == PlayerPanel.class) {
                PlayerPanel panel = (PlayerPanel) component;
                if (panel.getID() == id) {
                    playersPanel.remove(component);
                    playersPanel.repaint();
                    playersPanel.revalidate();
                }
            }
        }
    }

    void setPlayerWithBall(int newBallID) {
        for (Component component : playersPanel.getComponents()) {
            if (component.getClass() == PlayerPanel.class) {
                PlayerPanel panel = (PlayerPanel) component;
                panel.setColoursOnBallPossession(panel.getID() == newBallID);
            }
        }
    }

    void setAllButtonsEnabled(boolean enabled) {
        for (Component component : playersPanel.getComponents()) {
            if (component.getClass() == PlayerPanel.class) {
                PlayerPanel panel = (PlayerPanel) component;
                panel.setButtonEnabled(enabled);
            }
        }
    }
}
