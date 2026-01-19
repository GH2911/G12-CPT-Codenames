
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class testcodenames implements ActionListener {

    JFrame theFrame;
    JPanel mainPanel, boardPanel, leftPanel, rightPanel, topPanel, bottomPanel;
    JButton[][] wordButtons;

    JPanel rolePanel;
    JButton btnSpy, btnOp;

    final int intROWS = 5;
    final int intCOLS = 5;

    String myRole = ""; // "SPYMASTER" or "OPERATIVE"

    String[][] words = new String[5][5];
    String[][] colors = new String[5][5]; // RED, BLUE, NEUTRAL, ASSASSIN

    @Override
    public void actionPerformed(ActionEvent evt) {

        // ROLE PICK
        if (evt.getSource() == btnSpy) {
            myRole = "SPYMASTER";
            startGame();
            return;
        }

        if (evt.getSource() == btnOp) {
            myRole = "OPERATIVE";
            startGame();
            return;
        }

        // BOARD CLICKS (OPERATIVES ONLY)
        if (!myRole.equals("OPERATIVE")) return;

        for (int r = 0; r < intROWS; r++) {
            for (int c = 0; c < intCOLS; c++) {
                if (evt.getSource() == wordButtons[r][c]) {
                    revealWord(r, c);
                }
            }
        }
    }

    public testcodenames(String strTitle) {

        theFrame = new JFrame(strTitle);
        theFrame.setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(210,180,140));
        theFrame.setContentPane(mainPanel);

        // TOP
        topPanel = new JPanel();
        topPanel.add(new JLabel("Choose your role"));
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ROLE PANEL
        rolePanel = new JPanel();
        btnSpy = new JButton("SPYMASTER");
        btnOp = new JButton("OPERATIVE");

        btnSpy.addActionListener(this);
        btnOp.addActionListener(this);

        rolePanel.add(btnSpy);
        rolePanel.add(btnOp);
        mainPanel.add(rolePanel, BorderLayout.CENTER);

        // FRAME SETTINGS
        theFrame.setSize(1280,720);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setLocationRelativeTo(null);
        theFrame.setVisible(true);
    }

-    void startGame() {
        mainPanel.remove(rolePanel);
        topPanel.removeAll();
        topPanel.add(new JLabel(myRole.equals("SPYMASTER") ?
                "You see the colours" : "Guess the words"));

        generateWords();
        generateColors();
        buildBoard();

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    void buildBoard() {

        boardPanel = new JPanel(new GridLayout(5,5,15,15));
        boardPanel.setBackground(new Color(139,90,43));

        wordButtons = new JButton[5][5];

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {

                JButton btn = new JButton(words[r][c]);
                btn.setFont(new Font("Arial", Font.BOLD, 18));
                btn.setBackground(getHiddenColor(r,c));
                btn.setFocusPainted(false);
                btn.addActionListener(this);

                wordButtons[r][c] = btn;
                boardPanel.add(btn);
            }
        }

        mainPanel.add(boardPanel, BorderLayout.CENTER);
    }

    void revealWord(int r, int c) {

        switch (colors[r][c]) {
            case "RED": wordButtons[r][c].setBackground(Color.RED); break;
            case "BLUE": wordButtons[r][c].setBackground(Color.BLUE); break;
            case "NEUTRAL": wordButtons[r][c].setBackground(Color.LIGHT_GRAY); break;
            case "ASSASSIN":
                wordButtons[r][c].setBackground(Color.BLACK);
                JOptionPane.showMessageDialog(theFrame, "ASSASSIN! GAME OVER");
                break;
        }

        wordButtons[r][c].setEnabled(false);
    }

    Color getHiddenColor(int r, int c) {

        if (myRole.equals("SPYMASTER")) {
            switch (colors[r][c]) {
                case "RED": return new Color(255,150,150);
                case "BLUE": return new Color(150,150,255);
                case "ASSASSIN": return Color.DARK_GRAY;
            }
        }
        return new Color(245,235,200);
    }

    void generateWords() {

        ArrayList<String> allWords = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("wordlist.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                allWords.add(line);
            }
        } catch (IOException e) {
            System.out.println("Word file error");
        }

        Collections.shuffle(allWords);

        int index = 0;
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                words[r][c] = allWords.get(index++);
    }

    void generateColors() {

        ArrayList<String> bag = new ArrayList<>();

        for (int i = 0; i < 9; i++) bag.add("RED");
        for (int i = 0; i < 8; i++) bag.add("BLUE");
        for (int i = 0; i < 7; i++) bag.add("NEUTRAL");
        bag.add("ASSASSIN");

        Collections.shuffle(bag);

        int i = 0;
        for (int r = 0; r < 5; r++)
            for (int c = 0; c < 5; c++)
                colors[r][c] = bag.get(i++);
    }

    public static void main(String[] args) {
        new testcodenames("Codenames");
    }
}
