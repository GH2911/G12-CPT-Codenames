package MainProgram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import sockets.SuperSocketMaster;

public class MainCodenamesGame implements ActionListener {

    // Properties
    JFrame theFrame;
    JPanel mainPanel, boardPanel, leftPanel, rightPanel, bottomPanel;
    JButton[][] btnWord;
    JButton btnEndTurn, btnToggleOverlay, btnGiveClue;
    JTextField txtChatInput;
    JTextArea txtGameLog;

    final int intRows = 5;
    final int intCols = 5;

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

    ArrayList<String> arrWordPool = new ArrayList<>();

    // Online
    SuperSocketMaster ssm;
    boolean blnIsServer = false;
    String strServerIP = "";
    JTextField txtChatField;

    // Overlay
    boolean blnOverlayOn = true;

    // Timer
    javax.swing.Timer turnTimer;
    int intTimeLeft = 60;

    boolean blnGameStarted = false;

    // ANIMATION TRACKING

    Color[][] colTarget = new Color[5][5]; 
    Color[][] colCurrent = new Color[5][5]; 

    int intDisplayedRed = intRedLeft;  
    int intDisplayedBlue = intBlueLeft; 

    // 60 FPS animation
    javax.swing.Timer animationTimer; 

    final int intFPS = 60;
    final int intAnimDuration = 300; 
    final float fltColorStep = 1.0f / ((intAnimDuration / 1000f) * intFPS);

    // Character/Player labels

    String getActorLabel() {
        return strMyTeam + " " + (strMyRole.equals("SPYMASTER") ? "Spymaster" : "Operative");
    }

    String getOperativeLabel() {
        return strCurrentTurn + " Operative";
    }

    String getSpymasterLabel() {
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

        for (int intRow = 0; intRow < 5; intRow++) {
            for (int intCol = 0; intCol < 5; intCol++) {
                if (evt.getSource() == btnWord[intRow][intCol] && !blnRevealed[intRow][intCol]) {
                    blnRevealed[intRow][intCol] = true;

                    // Set target color for animation
                    updateTargetColors();

                    btnWord[intRow][intCol].setEnabled(false);
                    sendNetwork("CLICK:" + intRow + ":" + intCol);

                    // Log Local Click
                    log(getActorLabel() + " taps " + strWords[intRow][intCol]);

                    if (strColors[intRow][intCol].equals("BLACK")) {
                        log("ASSASSIN! " + (strCurrentTurn.equals("RED") ? "BLUE" : "RED") + " wins!");
                        JOptionPane.showMessageDialog(theFrame,
                                "ASSASSIN! " + (strCurrentTurn.equals("RED") ? "BLUE" : "RED") + " wins!");
                        System.exit(0);
                    }
                    if (strColors[intRow][intCol].equals("RED")) intRedLeft--;
                    if (strColors[intRow][intCol].equals("BLUE")) intBlueLeft--;

                    // Animate score update
                    animateScores();

                    if (intRedLeft == 0 || intBlueLeft == 0) {
                        log((intRedLeft == 0 ? "RED" : "BLUE") + " TEAM WINS!");
                        JOptionPane.showMessageDialog(theFrame,
                                (intRedLeft == 0 ? "RED" : "BLUE") + " TEAM WINS!");
                        System.exit(0);
                    }

                    if (!strColors[intRow][intCol].equals(strCurrentTurn)) {
                        endTurn();
                    }
                }
            }
        }
    }

     // start animation timer for buttons and scores
    public MainCodenamesGame(String strTitle) {
        setupGUI();
        setupAnimationTimer();
    }

    void startTimer() {
        if (lblTimer == null) return;

        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> {
            intTimeLeft--;
            lblTimer.setText("Time left: " + intTimeLeft);
            if (intTimeLeft <= 0) {
                ((javax.swing.Timer) e.getSource()).stop();
                endTurn();
            }
        });
        if (turnTimer != null) turnTimer.stop();
        turnTimer = timer;
        intTimeLeft = 60;
        lblTimer.setText("Time left: " + intTimeLeft);
        turnTimer.start();
    }

    // End of Turn .

    void endTurn() {
        log(getOperativeLabel() + " ends guessing");

        strCurrentTurn = strCurrentTurn.equals("RED") ? "BLUE" : "RED";
        lblTurnBox.setText(strCurrentTurn + "'s turn");
        lblHint.setText("Waiting for clue...");
        lblHintNumber.setText("");
        intTimeLeft = 60;

        sendNetwork("ENDTURN:" + strCurrentTurn);

        log(strCurrentTurn + " team's turn begins");

        updateTargetColors();
    }

    // Spymaster Gives Clue

    void giveClue() {
        String strClue = JOptionPane.showInputDialog("Enter clue + number (e.g. Animal 2)");
        if (strClue != null && !strClue.isEmpty()) {
            String strWord = strClue;
            String strNumber = "";
            if (strClue.contains(" ")) {
                int intIdx = strClue.lastIndexOf(" ");
                strWord = strClue.substring(0, intIdx).trim();
                strNumber = strClue.substring(intIdx + 1).trim();
            }

            // Clue Check (GAME REQUIREMENT)
            for (int intRow = 0; intRow < 5; intRow++) {
                for (int intCol = 0; intCol < 5; intCol++) {
                    if (strWords[intRow][intCol].equalsIgnoreCase(strWord)) {
                        JOptionPane.showMessageDialog(theFrame,
                                "Invalid clue! You cannot use a word that is already on the board.");
                        return;
                    }
                }
            }
            

            lblHint.setText(strWord);
            lblHintNumber.setText(strNumber);

            sendNetwork("CLUE:" + strWord + ":" + strNumber);

            log(getSpymasterLabel() + " gives clue " + strWord + " " + strNumber);
        }
    }

    void setupLobby() {
        JDialog dlgLobby = new JDialog(theFrame, "Lobby", true);
        dlgLobby.setLayout(new BorderLayout());

        JPanel pnlTop = new JPanel();
        pnlTop.add(new JLabel("Lobby - Choose role & team"));

        JPanel pnlHost = new JPanel();
        JRadioButton rbHost = new JRadioButton("Host");
        JRadioButton rbJoin = new JRadioButton("Join");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbHost);
        bg.add(rbJoin);
        rbHost.setSelected(true);

        JTextField txtIPInput = new JTextField("127.0.0.1", 10);

        pnlHost.add(rbHost);
        pnlHost.add(rbJoin);
        pnlHost.add(new JLabel("IP:"));
        pnlHost.add(txtIPInput);

        pnlTop.add(pnlHost);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> lstLobby = new JList<>(listModel);
        lstLobby.setPreferredSize(new Dimension(250, 200));

        listModel.addElement("Red Operative");
        listModel.addElement("Red Spymaster");
        listModel.addElement("Blue Operative");
        listModel.addElement("Blue Spymaster");

        JButton btnStart = new JButton("Start Game");
        btnStart.addActionListener(e -> {
            if (lstLobby.getSelectedValue() == null) return;

            blnIsServer = rbHost.isSelected();
            strServerIP = txtIPInput.getText().trim();

            String strSel = lstLobby.getSelectedValue();
            strMyTeam = strSel.contains("Red") ? "RED" : "BLUE";
            strMyRole = strSel.contains("Spymaster") ? "SPYMASTER" : "OPERATIVE";

            dlgLobby.dispose();

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

        dlgLobby.add(pnlTop, BorderLayout.NORTH);
        dlgLobby.add(new JScrollPane(lstLobby), BorderLayout.CENTER);
        dlgLobby.add(btnStart, BorderLayout.SOUTH);

        dlgLobby.setSize(450, 350);
        dlgLobby.setLocationRelativeTo(theFrame);
        dlgLobby.setVisible(true);
    }

    void startGame() {
        setupBoardUI();
        updateTargetColors();
        startTimer();
        blnGameStarted = true;
        sendNetwork("START:" + strMyTeam + ":" + strMyRole);
    }

    void loadWords() {
        arrWordPool.clear();
        try {
            Scanner sc = new Scanner(new File("wordlist.txt"));
            while (sc.hasNextLine()) arrWordPool.add(sc.nextLine().trim());
            sc.close();
            Collections.shuffle(arrWordPool);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "wordlist.txt missing");
            System.exit(0);
        }
    }

    void setupBoard() {
        int intIdx = 0;
        for (int intRow = 0; intRow < 5; intRow++)
            for (int intCol = 0; intCol < 5; intCol++)
                strWords[intRow][intCol] = arrWordPool.get(intIdx++);

        ArrayList<String> arrBag = new ArrayList<>();
        for (int intCount = 0; intCount < 9; intCount++) arrBag.add("RED");
        for (int intCount2 = 0; intCount2 < 8; intCount2++) arrBag.add("BLUE");
        for (int intCount3 = 0; intCount3 < 7; intCount3++) arrBag.add("NEUTRAL");
        arrBag.add("BLACK");

        do {
            Collections.shuffle(arrBag);
        } while (arrBag.indexOf("BLACK") < 6);

        intIdx = 0;
        for (int intRow = 0; intRow < 5; intRow++)
            for (int intCol = 0; intCol < 5; intCol++)
                strColors[intRow][intCol] = arrBag.get(intIdx++);
    }

    void setupGUI() {
        theFrame = new JFrame("Codenames");
        theFrame.setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(210, 180, 140));
        theFrame.setContentPane(mainPanel);

        // Left Panel
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(200, 620));
        leftPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlRedTeam = new JPanel(new BorderLayout());
        pnlRedTeam.setBackground(new Color(170, 60, 50));
        pnlRedTeam.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblRedCount = new JLabel(String.valueOf(intRedLeft));
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

        // Right Panel
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(200, 620));
        rightPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlBlueTeam = new JPanel(new BorderLayout());
        pnlBlueTeam.setBackground(new Color(60, 130, 160));
        pnlBlueTeam.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblBlueCount = new JLabel(String.valueOf(intBlueLeft));
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

        txtGameLog = new JTextArea();
        txtGameLog.setEditable(false);
        JScrollPane logScroll = new JScrollPane(txtGameLog);
        rightPanel.add(logScroll, BorderLayout.CENTER);
        
        JPanel pnlChat = new JPanel();
        pnlChat.setLayout(new BoxLayout(pnlChat, BoxLayout.Y_AXIS));
        pnlChat.setBackground(new Color(210, 180, 140));

        txtChatField = new JTextField();
        txtChatField.setPreferredSize(new Dimension(180, 30));
        txtChatField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtChatField.addActionListener(e -> {
            String strText = txtChatField.getText().trim();
            if (!strText.isEmpty()) {
                log("You: " + strText);
                sendNetwork("CHAT:" + strText);
                txtChatField.setText("");
            }
        });

        pnlChat.add(txtChatField);
        rightPanel.add(pnlChat, BorderLayout.SOUTH);

        mainPanel.add(rightPanel, BorderLayout.EAST);

        // Bottom Panel
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

        // Center Board Panel
        JPanel pnlCenter = new JPanel();
        pnlCenter.setLayout(new BoxLayout(pnlCenter, BoxLayout.Y_AXIS));
        pnlCenter.setBackground(new Color(210, 180, 140));

        // Turn Box
        JPanel pnlTurnBox = new JPanel();
        pnlTurnBox.setPreferredSize(new Dimension(880, 45));
        pnlTurnBox.setMaximumSize(new Dimension(880, 45));
        pnlTurnBox.setBackground(Color.WHITE);
        pnlTurnBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        lblTurnBox = new JLabel(strCurrentTurn + "'s turn", SwingConstants.CENTER);
        lblTurnBox.setFont(new Font("Arial", Font.BOLD, 18));
        pnlTurnBox.add(lblTurnBox);
        pnlCenter.add(pnlTurnBox);
        pnlCenter.add(Box.createVerticalStrut(10));

        // Board Grid
        boardPanel = new JPanel(new GridLayout(5, 5, 15, 15));
        boardPanel.setBackground(new Color(139, 90, 43));
        btnWord = new JButton[5][5];
        for (int intRow = 0; intRow < 5; intRow++)
            for (int intCol = 0; intCol < 5; intCol++) {
                btnWord[intRow][intCol] = new JButton("WORD");
                btnWord[intRow][intCol].setFont(new Font("Arial", Font.BOLD, 18));
                btnWord[intRow][intCol].setBackground(new Color(245, 235, 200));
                btnWord[intRow][intCol].setFocusPainted(false);
                btnWord[intRow][intCol].setOpaque(true);
                btnWord[intRow][intCol].setBorderPainted(false);
                btnWord[intRow][intCol].addActionListener(this);

                // Initialize animation colors
                colCurrent[intRow][intCol] = btnWord[intRow][intCol].getBackground();
                colTarget[intRow][intCol] = btnWord[intRow][intCol].getBackground();

                boardPanel.add(btnWord[intRow][intCol]);
            }
        pnlCenter.add(boardPanel);
        pnlCenter.add(Box.createVerticalStrut(10));

        // Hint row
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

        pnlCenter.add(pnlHintRow);

        mainPanel.add(pnlCenter, BorderLayout.CENTER);

        theFrame.setSize(1280, 720);
        theFrame.setResizable(false);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setLocationRelativeTo(null);
        theFrame.setVisible(true);

        setupLobby();
    }

    void setupBoardUI() {
        for (int intRow = 0; intRow < 5; intRow++)
            for (int intCol = 0; intCol < 5; intCol++)
                btnWord[intRow][intCol].setText(strWords[intRow][intCol]);

        updateTargetColors();
    }

    // Animation timer
    void setupAnimationTimer() {
        animationTimer = new javax.swing.Timer(1000 / intFPS, e -> updateAnimations());
        animationTimer.start();
    }

    void updateAnimations() {
        boolean blnUpdated = false;
        // Animate buttons
        for (int intRow = 0; intRow < 5; intRow++) {
            for (int intCol = 0; intCol < 5; intCol++) {
                Color colCur = colCurrent[intRow][intCol];
                Color colTgt = colTarget[intRow][intCol];
                if (!colCur.equals(colTgt)) {
                    colCurrent[intRow][intCol] = blendColors(colCur, colTgt, fltColorStep);
                    btnWord[intRow][intCol].setBackground(colCurrent[intRow][intCol]);
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

    Color blendColors(Color colStart, Color colEnd, float fltStep) {
        float fltR = colStart.getRed() + (colEnd.getRed() - colStart.getRed()) * fltStep;
        float fltG = colStart.getGreen() + (colEnd.getGreen() - colStart.getGreen()) * fltStep;
        float fltB = colStart.getBlue() + (colEnd.getBlue() - colStart.getBlue()) * fltStep;
        return new Color(clamp(fltR), clamp(fltG), clamp(fltB));
    }

    int clamp(float fltVal) {
        return Math.min(255, Math.max(0, Math.round(fltVal)));
    }

    void updateTargetColors() {
        for (int intRow = 0; intRow < 5; intRow++) {
            for (int intCol = 0; intCol < 5; intCol++) {
                if (blnRevealed[intRow][intCol]) {
                    colTarget[intRow][intCol] = getColorFromString(strColors[intRow][intCol]);
                } else if (strMyRole.equals("SPYMASTER") && blnOverlayOn) {
                    colTarget[intRow][intCol] = getColorFromString(strColors[intRow][intCol]);
                } else {
                    colTarget[intRow][intCol] = new Color(245, 235, 200);
                }
            }
        }
    }

    Color getColorFromString(String strVal) {
        switch (strVal) {
            case "RED": return new Color(170, 60, 50);
            case "BLUE": return new Color(60, 130, 160);
            case "BLACK": return new Color(90, 90, 90);
            default: return new Color(200, 190, 170);
        }
    }

    void animateScores() {

    }

    // Networking and log
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
            for (int intCount4 = 0; intCount4 < 10; intCount4++) {
                if (ssm.connect()) {
                    blnConnected = true;
                    break;
                }
                try { Thread.sleep(200); } catch (Exception ignored) {}
            }
            if (blnConnected) {
                log("Connected to server: " + strServerIP);
                sendNetwork("REQUESTBOARD");
            } else log("Failed to connect to server");
        }
    }

    void sendBoardToClient() {
        StringBuilder strW = new StringBuilder();
        StringBuilder strC = new StringBuilder();

        for (int intRow = 0; intRow < 5; intRow++)
            for (int intCol = 0; intCol < 5; intCol++) {
                strW.append(strWords[intRow][intCol]);
                strC.append(strColors[intRow][intCol]);
                if (!(intRow == 4 && intCol == 4)) {
                    strW.append(",");
                    strC.append(",");
                }
            }

        sendNetwork("BOARD:" + strW.toString() + "|" + strC.toString());
    }

    void handleNetwork() {
        String strMsg = ssm.readText();
        if (strMsg == null) return;

        log("Network: " + strMsg);

        if (strMsg.equals("REQUESTBOARD") && blnIsServer) {
            sendBoardToClient();
        }

        if (strMsg.startsWith("BOARD:")) {
            String[] arrParts = strMsg.substring(6).split("\\|");
            String[] arrW = arrParts[0].split(",");
            String[] arrC = arrParts[1].split(",");

            int intIdx = 0;
            for (int intRow = 0; intRow < 5; intRow++)
                for (int intCol = 0; intCol < 5; intCol++) {
                    strWords[intRow][intCol] = arrW[intIdx];
                    strColors[intRow][intCol] = arrC[intIdx];
                    intIdx++;
                }

            setupBoardUI();
            blnGameStarted = true;

            if (strMyRole.equals("SPYMASTER")) {
                updateTargetColors();
            }
        }

        if (strMsg.startsWith("CLICK:")) {
            String[] arrP = strMsg.split(":");
            int intRow = Integer.parseInt(arrP[1]);
            int intCol = Integer.parseInt(arrP[2]);
            if (!blnRevealed[intRow][intCol]) {
                blnRevealed[intRow][intCol] = true;
                btnWord[intRow][intCol].setEnabled(false);

                updateTargetColors();

                if (strColors[intRow][intCol].equals("RED")) intRedLeft--;
                if (strColors[intRow][intCol].equals("BLUE")) intBlueLeft--;

                animateScores();

                log(getOperativeLabel() + " taps " + strWords[intRow][intCol]);
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
            String[] arrParts = strMsg.split(":");
            if (arrParts.length == 3) {
                String strWord = arrParts[1];
                String strNumber = arrParts[2];
                lblHint.setText(strWord);
                lblHintNumber.setText(strNumber);

                log(getSpymasterLabel() + " gives clue " + strWord + " " + strNumber);
            }
        }

        if (strMsg.startsWith("CHAT:")) {
            log("Player: " + strMsg.substring(5));
        }

        if (strMsg.startsWith("START:")) {
            String[] arrParts = strMsg.split(":");
            strMyTeam = arrParts[1];
            strMyRole = arrParts[2];
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
        if (txtGameLog != null) txtGameLog.append(strMsg + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainCodenamesGame("Codenames"));
    }
}

