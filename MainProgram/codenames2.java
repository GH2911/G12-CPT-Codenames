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
        theFrame.setContentPane(mainPanel);


        topPanel = new JPanel(new GridLayout(3, 1));
        lblTurn = new JLabel("RED team's turn", SwingConstants.CENTER);
        lblClue = new JLabel("Waiting for clue...", SwingConstants.CENTER);
        lblTimer = new JLabel("Time left: 60", SwingConstants.CENTER);
        topPanel.add(lblTurn);
        topPanel.add(lblClue);
        topPanel.add(lblTimer);
        mainPanel.add(topPanel, BorderLayout.NORTH);


        rightPanel = new JPanel(new BorderLayout());
        gameLog = new JTextArea();
        gameLog.setEditable(false);
        JScrollPane logScroll = new JScrollPane(gameLog);
        rightPanel.add(logScroll, BorderLayout.CENTER);


        JPanel chatPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        btnSendChat = new JButton("Send");
        chatPanel.add(chatInput, BorderLayout.CENTER);
        chatPanel.add(btnSendChat, BorderLayout.EAST);
        rightPanel.add(chatPanel, BorderLayout.SOUTH);


        btnSendChat.addActionListener(e -> sendChat());
        chatInput.addActionListener(e -> sendChat());


        rightPanel.setPreferredSize(new Dimension(300, 0));
        mainPanel.add(rightPanel, BorderLayout.EAST);


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


        theFrame.setSize(1280, 720);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setLocationRelativeTo(null);
        theFrame.setVisible(true);


        setupLobby();
    }


    void setupBoardUI() {
        if (boardPanel != null) mainPanel.remove(boardPanel);


        boardPanel = new JPanel(new GridLayout(5, 5, 15, 15));
        boardPanel.setBackground(new Color(139, 90, 43));


        wordButtons = new JButton[5][5];
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                JButton b = new JButton(words[r][c]);
                b.setFont(new Font("Arial", Font.BOLD, 16));
                b.setFocusPainted(false);
                b.addActionListener(this);
                addHoverEffect(b);
                wordButtons[r][c] = b;
                boardPanel.add(b);
            }
        }


        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
        mainPanel.repaint();
    }


    void addHoverEffect(JButton b) {
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            }


            @Override
            public void mouseExited(MouseEvent e) {
                b.setBorder(UIManager.getBorder("Button.border"));
            }
        });
    }


        
    void updateBoardColors() {
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++) {
                if (revealed[r][c]) revealColor(r, c);
                else if (myRole.equals("SPYMASTER") && overlayOn) {
                    revealColor(r, c, wordButtons[r][c]);
                } else {
                    wordButtons[r][c].setBackground(new Color(245, 235, 200));
                }
            }
    }


    void revealColor(int r, int c) {
        revealColor(r, c, wordButtons[r][c]);
    }


    void revealColor(int r, int c, JButton b) {
        switch (colors[r][c]) {
            case "RED":
                b.setBackground(new Color(170, 60, 50));
                break;
            case "BLUE":
                b.setBackground(new Color(60, 130, 160));
                break;
            case "BLACK":
                b.setBackground(Color.BLACK);
                break;
            default:
                b.setBackground(new Color(200, 190, 170));
        }
    }


    // --------- NETWORK ----------
    void setupSocket() {
        if (isServer) {
            ssm = new SuperSocketMaster(1337, evt -> handleNetwork());
            log("Server started on port 1337");
        } else {
            ssm = new SuperSocketMaster(serverIP, 1337, evt -> handleNetwork());
            if (ssm.connect()) {
                log("Connected to server: " + serverIP);
            } else {
                log("Failed to connect to server");
            }
        }
    }


    void sendBoardToClient() {
        // board format: word1,word2,...,word25|color1,color2,...,color25
        StringBuilder w = new StringBuilder();
        StringBuilder c = new StringBuilder();


        for (int r = 0; r < 5; r++) {
            for (int col = 0; col < 5; col++) {
                w.append(words[r][col]).append(",");
                c.append(colors[r][col]).append(",");
            }
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
            for (int r = 0; r < 5; r++) {
                for (int col = 0; col < 5; col++) {
                    words[r][col] = w[idx];
                    colors[r][col] = c[idx];
                    idx++;
                }
            }
            setupBoardUI();
            updateBoardColors();
            gameStarted = true;
        }


        if (msg.startsWith("CLICK:")) {
            String[] p = msg.split(":");
            int r = Integer.parseInt(p[1]);
            int c = Integer.parseInt(p[2]);
            if (!revealed[r][c]) {
                revealed[r][c] = true;
                revealColor(r, c);
                wordButtons[r][c].setEnabled(false);
            }
        }


        if (msg.startsWith("ENDTURN:")) {
            currentTurn = msg.split(":")[1];
            lblTurn.setText(currentTurn + " team's turn");
            lblClue.setText("Waiting for clue...");
            timeLeft = 60;
        }


        if (msg.startsWith("CLUE:")) {
            lblClue.setText("Clue: " + msg.substring(5));
        }


        if (msg.startsWith("CHAT:")) {
            log("Opponent: " + msg.substring(5));
        }


        if (msg.startsWith("START:")) {
            // client receives team/role
            String[] parts = msg.split(":");
            myTeam = parts[1];
            myRole = parts[2];
            log("Assigned: " + myTeam + " " + myRole);
        }
    }


    void sendNetwork(String msg) {
        if (ssm != null) ssm.sendText(msg);
    }


    void log(String msg) {
        if (gameLog != null) {
            gameLog.append(msg + "\n");
        }
    }


    void sendChat() {
        String text = chatInput.getText().trim();
        if (text.isEmpty()) return;
        log("You: " + text);
        sendNetwork("CHAT:" + text);
        chatInput.setText("");
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new codenames2("Codenames"));
    }
}



