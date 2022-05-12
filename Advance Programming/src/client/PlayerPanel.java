package client;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

class PlayerPanel extends JPanel {
    private final JLabel playerLabel;
    private final JLabel holdsBallLabel;
    private final JButton giveBallButton;
    private final int playerID;
    private final ClientController clientController;
    Color color;

    // JPanel class for individual player panel instances
    PlayerPanel(int id, boolean playerHasBall, ClientController clientController) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.clientController = clientController;
        this.playerID = id;

        playerLabel = new JLabel("", SwingConstants.CENTER);
        holdsBallLabel = new JLabel("", SwingConstants.CENTER);
        giveBallButton = new JButton("Give Ball!");

        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        holdsBallLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        giveBallButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        playerLabel.setFont(playerLabel.getFont().deriveFont(16f));
        holdsBallLabel.setFont(holdsBallLabel.getFont().deriveFont(16f));

        giveBallButton.setEnabled(clientController.hasBall());
        setColoursOnBallPossession(playerHasBall);

        color = chooseColour();

        String playerIDString;
        if (playerID == clientController.getPlayerID()) {
            playerIDString = "orange>" + playerID;
        } else {
            playerIDString = "blue>" + playerID;
        }

        playerLabel.setText("<html><p align=\"center\">Player:<font color=" + playerIDString + "</font></p></html>");

        add(Box.createHorizontalStrut(1));
        add(playerLabel);
        add(Box.createHorizontalStrut(1));
        add(holdsBallLabel);
        add(Box.createHorizontalStrut(1));
        add(giveBallButton);
        add(Box.createHorizontalStrut(1));


        setPreferredSize(new Dimension(100, 130));
        repaint();
        revalidate();

        giveBallButton.addActionListener((event) -> {
            clientController.giveBallTo(playerID);
        });

    }

    int getID() {
        return this.playerID;
    }

    void setButtonEnabled(boolean enabled) {
        giveBallButton.setEnabled(enabled);
    }

    void setColoursOnBallPossession(boolean hasBall) {
        if (hasBall) {
            holdsBallLabel.setText("Has Ball!");
            holdsBallLabel.setForeground(Color.green);
        } else {
            holdsBallLabel.setText("No Ball");
            holdsBallLabel.setForeground(Color.darkGray);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // overridden paint component to paint the panels in desired format
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(color);
        graphics.fillRoundRect(0, 0, width, height, 0, 0);
    }

    private Color chooseColour() {
        ArrayList<Color> selectedColours = new ArrayList<>();
        // selected a few colours that shouldn't have problems with contrasting foreground text
        selectedColours.add(new Color(102, 102, 153));
        selectedColours.add(new Color(102, 153, 153));
        selectedColours.add(new Color(153, 153, 102));
        selectedColours.add(new Color(153, 204, 255));
        selectedColours.add(new Color(255, 204, 204));
        selectedColours.add(new Color(204, 255, 204));
        selectedColours.add(new Color(204, 255, 0));
        selectedColours.add(new Color(153, 102, 51));
        selectedColours.add(new Color(204, 102, 0));

        return selectedColours.get(new Random().nextInt(selectedColours.size()));
    }
}