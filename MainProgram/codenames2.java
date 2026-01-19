package MainProgram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import sockets.SuperSocketMaster;


public class codenames2 implements ActionListener {


    JFrame theFrame;
    JPanel mainPanel, boardPanel, topPanel, bottomPanel, rightPanel;
    JButton[][] wordButtons;
    JButton btnEndTurn, btnToggleOverlay, btnGiveClue;
    JButton btnSendChat;
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


    JLabel lblTurn;
    JLabel lblClue;
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


    // Lobby
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


    public codenames2(String title) {
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
        lblTurn.setText(currentTurn + " team's turn");
        lblClue.setText("Waiting for clue...");
        timeLeft = 60;


        sendNetwork("ENDTURN:" + currentTurn);
        log("Turn ended. Now " + currentTurn + "'s turn.");
    }


    void giveClue() {
        String clue = JOptionPane.showInputDialog("Enter clue + number (e.g. Animal 2)");
        if (clue != null && !clue.isEmpty()) {
            lblClue.setText("Clue: " + clue);
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


            // set host/join
            isServer = rbHost.isSelected();
            serverIP = ipInput.getText().trim();


            String sel = lobbyList.getSelectedValue();
            myTeam = sel.contains("Red") ? "RED" : "BLUE";
            myRole = sel.contains("Spymaster") ? "SPYMASTER" : "OPERATIVE";
            lobby.dispose();


            setupSocket();


            if (isServer) {
                // Host creates board and sends it
                loadWords();
                setupBoard();
                sendBoardToClient();
                startGame();
            }
        });


