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

                    // fade / mark as selected
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
        this.topPanel.add(new JLabel("Give your operatives a clue."));
        this.mainPanel.add(this.topPanel, BorderLayout.NORTH);

        // Bottom panel
        this.bottomPanel = new JPanel();
        this.bottomPanel.setPreferredSize(new Dimension(1280, 40));
        this.bottomPanel.setBackground(new Color(210, 180, 140));
        this.mainPanel.add(this.bottomPanel, BorderLayout.SOUTH);

        // Board panel
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

        // left panel
        this.leftPanel = new JPanel();
        this.leftPanel.setPreferredSize(new Dimension(200, 620));
        this.leftPanel.setBackground(new Color(210, 180, 140));
        this.leftPanel.setLayout(new BorderLayout());

        JPanel pnlRedTeam = new JPanel(new BorderLayout());
        pnlRedTeam.setBackground(new Color(170, 60, 50));
        pnlRedTeam.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblRedCount = new JLabel("9");
        lblRedCount.setFont(new Font("Arial", Font.BOLD, 48));
        lblRedCount.setForeground(Color.WHITE);
        lblRedCount.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel pnlRedText = new JPanel();
        pnlRedText.setBackground(new Color(170, 60, 50));
        pnlRedText.setLayout(new BoxLayout(pnlRedText, BoxLayout.Y_AXIS));

        JLabel lblRedOperatives = new JLabel("Operative(s)");
        lblRedOperatives.setForeground(Color.WHITE);

        JLabel lblRedOpName = new JLabel("-");
        lblRedOpName.setForeground(Color.WHITE);

        JLabel lblRedSpymaster = new JLabel("Spymaster(s)");
        lblRedSpymaster.setForeground(Color.WHITE);

        JLabel lblRedSpyName = new JLabel("-");
        lblRedSpyName.setForeground(Color.WHITE);

        pnlRedText.add(lblRedOperatives);
        pnlRedText.add(lblRedOpName);
        pnlRedText.add(Box.createVerticalStrut(10));
        pnlRedText.add(lblRedSpymaster);
        pnlRedText.add(lblRedSpyName);

        pnlRedTeam.add(pnlRedText, BorderLayout.WEST);
        pnlRedTeam.add(lblRedCount, BorderLayout.EAST);

        this.leftPanel.add(pnlRedTeam, BorderLayout.NORTH);
        centerContainer.add(this.leftPanel);

        // board
        this.boardPanel.setPreferredSize(new Dimension(880, 620));
        centerContainer.add(this.boardPanel);

        // right panel
        this.rightPanel = new JPanel();
        this.rightPanel.setPreferredSize(new Dimension(200, 620));
        this.rightPanel.setBackground(new Color(210, 180, 140));
        this.rightPanel.setLayout(new BorderLayout());

        JPanel pnlBlueTeam = new JPanel(new BorderLayout());
        pnlBlueTeam.setBackground(new Color(60, 130, 160));
        pnlBlueTeam.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblBlueCount = new JLabel("8");
        lblBlueCount.setFont(new Font("Arial", Font.BOLD, 48));
        lblBlueCount.setForeground(Color.WHITE);
        lblBlueCount.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel pnlBlueText = new JPanel();
        pnlBlueText.setBackground(new Color(60, 130, 160));
        pnlBlueText.setLayout(new BoxLayout(pnlBlueText, BoxLayout.Y_AXIS));

        JLabel lblBlueOperatives = new JLabel("Operative(s)");
        lblBlueOperatives.setForeground(Color.WHITE);

        JLabel lblBlueOpName = new JLabel("-");
        lblBlueOpName.setForeground(Color.WHITE);

        JLabel lblBlueSpymaster = new JLabel("Spymaster(s)");
        lblBlueSpymaster.setForeground(Color.WHITE);

        JLabel lblBlueSpyName = new JLabel("-");
        lblBlueSpyName.setForeground(Color.WHITE);

        pnlBlueText.add(lblBlueOperatives);
        pnlBlueText.add(lblBlueOpName);
        pnlBlueText.add(Box.createVerticalStrut(10));
        pnlBlueText.add(lblBlueSpymaster);
        pnlBlueText.add(lblBlueSpyName);

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
