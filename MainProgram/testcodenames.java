package MainProgram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import sockets.SuperSocketMaster;


public class testcodenames implements ActionListener {


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


