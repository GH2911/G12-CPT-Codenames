import javax.swing.*;

import sockets.SuperSocketMaster;

import java.awt.*;
import java.awt.event.*;

// SOCKET
// Make sure SuperSocketMaster.java is in the same folder
public class testdoc implements ActionListener {

    // Properties
    public JFrame theFrame;
    public JPanel mainPanel;
    public JPanel boardPanel;
    public JPanel leftPanel;
    public JPanel rightPanel;
    public JPanel topPanel;
    public JPanel bottomPanel;
    public JButton[][] wordButtons;

    public final int intROWS = 5;
    public final int intCOLS = 5;

    // SOCKET
    SuperSocketMaster ssm;
    JTextArea gameLog;
    JButton btnServer;
    JButton btnClient;

    // Methods
    @Override
    public void actionPerformed(ActionEvent evt) {

        // SOCKET — received network message
        if (evt.getSource() == ssm) {
            String msg = ssm.readText();
            gameLog.append(msg + "\n");

            if (msg.startsWith("CLICK")) {
                String[] parts = msg.split(",");
                int r = Integer.parseInt(parts[1]);
                int c = Integer.parseInt(parts[2]);

                wordButtons[r][c].setBackground(new Color(220, 210, 180));
                wordButtons[r][c].setEnabled(false);
            }
            return;
        }

        // SOCKET — server button
        if (evt.getSource() == btnServer) {
            ssm = new SuperSocketMaster(1337, this);
            ssm.connect();
            gameLog.append("Server started\n");
            return;
        }

        // SOCKET — client button
        if (evt.getSource() == btnClient) {
            String ip = JOptionPane.showInputDialog("Enter server IP:");
            ssm = new SuperSocketMaster(ip, 1337, this);
            ssm.connect();
            gameLog.append("Connected to server\n");
            return;
        }

        // ORIGINAL BUTTON LOGIC (unchanged)
        for (int intRow = 0; intRow < intROWS; intRow++) {
            for (int intCol = 0; intCol < intCOLS; intCol++) {
                if (evt.getSource() == wordButtons[intRow][intCol]) {

                    int intBoxNumber = (intRow * intCOLS) + intCol + 1;
                    System.out.println(
                        "Clicked box #" + intBoxNumber +
                        " (row " + (intRow + 1) + ", col " + (intCol + 1) + ")"
                    );

                    wordButtons[intRow][intCol].setBackground(new Color(220, 210, 180));
                    wordButtons[intRow][intCol].setEnabled(false);

                    // SOCKET — send click
                    if (ssm != null) {
                        ssm.sendText("CLICK," + intRow + "," + intCol);
                    }
                }
            }
        }
    }

    // Constructor
    public testdoc(String strTitle) {

        this.theFrame = new JFrame(strTitle);
        this.theFrame.setLayout(new BorderLayout());

        this.mainPanel = new JPanel(new BorderLayout());
        this.mainPanel.setBackground(new Color(210, 180, 140));
        this.theFrame.setContentPane(this.mainPanel);

        this.topPanel = new JPanel();
        this.topPanel.setPreferredSize(new Dimension(1280, 60));
        this.topPanel.setBackground(new Color(210, 180, 140));
        this.topPanel.add(new JLabel("Give your operatives a clue."));
        this.mainPanel.add(this.topPanel, BorderLayout.NORTH);

        // SOCKET — server / client buttons
        btnServer = new JButton("Host Game");
        btnClient = new JButton("Join Game");
        btnServer.addActionListener(this);
        btnClient.addActionListener(this);
        this.topPanel.add(btnServer);
        this.topPanel.add(btnClient);

        this.bottomPanel = new JPanel();
        this.bottomPanel.setPreferredSize(new Dimension(1280, 40));
        this.bottomPanel.setBackground(new Color(210, 180, 140));
        this.mainPanel.add(this.bottomPanel, BorderLayout.SOUTH);

        this.boardPanel = new JPanel(new GridLayout(intROWS, intCOLS, 15, 15));
        this.boardPanel.setBackground(new Color(139, 90, 43));

        this.wordButtons = new JButton[intROWS][intCOLS];
        for (int intRow = 0; intRow < intROWS; intRow++) {
            for (int intCol = 0; intCol < intCOLS; intCol++) {
                this.wordButtons[intRow][intCol] = new JButton("WORD");
                this.wordButtons[intRow][intCol].setFont(new Font("Arial", Font.BOLD, 18));
                this.wordButtons[intRow][intCol].setBackground(new Color(245, 235, 200));
                this.wordButtons[intRow][intCol].setFocusPainted(false);
                this.wordButtons[intRow][intCol].addActionListener(this);
                this.boardPanel.add(this.wordButtons[intRow][intCol]);
            }
        }

        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.X_AXIS));
        centerContainer.setBackground(new Color(210, 180, 140));

        this.leftPanel = new JPanel();
        this.leftPanel.setPreferredSize(new Dimension(200, 620));
        this.leftPanel.setBackground(new Color(210, 180, 140));
        centerContainer.add(this.leftPanel);

        this.boardPanel.setPreferredSize(new Dimension(880, 620));
        centerContainer.add(this.boardPanel);

        this.rightPanel = new JPanel(new BorderLayout());
        this.rightPanel.setPreferredSize(new Dimension(200, 620));
        this.rightPanel.setBackground(new Color(210, 180, 140));

        // SOCKET — game log
        gameLog = new JTextArea();
        gameLog.setEditable(false);
        JScrollPane scroll = new JScrollPane(gameLog);
        scroll.setBorder(BorderFactory.createTitledBorder("Game log"));
        this.rightPanel.add(scroll, BorderLayout.CENTER);

        centerContainer.add(this.rightPanel);
        this.mainPanel.add(centerContainer, BorderLayout.CENTER);

        this.theFrame.setSize(1280, 720);
        this.theFrame.setResizable(false);
        this.theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.theFrame.setLocationRelativeTo(null);
        this.theFrame.setVisible(true);
    }

    public static void main(String[] args) {
        new testdoc("Codenames");
    }
}
