import javax.swing.*;
import java.awt.*;

public class testCodenameGrid extends JPanel {

    static final int intpwidth = 1280;
    static final int intpheight = 720;
    static final int intgridsize = 5;

    public testCodenameGrid() {
        setPreferredSize(new Dimension(intpwidth, intpheight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellSize = 100; // size of each card
        int intgridWidth = intgridsize * cellSize;
        int intgridHeight = intgridsize * cellSize;

        int intstartX = (intpwidth - intgridWidth) / 2;
        int intstartY = (intpheight - intgridHeight) / 2;

        g.setColor(Color.BLACK);

        // Draw grid
        for (int row = 0; row < intgridsize; row++) {
            for (int col = 0; col < intgridsize; col++) {
                int intx = intstartX + col * cellSize;
                int inty = intstartY + row * cellSize;
                g.drawRect(intx, inty, cellSize, cellSize);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Codenames Grid");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new testCodenameGrid());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
