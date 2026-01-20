package MainProgram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import sockets.SuperSocketMaster;

public class testcodenames3 implements ActionListener {

    // Properties
    JFrame theFrame;
    JPanel mainPanel, boardPanel, leftPanel, rightPanel, bottomPanel;
    JButton[][] wordButtons;
    JButton btnEndTurn, btnToggleOverlay, btnGiveClue;
    JTextField chatInput;
    JTextArea gameLog;

    final int intROWS = 5;
    final int intCOLS = 5;

    String[][] strWords = new String[5][5];
    String[][] strColors = new String[5][5];
    boolean[][] blnRevealed = new boolean[5][5];

    String strMyRole = "OPERATIVE";
    String strMyTeam = "RED";
    String strCurrentTurn = "RED";

    int intRedLeft = 9;
    int intBlueLeft = 8;

    // GUI Labels
    JLabel lblTurnBox;
    JLabel lblHint;
    JLabel lblHintNumber;
    JLabel lblRedCount;
    JLabel lblBlueCount;
    JLabel lblTimer;

    ArrayList<String> strWordPool = new ArrayList<>();

    // Online
    SuperSocketMaster ssm;
    boolean blnIsServer = false;
    String strServerIP = "";
    JTextField chatField;

    // Overlay
    boolean blnOverlayOn = true;

    // Timer
    javax.swing.Timer turnTimer;
    int intTimeLeft = 60;

    boolean blnGameStarted = false;

    // Animation
    Color[][] targetColors = new Color[5][5];
    Color[][] currentColors = new Color[5][5];

    int intDisplayedRed = intRedLeft;
    int intDisplayedBlue = intBlueLeft;

    javax.swing.Timer animationTimer;

    final int intFPS = 60;
    final int intANIM_DURATION = 300;
    final float fltCOLOR_STEP = 1.0f / ((intANIM_DURATION / 1000f) * intFPS);

    // Labels
    String strActorLabel() {
        return strMyTeam + " " + (strMyRole.equals("SPYMASTER") ? "Spymaster" : "Operative");
    }

    String strOperativeLabel() {
        return strCurrentTurn + " Operative";
    }

    String strSpymasterLabel() {
        return strCurrentTurn + " Spymaster";
    }

    @Override
    public void actionPerformed(ActionEvent evt) {

        if (evt.getSource() == btnToggleOverlay) {
            blnOverlayOn = !blnOverlayOn;
            updateTargetColors();
            return;
        }

        if (!blnGameStarted) return;

        if (evt.getSource() == btnEndTurn) {
            endTurn();
            return;
        }

        if (evt.getSource() == btnGiveClue) {
            if (strMyRole.equals("SPYMASTER") && strMyTeam.equals(strCurrentTurn)) {
                giveClue();
            } else {
                JOptionPane.showMessageDialog(theFrame, "Only the spymaster of the current team can give clues.");
            }
            return;
        }

        if (!strMyRole.equals("OPERATIVE") || !strMyTeam.equals(strCurrentTurn)) return;

        for (int intCount = 0; intCount < 5; intCount++) {
            for (int intCount2 = 0; intCount2 < 5; intCount2++) {
                if (evt.getSource() == wordButtons[intCount][intCount2] && !blnRevealed[intCount][intCount2]) {
                    blnRevealed[intCount][intCount2] = true;

                    updateTargetColors();

                    wordButtons[intCount][intCount2].setEnabled(false);
                    sendNetwork("CLICK:" + intCount + ":" + intCount2);

                    log(strActorLabel() + " taps " + strWords[intCount][intCount2]);

                    if (strColors[intCount][intCount2].equals("BLACK")) {
                        log("ASSASSIN! " + (strCurrentTurn.equals("RED") ? "BLUE" : "RED") + " wins!");
                        JOptionPane.showMessageDialog(theFrame,
                                "ASSASSIN! " + (strCurrentTurn.equals("RED") ? "BLUE" : "RED") + " wins!");
                        System.exit(0);
                    }

                    if (strColors[intCount][intCount2].equals("RED")) intRedLeft--;
                    if (strColors[intCount][intCount2].equals("BLUE")) intBlueLeft--;

                    animateScores();

                    if (intRedLeft == 0 || intBlueLeft == 0) {
                        log((intRedLeft == 0 ? "RED" : "BLUE") + " TEAM WINS!");
                        JOptionPane.showMessageDialog(theFrame,
                                (intRedLeft == 0 ? "RED" : "BLUE") + " TEAM WINS!");
                        System.exit(0);
                    }

                    if (!strColors[intCount][intCount2].equals(strCurrentTurn)) {
                        endTurn();
                    }
                }
            }
        }
    }

    // Animation timer
    public testcodenames3(String strTitle) {
        setupGUI();
        setupAnimationTimer();
    }

    void startTimer() {
        if (lblTimer == null) return;

        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
            intTimeLeft--;
            lblTimer.setText("Time left: " + intTimeLeft);
            if (intTimeLeft <= 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                endTurn();
            }
        });
        if (turnTimer != null) turnTimer.stop();
        turnTimer = t;
        intTimeLeft = 60;
        lblTimer.setText("Time left: " + intTimeLeft);
        turnTimer.start();
    }

    // ======================
    // END TURN
    // ======================
    void endTurn() {
        log(strOperativeLabel() + " ends guessing");

        strCurrentTurn = strCurrentTurn.equals("RED") ? "BLUE" : "RED";
        lblTurnBox.setText(strCurrentTurn + "'s turn");
        lblHint.setText("Waiting for clue...");
        lblHintNumber.setText("");
        intTimeLeft = 60;

        sendNetwork("ENDTURN:" + strCurrentTurn);

        log(strCurrentTurn + " team's turn begins");

        updateTargetColors();
    }

    // ======================
    // GIVE CLUE
    // ======================
    void giveClue() {
        String strClue = JOptionPane.showInputDialog("Enter clue + number (e.g. Animal 2)");
        if (strClue != null && !strClue.isEmpty()) {
            String strWord = strClue;
            String strNumber = "";
            if (strClue.contains(" ")) {
                int intIndex = strClue.lastIndexOf(" ");
                strWord = strClue.substring(0, intIndex).trim();
                strNumber = strClue.substring(intIndex + 1).trim();
            }

            for (int intCount = 0; intCount < 5; intCount++) {
                for (int intCount2 = 0; intCount2 < 5; intCount2++) {
                    if (strWords[intCount][intCount2].equalsIgnoreCase(strWord)) {
                        JOptionPane.showMessageDialog(theFrame,
                                "Invalid clue! You cannot use a word that is already on the board.");
                        return;
                    }
                }
            }

            lblHint.setText(strWord);
            lblHintNumber.setText(strNumber);

            sendNetwork("CLUE:" + strWord + ":" + strNumber);

            log(strSpymasterLabel() + " gives clue " + strWord + " " + strNumber);
        }
    }

    void setupLobby() {
        JDialog lobby = new JDialog(theFrame, "Lobby", true);
        lobby.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("Lobby - Choose role & team"));

        JPanel hostPanel = new JPanel();
        JRadioButton rbHost = new JRadioButton("Host");
        JRadioButton rbJoin = new JRadioButton("Join");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbHost);
        bg.add(rbJoin);
        rbHost.setSelected(true);

        JTextField ipInput = new JTextField("127.0.0.1", 10);

        hostPanel.add(rbHost);
        hostPanel.add(rbJoin);
        hostPanel.add(new JLabel("IP:"));
        hostPanel.add(ipInput);

        top.add(hostPanel);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> lobbyList = new JList<>(listModel);
        lobbyList.setPreferredSize(new Dimension(250, 200));

        listModel.addElement("Red Operative");
        listModel.addElement("Red Spymaster");
        listModel.addElement("Blue Operative");
        listModel.addElement("Blue Spymaster");

        JButton btnStart = new JButton("Start Game");
        btnStart.addActionListener(e -> {
            if (lobbyList.getSelectedValue() == null) return;

            blnIsServer = rbHost.isSelected();
            strServerIP = ipInput.getText().trim();

            String strSelection = lobbyList.getSelectedValue();
            strMyTeam = strSelection.contains("Red") ? "RED" : "BLUE";
            strMyRole = strSelection.contains("Spymaster") ? "SPYMASTER" : "OPERATIVE";

            lobby.dispose();

            setupSocket();

            if (blnIsServer) {
                loadWords();
                setupBoard();
                sendBoardToClient();
                startGame();
            }

            if (strMyRole.equals("SPYMASTER")) {
                updateTargetColors();
            }
        });

        lobby.add(top, BorderLayout.NORTH);
        lobby.add(new JScrollPane(lobbyList), BorderLayout.CENTER);
        lobby.add(btnStart, BorderLayout.SOUTH);

        lobby.setSize(450, 350);
        lobby.setLocationRelativeTo(theFrame);
        lobby.setVisible(true);
    }

    void startGame() {
        setupBoardUI();
        updateTargetColors();
        startTimer();
        blnGameStarted = true;
        sendNetwork("START:" + strMyTeam + ":" + strMyRole);
    }

    // Animation timer
    public testcodenames3(String strTitle) {
        setupGUI();
        setupAnimationTimer();
    }

    void startTimer() {
        if (lblTimer == null) return;

        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
            intTimeLeft--;
            lblTimer.setText("Time left: " + intTimeLeft);
            if (intTimeLeft <= 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                endTurn();
            }
        });
        if (turnTimer != null) turnTimer.stop();
        turnTimer = t;
        intTimeLeft = 60;
        lblTimer.setText("Time left: " + intTimeLeft);
        turnTimer.start();
    }

    // ======================
    // END TURN
    // ======================
    void endTurn() {
        log(strOperativeLabel() + " ends guessing");

        strCurrentTurn = strCurrentTurn.equals("RED") ? "BLUE" : "RED";
        lblTurnBox.setText(strCurrentTurn + "'s turn");
        lblHint.setText("Waiting for clue...");
        lblHintNumber.setText("");
        intTimeLeft = 60;

        sendNetwork("ENDTURN:" + strCurrentTurn);

        log(strCurrentTurn + " team's turn begins");

        updateTargetColors();
    }

    // ======================
    // GIVE CLUE
    // ======================
    void giveClue() {
        String strClue = JOptionPane.showInputDialog("Enter clue + number (e.g. Animal 2)");
        if (strClue != null && !strClue.isEmpty()) {
            String strWord = strClue;
            String strNumber = "";
            if (strClue.contains(" ")) {
                int intIndex = strClue.lastIndexOf(" ");
                strWord = strClue.substring(0, intIndex).trim();
                strNumber = strClue.substring(intIndex + 1).trim();
            }

            for (int intCount = 0; intCount < 5; intCount++) {
                for (int intCount2 = 0; intCount2 < 5; intCount2++) {
                    if (strWords[intCount][intCount2].equalsIgnoreCase(strWord)) {
                        JOptionPane.showMessageDialog(theFrame,
                                "Invalid clue! You cannot use a word that is already on the board.");
                        return;
                    }
                }
            }

            lblHint.setText(strWord);
            lblHintNumber.setText(strNumber);

            sendNetwork("CLUE:" + strWord + ":" + strNumber);

            log(strSpymasterLabel() + " gives clue " + strWord + " " + strNumber);
        }
    }

    void setupLobby() {
        JDialog lobby = new JDialog(theFrame, "Lobby", true);
        lobby.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.add(new JLabel("Lobby - Choose role & team"));

        JPanel hostPanel = new JPanel();
        JRadioButton rbHost = new JRadioButton("Host");
        JRadioButton rbJoin = new JRadioButton("Join");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbHost);
        bg.add(rbJoin);
        rbHost.setSelected(true);

        JTextField ipInput = new JTextField("127.0.0.1", 10);

        hostPanel.add(rbHost);
        hostPanel.add(rbJoin);
        hostPanel.add(new JLabel("IP:"));
        hostPanel.add(ipInput);

        top.add(hostPanel);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> lobbyList = new JList<>(listModel);
        lobbyList.setPreferredSize(new Dimension(250, 200));

        listModel.addElement("Red Operative");
        listModel.addElement("Red Spymaster");
        listModel.addElement("Blue Operative");
        listModel.addElement("Blue Spymaster");

        JButton btnStart = new JButton("Start Game");
        btnStart.addActionListener(e -> {
            if (lobbyList.getSelectedValue() == null) return;

            blnIsServer = rbHost.isSelected();
            strServerIP = ipInput.getText().trim();

            String strSelection = lobbyList.getSelectedValue();
            strMyTeam = strSelection.contains("Red") ? "RED" : "BLUE";
            strMyRole = strSelection.contains("Spymaster") ? "SPYMASTER" : "OPERATIVE";

            lobby.dispose();

            setupSocket();

            if (blnIsServer) {
                loadWords();
                setupBoard();
                sendBoardToClient();
                startGame();
            }

            if (strMyRole.equals("SPYMASTER")) {
                updateTargetColors();
            }
        });

        lobby.add(top, BorderLayout.NORTH);
        lobby.add(new JScrollPane(lobbyList), BorderLayout.CENTER);
        lobby.add(btnStart, BorderLayout.SOUTH);

        lobby.setSize(450, 350);
        lobby.setLocationRelativeTo(theFrame);
        lobby.setVisible(true);
    }

    void startGame() {
        setupBoardUI();
        updateTargetColors();
        startTimer();
        blnGameStarted = true;
        sendNetwork("START:" + strMyTeam + ":" + strMyRole);
    }

    // BOTTOM PANEL
        bottomPanel = new JPanel();

        btnEndTurn = new JButton("End Turn");
        btnEndTurn.addActionListener(this);
        bottomPanel.add(btnEndTurn);

        btnToggleOverlay = new JButton("Toggle Overlay");
        btnToggleOverlay.addActionListener(this);
        bottomPanel.add(btnToggleOverlay);

        btnGiveClue = new JButton("Give Clue");
        btnGiveClue.addActionListener(this);
        bottomPanel.add(btnGiveClue);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // CENTER BOARD PANEL
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlTurnBox = new JPanel();
        pnlTurnBox.setPreferredSize(new Dimension(880, 45));
        pnlTurnBox.setMaximumSize(new Dimension(880, 45));
        pnlTurnBox.setBackground(Color.WHITE);
        pnlTurnBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        lblTurnBox = new JLabel(strCurrentTurn + "'s turn", SwingConstants.CENTER);
        lblTurnBox.setFont(new Font("Arial", Font.BOLD, 18));
        pnlTurnBox.add(lblTurnBox);

        centerPanel.add(pnlTurnBox);
        centerPanel.add(Box.createVerticalStrut(10));

        boardPanel = new JPanel(new GridLayout(5, 5, 15, 15));
        boardPanel.setBackground(new Color(139, 90, 43));
        wordButtons = new JButton[5][5];

        for (int intCount = 0; intCount < 5; intCount++)
            for (int intCount2 = 0; intCount2 < 5; intCount2++) {
                wordButtons[intCount][intCount2] = new JButton("WORD");
                wordButtons[intCount][intCount2].setFont(new Font("Arial", Font.BOLD, 18));
                wordButtons[intCount][intCount2].setBackground(new Color(245, 235, 200));
                wordButtons[intCount][intCount2].setFocusPainted(false);
                wordButtons[intCount][intCount2].setOpaque(true);
                wordButtons[intCount][intCount2].setBorderPainted(false);
                wordButtons[intCount][intCount2].addActionListener(this);

                currentColors[intCount][intCount2] =
                        wordButtons[intCount][intCount2].getBackground();
                targetColors[intCount][intCount2] =
                        wordButtons[intCount][intCount2].getBackground();

                boardPanel.add(wordButtons[intCount][intCount2]);
            }

        centerPanel.add(boardPanel);
        centerPanel.add(Box.createVerticalStrut(10));

        JPanel pnlHintRow = new JPanel();
        pnlHintRow.setLayout(new BoxLayout(pnlHintRow, BoxLayout.X_AXIS));
        pnlHintRow.setBackground(new Color(210, 180, 140));
        pnlHintRow.setPreferredSize(new Dimension(880, 45));
        pnlHintRow.setMaximumSize(new Dimension(880, 45));

        JPanel pnlHintBox = new JPanel();
        pnlHintBox.setPreferredSize(new Dimension(700, 45));
        pnlHintBox.setMaximumSize(new Dimension(700, 45));
        pnlHintBox.setBackground(Color.WHITE);
        pnlHintBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        lblHint = new JLabel("Waiting for clue...", SwingConstants.CENTER);
        lblHint.setFont(new Font("Arial", Font.BOLD, 16));
        pnlHintBox.add(lblHint);

        JPanel pnlHintNumber = new JPanel();
        pnlHintNumber.setPreferredSize(new Dimension(45, 45));
        pnlHintNumber.setMaximumSize(new Dimension(45, 45));
        pnlHintNumber.setBackground(Color.WHITE);
        pnlHintNumber.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        lblHintNumber = new JLabel("", SwingConstants.CENTER);
        lblHintNumber.setFont(new Font("Arial", Font.BOLD, 16));
        pnlHintNumber.add(lblHintNumber);

        pnlHintRow.add(Box.createHorizontalGlue());
        pnlHintRow.add(pnlHintBox);
        pnlHintRow.add(Box.createHorizontalStrut(10));
        pnlHintRow.add(pnlHintNumber);
        pnlHintRow.add(Box.createHorizontalGlue());

        centerPanel.add(pnlHintRow);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        theFrame.setSize(1280, 720);
        theFrame.setResizable(false);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setLocationRelativeTo(null);
        theFrame.setVisible(true);

        setupLobby();
    }

    void setupBoardUI() {
        for (int intCount = 0; intCount < 5; intCount++)
            for (int intCount2 = 0; intCount2 < 5; intCount2++)
                wordButtons[intCount][intCount2].setText(strWords[intCount][intCount2]);

        updateTargetColors();
    }

    // ======================
    // ANIMATION HELPERS
    // ======================
    void setupAnimationTimer() {
        animationTimer = new javax.swing.Timer(1000 / FPS, evt -> updateAnimations());
        animationTimer.start();
    }

    void updateAnimations() {
        boolean blnUpdated = false;

        // Animate buttons
        for (int intCount = 0; intCount < 5; intCount++) {
            for (int intCount2 = 0; intCount2 < 5; intCount2++) {
                Color cur = currentColors[intCount][intCount2];
                Color tgt = targetColors[intCount][intCount2];
                if (!cur.equals(tgt)) {
                    currentColors[intCount][intCount2] =
                            blendColors(cur, tgt, COLOR_STEP);
                    wordButtons[intCount][intCount2]
                            .setBackground(currentColors[intCount][intCount2]);
                    blnUpdated = true;
                }
            }
        }

        // Animate Red score
        if (intDisplayedRed != intRedLeft) {
            if (intDisplayedRed > intRedLeft) intDisplayedRed--;
            else intDisplayedRed++;
            lblRedCount.setText(String.valueOf(intDisplayedRed));
            blnUpdated = true;
        }

        // Animate Blue score
        if (intDisplayedBlue != intBlueLeft) {
            if (intDisplayedBlue > intBlueLeft) intDisplayedBlue--;
            else intDisplayedBlue++;
            lblBlueCount.setText(String.valueOf(intDisplayedBlue));
            blnUpdated = true;
        }

        if (blnUpdated) boardPanel.repaint();
    }

    Color blendColors(Color start, Color end, float step) {
        float r = start.getRed() + (end.getRed() - start.getRed()) * step;
        float g = start.getGreen() + (end.getGreen() - start.getGreen()) * step;
        float b = start.getBlue() + (end.getBlue() - start.getBlue()) * step;
        return new Color(clamp(r), clamp(g), clamp(b));
    }

    int clamp(float val) {
        return Math.min(255, Math.max(0, Math.round(val)));
    }

    void updateTargetColors() {
        for (int intCount = 0; intCount < 5; intCount++) {
            for (int intCount2 = 0; intCount2 < 5; intCount2++) {
                if (blnRevealed[intCount][intCount2]) {
                    targetColors[intCount][intCount2] =
                            getColorFromString(strColors[intCount][intCount2]);
                } else if (strMyRole.equals("SPYMASTER") && blnOverlayOn) {
                    targetColors[intCount][intCount2] =
                            getColorFromString(strColors[intCount][intCount2]);
                } else {
                    targetColors[intCount][intCount2] =
                            new Color(245, 235, 200);
                }
            }
        }
    }

    Color getColorFromString(String strColor) {
        switch (strColor) {
            case "RED": return new Color(170, 60, 50);
            case "BLUE": return new Color(60, 130, 160);
            case "BLACK": return new Color(90, 90, 90);
            default: return new Color(200, 190, 170);
        }
    }

    void animateScores() {
        // animation handled in updateAnimations
    }

    // ======================
    // NETWORKING AND LOGGING
    // ======================
    void setupSocket() {
        if (blnIsServer) {
            ssm = new SuperSocketMaster(1337, evt -> handleNetwork());
            if (!ssm.connect()) {
                log("Server failed to start!");
                return;
            }
            log("Server started on port 1337");
            try { Thread.sleep(1000); } catch (Exception ignored) {}
        } else {
            ssm = new SuperSocketMaster(strServerIP, 1337, evt -> handleNetwork());
            boolean blnConnected = false;
            for (int intCount = 0; intCount < 10; intCount++) {
                if (ssm.connect()) {
                    blnConnected = true;
                    break;
                }
                try { Thread.sleep(200); } catch (Exception ignored) {}
            }
            if (blnConnected) {
                log("Connected to server: " + strServerIP);
                sendNetwork("REQUESTBOARD");
            } else {
                log("Failed to connect to server");
            }
        }
    }

    void sendBoardToClient() {
        StringBuilder strWordBuilder = new StringBuilder();
        StringBuilder strColorBuilder = new StringBuilder();

        for (int intCount = 0; intCount < 5; intCount++) {
            for (int intCount2 = 0; intCount2 < 5; intCount2++) {
                strWordBuilder.append(strWords[intCount][intCount2]);
                strColorBuilder.append(strColors[intCount][intCount2]);
                if (!(intCount == 4 && intCount2 == 4)) {
                    strWordBuilder.append(",");
                    strColorBuilder.append(",");
                }
            }
        }

        sendNetwork("BOARD:" + strWordBuilder + "|" + strColorBuilder);
    }

    void handleNetwork() {
        String strMsg = ssm.readText();
        if (strMsg == null) return;

        log("Network: " + strMsg);

        if (strMsg.equals("REQUESTBOARD") && blnIsServer) {
            sendBoardToClient();
        }

        if (strMsg.startsWith("BOARD:")) {
            String[] strParts = strMsg.substring(6).split("\\|");
            String[] strWordArr = strParts[0].split(",");
            String[] strColorArr = strParts[1].split(",");

            int intIndex = 0;
            for (int intCount = 0; intCount < 5; intCount++) {
                for (int intCount2 = 0; intCount2 < 5; intCount2++) {
                    strWords[intCount][intCount2] = strWordArr[intIndex];
                    strColors[intCount][intCount2] = strColorArr[intIndex];
                    intIndex++;
                }
            }

            setupBoardUI();
            blnGameStarted = true;

            if (strMyRole.equals("SPYMASTER")) {
                updateTargetColors();
            }
        }

        if (strMsg.startsWith("CLICK:")) {
            String[] strParts = strMsg.split(":");
            int intRow = Integer.parseInt(strParts[1]);
            int intCol = Integer.parseInt(strParts[2]);

            if (!blnRevealed[intRow][intCol]) {
                blnRevealed[intRow][intCol] = true;
                wordButtons[intRow][intCol].setEnabled(false);

                updateTargetColors();

                if (strColors[intRow][intCol].equals("RED")) intRedLeft--;
                if (strColors[intRow][intCol].equals("BLUE")) intBlueLeft--;

                animateScores();
                log(strOperativeLabel() + " taps " + strWords[intRow][intCol]);
            }
        }

        if (strMsg.startsWith("ENDTURN:")) {
            strCurrentTurn = strMsg.split(":")[1];
            lblTurnBox.setText(strCurrentTurn + "'s turn");
            lblHint.setText("Waiting for clue...");
            lblHintNumber.setText("");
            intTimeLeft = 60;

            log(strCurrentTurn + " team's turn begins");
            updateTargetColors();
        }

        if (strMsg.startsWith("CLUE:")) {
            String[] strParts = strMsg.split(":");
            if (strParts.length == 3) {
                String strWord = strParts[1];
                String strNumber = strParts[2];
                lblHint.setText(strWord);
                lblHintNumber.setText(strNumber);

                log(strSpymasterLabel() + " gives clue " + strWord + " " + strNumber);
            }
        }

        if (strMsg.startsWith("CHAT:")) {
            log("Player: " + strMsg.substring(5));
        }

        if (strMsg.startsWith("START:")) {
            String[] strParts = strMsg.split(":");
            strMyTeam = strParts[1];
            strMyRole = strParts[2];
            log("Assigned: " + strMyTeam + " " + strMyRole);

            if (strMyRole.equals("SPYMASTER")) {
                updateTargetColors();
            }
        }
    }

    void sendNetwork(String strMsg) {
        if (ssm != null) ssm.sendText(strMsg);
    }

    void log(String strMsg) {
        if (gameLog != null) gameLog.append(strMsg + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new testcodenames3("Codenames"));
    }
}