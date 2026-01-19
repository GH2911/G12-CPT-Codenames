package MainProgram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class codenames implements ActionListener {

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

    // Methods
    @Override
    public void actionPerformed(ActionEvent evt) {

        for (int intRow = 0; intRow < intROWS; intRow++) {
            for (int intCol = 0; intCol < intCOLS; intCol++) {

                if (evt.getSource() == wordButtons[intRow][intCol]) {

                    int intBoxNumber = (intRow * intCOLS) + intCol + 1;

                    System.out.println(
                        "Clicked box #" + intBoxNumber +
                        " (row " + (intRow + 1) +
                        ", col " + (intCol + 1) + ")"
                    );

                    // Fade the card to show it was selected
                    wordButtons[intRow][intCol].setBackground(new Color(220, 210, 180));
                    wordButtons[intRow][intCol].setOpaque(true);
                    wordButtons[intRow][intCol].setContentAreaFilled(true);
                    wordButtons[intRow][intCol].setBorderPainted(false);

                    // Prevent re-click
                    wordButtons[intRow][intCol].setEnabled(false);
                }
            }
        }
    }

    // Constructor
    public codenames(String strTitle) {

        // Frame
        this.theFrame = new JFrame(strTitle);
        this.theFrame.setLayout(new BorderLayout());

        // Main panel (light brown)
        this.mainPanel = new JPanel(new BorderLayout());
        this.mainPanel.setBackground(new Color(210, 180, 140));
        this.theFrame.setContentPane(this.mainPanel);

        // Top panel
        this.topPanel = new JPanel();
        this.topPanel.setPreferredSize(new Dimension(1280, 60));
        this.topPanel.setBackground(new Color(210, 180, 140));
        this.topPanel.add(new JLabel("Codenames Game"));
        this.mainPanel.add(this.topPanel, BorderLayout.NORTH);

        // Bottom panel
        this.bottomPanel = new JPanel();
        this.bottomPanel.setPreferredSize(new Dimension(1280, 40));
        this.bottomPanel.setBackground(new Color(210, 180, 140));
        this.mainPanel.add(this.bottomPanel, BorderLayout.SOUTH);

        // Board panel (dark brown)
        this.boardPanel = new JPanel();
        this.boardPanel.setLayout(new GridLayout(intROWS, intCOLS, 15, 15));
        this.boardPanel.setBackground(new Color(139, 90, 43));

        this.wordButtons = new JButton[intROWS][intCOLS];
        for (int intRow = 0; intRow < intROWS; intRow++) {
            for (int intCol = 0; intCol < intCOLS; intCol++) {

                this.wordButtons[intRow][intCol] = new JButton("WORD");
                this.wordButtons[intRow][intCol].setFont(new Font("Arial", Font.BOLD, 18));

                // Beige cards
                this.wordButtons[intRow][intCol].setBackground(new Color(245, 235, 200));
                this.wordButtons[intRow][intCol].setOpaque(true);
                this.wordButtons[intRow][intCol].setContentAreaFilled(true);
                this.wordButtons[intRow][intCol].setBorderPainted(true);
                this.wordButtons[intRow][intCol].setFocusPainted(false);

                this.wordButtons[intRow][intCol].addActionListener(this);
                this.boardPanel.add(this.wordButtons[intRow][intCol]);
            }
        }

        // Center container
        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.X_AXIS));
        centerContainer.setBackground(new Color(210, 180, 140));

        // Left panel
        this.leftPanel = new JPanel();
        this.leftPanel.setLayout(new BoxLayout(this.leftPanel, BoxLayout.Y_AXIS));
        this.leftPanel.setPreferredSize(new Dimension(180, 620));
        this.leftPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlRedTeam = new JPanel();
        pnlRedTeam.setBackground(new Color(255, 200, 200));
        pnlRedTeam.setMaximumSize(new Dimension(180, 310));
        pnlRedTeam.add(new JLabel("RED TEAM"));
        this.leftPanel.add(pnlRedTeam);

        JPanel pnlEmptyLeft = new JPanel();
        pnlEmptyLeft.setBackground(new Color(210, 180, 140));
        pnlEmptyLeft.setMaximumSize(new Dimension(180, 310));
        this.leftPanel.add(pnlEmptyLeft);

        centerContainer.add(this.leftPanel);

        // Board
        this.boardPanel.setPreferredSize(new Dimension(920, 620));
        centerContainer.add(this.boardPanel);

        // Right panel
        this.rightPanel = new JPanel();
        this.rightPanel.setLayout(new BoxLayout(this.rightPanel, BoxLayout.Y_AXIS));
        this.rightPanel.setPreferredSize(new Dimension(180, 620));
        this.rightPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlBlueTeam = new JPanel();
        pnlBlueTeam.setBackground(new Color(200, 200, 255));
        pnlBlueTeam.setMaximumSize(new Dimension(180, 310));
        pnlBlueTeam.add(new JLabel("BLUE TEAM"));
        this.rightPanel.add(pnlBlueTeam);

        JPanel pnlGameLog = new JPanel();
        pnlGameLog.setBackground(Color.WHITE);
        pnlGameLog.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlGameLog.setMaximumSize(new Dimension(180, 310));
        this.rightPanel.add(pnlGameLog);

        centerContainer.add(this.rightPanel);

        this.mainPanel.add(centerContainer, BorderLayout.CENTER);

        // Frame settings
        this.theFrame.setSize(1280, 720);
        this.theFrame.setResizable(false);
        this.theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.theFrame.setLocationRelativeTo(null);
        this.theFrame.setVisible(true);
    }

    // Main Method
    public static void main(String[] args) {
        new codenames("Codenames");
    }
}
