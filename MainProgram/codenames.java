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

                    wordButtons[intRow][intCol].setBackground(new Color(220, 210, 180));
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

        // Main panel
        this.mainPanel = new JPanel(new BorderLayout());
        this.mainPanel.setBackground(new Color(210, 180, 140));
        this.theFrame.setContentPane(this.mainPanel);

        // Top panel
        this.topPanel = new JPanel();
        this.topPanel.setPreferredSize(new Dimension(1280, 60));
        this.topPanel.setBackground(new Color(210, 180, 140));
        this.mainPanel.add(this.topPanel, BorderLayout.NORTH);

        // Bottom panel
        this.bottomPanel = new JPanel();
        this.bottomPanel.setPreferredSize(new Dimension(1280, 40));
        this.bottomPanel.setBackground(new Color(210, 180, 140));
        this.mainPanel.add(this.bottomPanel, BorderLayout.SOUTH);

        // Board panel (5x5 grid)

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

        // Center container

        JPanel centerContainer = new JPanel();
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.X_AXIS));
        centerContainer.setBackground(new Color(210, 180, 140));

        // Left panel (RED TEAM)

        this.leftPanel = new JPanel(new BorderLayout());
        this.leftPanel.setPreferredSize(new Dimension(200, 620));
        this.leftPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlRedTeam = new JPanel(new BorderLayout());
        pnlRedTeam.setBackground(new Color(170, 60, 50));
        pnlRedTeam.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblRedCount = new JLabel("9");
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

        for (Component c : pnlRedText.getComponents()) {
            c.setForeground(Color.WHITE);
        }

        pnlRedTeam.add(pnlRedText, BorderLayout.WEST);
        pnlRedTeam.add(lblRedCount, BorderLayout.EAST);

        this.leftPanel.add(pnlRedTeam, BorderLayout.NORTH);
        centerContainer.add(this.leftPanel);

        // Board wrapper (TOP / GRID / BOTTOM)

        JPanel pnlBoardWrapper = new JPanel();
        pnlBoardWrapper.setLayout(new BoxLayout(pnlBoardWrapper, BoxLayout.Y_AXIS));
        pnlBoardWrapper.setBackground(new Color(210, 180, 140));
        pnlBoardWrapper.setPreferredSize(new Dimension(880, 620));

        // Turn Box
        JPanel pnlTurnBox = new JPanel();
        pnlTurnBox.setPreferredSize(new Dimension(880, 45));
        pnlTurnBox.setMaximumSize(new Dimension(880, 45));
        pnlTurnBox.setBackground(Color.WHITE);
        pnlTurnBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        pnlBoardWrapper.add(pnlTurnBox);

        pnlBoardWrapper.add(Box.createVerticalStrut(10));

        // Grid 
        pnlBoardWrapper.add(this.boardPanel);

        pnlBoardWrapper.add(Box.createVerticalStrut(10));

        // Hint and Number box
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

        pnlBoardWrapper.add(pnlHintRow);

        centerContainer.add(pnlBoardWrapper);

        // Right panel (BLUE TEAM)

        this.rightPanel = new JPanel(new BorderLayout());
        this.rightPanel.setPreferredSize(new Dimension(200, 620));
        this.rightPanel.setBackground(new Color(210, 180, 140));

        JPanel pnlBlueTeam = new JPanel(new BorderLayout());
        pnlBlueTeam.setBackground(new Color(60, 130, 160));
        pnlBlueTeam.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblBlueCount = new JLabel("8");
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

        for (Component c : pnlBlueText.getComponents()) {
            c.setForeground(Color.WHITE);
        }

        pnlBlueTeam.add(lblBlueCount, BorderLayout.WEST);
        pnlBlueTeam.add(pnlBlueText, BorderLayout.EAST);

        this.rightPanel.add(pnlBlueTeam, BorderLayout.NORTH);

        JPanel pnlGameLog = new JPanel();
        pnlGameLog.setBackground(new Color(230, 210, 200));
        pnlGameLog.setBorder(BorderFactory.createTitledBorder("Game log"));
        this.rightPanel.add(pnlGameLog, BorderLayout.CENTER);

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
