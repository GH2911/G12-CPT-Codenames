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

    String[][] words = new String[5][5];
    String[][] colors = new String[5][5];
    boolean[][] revealed = new boolean[5][5];

    String myRole = "OPERATIVE";
    String myTeam = "RED";
    String currentTurn = "RED";

    int redLeft = 9;
    int blueLeft = 8;

    // GUI Labels
    JLabel lblTurnBox;
    JLabel lblHint;
    JLabel lblRedCount;
    JLabel lblBlueCount;
    JLabel lblTimer;

    ArrayList<String> wordPool = new ArrayList<>();

    // Online
    SuperSocketMaster ssm;
    boolean isServer = false;
    String serverIP = "";

    // Overlay
    boolean overlayOn = true;

    // Timer
    javax.swing.Timer turnTimer;
    int timeLeft = 60;

    boolean gameStarted = false;

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!gameStarted) return;

        if (evt.getSource() == btnEndTurn) {
            endTurn();
            return;
        }
        if (evt.getSource() == btnToggleOverlay) {
            overlayOn = !overlayOn;
            updateBoardColors();
            boardPanel.repaint();
            return;
        }
        if (evt.getSource() == btnGiveClue) {
            if (myRole.equals("SPYMASTER") && myTeam.equals(currentTurn)) {
                giveClue();
            } else {
                JOptionPane.showMessageDialog(theFrame, "Only the spymaster of the current team can give clues.");
            }
            return;
        }

        if (!myRole.equals("OPERATIVE") || !myTeam.equals(currentTurn)) return;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                if (evt.getSource() == wordButtons[r][c] && !revealed[r][c]) {
                    revealed[r][c] = true;
                    revealColor(r, c);
                    wordButtons[r][c].setEnabled(false);

                    sendNetwork("CLICK:" + r + ":" + c);

                    if (colors[r][c].equals("BLACK")) {
                        log("ASSASSIN! " + (currentTurn.equals("RED") ? "BLUE" : "RED") + " wins!");
                        JOptionPane.showMessageDialog(theFrame,
                                "ASSASSIN! " + (currentTurn.equals("RED") ? "BLUE" : "RED") + " wins!");
                        System.exit(0);
                    }
                    if (colors[r][c].equals("RED")) redLeft--;
                    if (colors[r][c].equals("BLUE")) blueLeft--;

                    lblRedCount.setText(String.valueOf(redLeft));
                    lblBlueCount.setText(String.valueOf(blueLeft));

                    if (redLeft == 0 || blueLeft == 0) {
                        log((redLeft == 0 ? "RED" : "BLUE") + " TEAM WINS!");
                        JOptionPane.showMessageDialog(theFrame,
                                (redLeft == 0 ? "RED" : "BLUE") + " TEAM WINS!");
                        System.exit(0);
                    }

                    if (!colors[r][c].equals(currentTurn)) {
                        endTurn();
                    }
                }
            }
        }
    }

    public testcodenames3(String title) {
        setupGUI();
    }

    void startTimer() {
        if (lblTimer == null) return;

        javax.swing.Timer t = new javax.swing.Timer(1000, e -> {
            timeLeft--;
            lblTimer.setText("Time left: " + timeLeft);
            if (timeLeft <= 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                endTurn();
            }
        });
        if (turnTimer != null) turnTimer.stop();
        turnTimer = t;
        timeLeft = 60;
        lblTimer.setText("Time left: " + timeLeft);
        turnTimer.start();
    }

    void endTurn() {
        currentTurn = currentTurn.equals("RED") ? "BLUE" : "RED";
        lblTurnBox.setText(currentTurn + "'s turn");
        lblHint.setText("Waiting for clue...");
        timeLeft = 60;
        sendNetwork("ENDTURN:" + currentTurn);
        log("Turn ended. Now " + currentTurn + "'s turn.");
    }

    void giveClue() {
        String clue = JOptionPane.showInputDialog("Enter clue + number (e.g. Animal 2)");
        if (clue != null && !clue.isEmpty()) {
            lblHint.setText(clue);
            sendNetwork("CLUE:" + clue);
            log("Clue given: " + clue);
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

            isServer = rbHost.isSelected();
            serverIP = ipInput.getText().trim();

            String sel = lobbyList.getSelectedValue();
            myTeam = sel.contains("Red") ? "RED" : "BLUE";
            myRole = sel.contains("Spymaster") ? "SPYMASTER" : "OPERATIVE";

            lobby.dispose();

            setupSocket();

            if (isServer) {
                loadWords();
                setupBoard();
                sendBoardToClient();
                startGame();
            }

            // Immediately apply overlay if spymaster
            if (myRole.equals("SPYMASTER")) {
                updateBoardColors();
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
        updateBoardColors();
        startTimer();
        gameStarted = true;
        sendNetwork("START:" + myTeam + ":" + myRole);
    }

    void loadWords() {
        wordPool.clear();
        try {
            Scanner sc = new Scanner(new File("wordlist.txt"));
            while (sc.hasNextLine()) wordPool.add(sc.nextLine().trim());
            sc.close();
            Collections.shuffle(wordPool);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "wordlist.txt missing");
            System.exit(0);
        }
    }

    void setupBoard() {
        int i = 0;
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                words[r][c] = wordPool.get(i++);

        ArrayList<String> bag = new ArrayList<>();
        for (int x = 0; x < 9; x++) bag.add("RED");
        for (int x = 0; x < 8; x++) bag.add("BLUE");
        for (int x = 0; x < 7; x++) bag.add("NEUTRAL");
        bag.add("BLACK");

        do {
            Collections.shuffle(bag);
        } while (bag.indexOf("BLACK") < 6);

        i = 0;
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                colors[r][c] = bag.get(i++);
    }

    void setupGUI() {
        theFrame = new JFrame("Codenames");
        theFrame.setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(210, 180, 140));
        theFrame.setContentPane(mainPanel);

        // LEFT PANEL
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 620));
        leftPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlRedTeam = new JPanel(new BorderLayout());
        pnlRedTeam.setBackground(new Color(170, 60, 50));
        pnlRedTeam.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblRedCount = new JLabel(String.valueOf(redLeft));
        lblRedCount.setFont(new Font("Arial", Font.BOLD, 48));
        lblRedCount.setForeground(Color.WHITE);

        JPanel pnlRedText = new JPanel();
        pnlRedText.setBackground(new Color(170, 60, 50));
        pnlRedText.setLayout(new BoxLayout(pnlRedText, BoxLayout.Y_AXIS));

        pnlRedText.add(new JLabel("Operative(s)"));
        pnlRedText.add(new JLabel("-"));
        pnlRedText.add(Box.createVerticalStrut(10));
        pnlRedText.add(new JLabel("Spymaster(s)"));
        pnlRedText.add(new JLabel("-"));
        for (Component c : pnlRedText.getComponents()) c.setForeground(Color.WHITE);

        pnlRedTeam.add(pnlRedText, BorderLayout.WEST);
        pnlRedTeam.add(lblRedCount, BorderLayout.EAST);

        leftPanel.add(pnlRedTeam, BorderLayout.NORTH);
        mainPanel.add(leftPanel, BorderLayout.WEST);

        // RIGHT PANEL
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(200, 620));
        rightPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlBlueTeam = new JPanel(new BorderLayout());
        pnlBlueTeam.setBackground(new Color(60, 130, 160));
        pnlBlueTeam.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblBlueCount = new JLabel(String.valueOf(blueLeft));
        lblBlueCount.setFont(new Font("Arial", Font.BOLD, 48));
        lblBlueCount.setForeground(Color.WHITE);

        JPanel pnlBlueText = new JPanel();
        pnlBlueText.setBackground(new Color(60, 130, 160));
        pnlBlueText.setLayout(new BoxLayout(pnlBlueText, BoxLayout.Y_AXIS));

        pnlBlueText.add(new JLabel("Operative(s)"));
        pnlBlueText.add(new JLabel("-"));
        pnlBlueText.add(Box.createVerticalStrut(10));
        pnlBlueText.add(new JLabel("Spymaster(s)"));
        pnlBlueText.add(new JLabel("-"));
        for (Component c : pnlBlueText.getComponents()) c.setForeground(Color.WHITE);

        pnlBlueTeam.add(lblBlueCount, BorderLayout.WEST);
        pnlBlueTeam.add(pnlBlueText, BorderLayout.EAST);

        rightPanel.add(pnlBlueTeam, BorderLayout.NORTH);

        gameLog = new JTextArea();
        gameLog.setEditable(false);
        JScrollPane logScroll = new JScrollPane(gameLog);
        rightPanel.add(logScroll, BorderLayout.CENTER);

        mainPanel.add(rightPanel, BorderLayout.EAST);

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

        // Turn Box
        JPanel pnlTurnBox = new JPanel();
        pnlTurnBox.setPreferredSize(new Dimension(880, 45));
        pnlTurnBox.setMaximumSize(new Dimension(880, 45));
        pnlTurnBox.setBackground(Color.WHITE);
        pnlTurnBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        lblTurnBox = new JLabel(currentTurn + "'s turn", SwingConstants.CENTER);
        lblTurnBox.setFont(new Font("Arial", Font.BOLD, 18));
        pnlTurnBox.add(lblTurnBox);
        centerPanel.add(pnlTurnBox);
        centerPanel.add(Box.createVerticalStrut(10));

        // Board Grid
        boardPanel = new JPanel(new GridLayout(5, 5, 15, 15));
        boardPanel.setBackground(new Color(139, 90, 43));
        wordButtons = new JButton[5][5];
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++) {
                wordButtons[r][c] = new JButton("WORD");
                wordButtons[r][c].setFont(new Font("Arial", Font.BOLD, 18));
                wordButtons[r][c].setBackground(new Color(245, 235, 200));
                wordButtons[r][c].setFocusPainted(false);
                wordButtons[r][c].addActionListener(this);
                boardPanel.add(wordButtons[r][c]);
            }
        centerPanel.add(boardPanel);
        centerPanel.add(Box.createVerticalStrut(10));

        // Hint box
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
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                wordButtons[r][c].setText(words[r][c]);

        updateBoardColors();
    }

    void updateBoardColors() {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                JButton b = wordButtons[r][c];
                if (revealed[r][c]) {
                    revealColor(r, c, b);
                } else if (myRole.equals("SPYMASTER") && overlayOn) {
                    revealColor(r, c, b);
                } else {
                    b.setBackground(new Color(245, 235, 200));
                }
            }
        }
    }

    void revealColor(int r, int c) {
        revealColor(r, c, wordButtons[r][c]);
    }

    void revealColor(int r, int c, JButton b) {
        switch (colors[r][c]) {
            case "RED": b.setBackground(new Color(170, 60, 50)); break;
            case "BLUE": b.setBackground(new Color(60, 130, 160)); break;
            case "BLACK": b.setBackground(Color.BLACK); break;
            default: b.setBackground(new Color(200, 190, 170));
        }
    }

    void setupSocket() {
        if (isServer) {
            ssm = new SuperSocketMaster(1337, evt -> handleNetwork());
            log("Server started on port 1337");
        } else {
            ssm = new SuperSocketMaster(serverIP, 1337, evt -> handleNetwork());
            if (ssm.connect()) log("Connected to server: " + serverIP);
            else log("Failed to connect to server");
        }
    }

    void sendBoardToClient() {
        StringBuilder w = new StringBuilder();
        StringBuilder c = new StringBuilder();

        for (int r = 0; r < 5; r++)
            for (int col = 0; col < 5; col++) {
                w.append(words[r][col]);
                c.append(colors[r][col]);
                if (!(r == 4 && col == 4)) { w.append(","); c.append(","); }
            }

        sendNetwork("BOARD:" + w.toString() + "|" + c.toString());
    }

    void handleNetwork() {
        String msg = ssm.readText();
        if (msg == null) return;

        log("Network: " + msg);

        if (msg.startsWith("BOARD:")) {
            String[] parts = msg.substring(6).split("\\|");
            String[] w = parts[0].split(",");
            String[] c = parts[1].split(",");

            int idx = 0;
            for (int r = 0; r < 5; r++)
                for (int col = 0; col < 5; col++) {
                    words[r][col] = w[idx];
                    colors[r][col] = c[idx];
                    idx++;
                }

            setupBoardUI();
            gameStarted = true;

            // Overlay for spymaster immediately
            if (myRole.equals("SPYMASTER")) {
                updateBoardColors();
            }
        }

        if (msg.startsWith("CLICK:")) {
            String[] p = msg.split(":");
            int r = Integer.parseInt(p[1]);
            int c = Integer.parseInt(p[2]);
            if (!revealed[r][c]) {
                revealed[r][c] = true;
                wordButtons[r][c].setEnabled(false);
                revealColor(r, c);

                if (colors[r][c].equals("RED")) redLeft--;
                if (colors[r][c].equals("BLUE")) blueLeft--;

                lblRedCount.setText(String.valueOf(redLeft));
                lblBlueCount.setText(String.valueOf(blueLeft));
            }
        }

        if (msg.startsWith("ENDTURN:")) {
            currentTurn = msg.split(":")[1];
            lblTurnBox.setText(currentTurn + "'s turn");
            lblHint.setText("Waiting for clue...");
            timeLeft = 60;
        }

        if (msg.startsWith("CLUE:")) {
            lblHint.setText(msg.substring(5));
        }

        if (msg.startsWith("CHAT:")) {
            log("Opponent: " + msg.substring(5));
        }

        if (msg.startsWith("START:")) {
            String[] parts = msg.split(":");
            myTeam = parts[1];
            myRole = parts[2];
            log("Assigned: " + myTeam + " " + myRole);

            // Immediately apply overlay if spymaster
            if (myRole.equals("SPYMASTER")) {
                updateBoardColors();
            }
        }
    }

    void sendNetwork(String msg) {
        if (ssm != null) ssm.sendText(msg);
    }

    void log(String msg) {
        if (gameLog != null) gameLog.append(msg + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new testcodenames3("Codenames"));
    }
}
