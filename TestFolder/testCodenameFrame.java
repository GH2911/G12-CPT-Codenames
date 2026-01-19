import javax.swing.*;
import java.awt.*;

public class testCodenameFrame{
    // Properties
    public JFrame theFrame;
    public JPanel thePanel;
    // Methods

    // Constructor
    public testCodenameFrame(){
        theFrame = new JFrame("Test Frame");
        thePanel = new JPanel();
        thePanel.setLayout(null);
        thePanel.setPreferredSize(new Dimension(1280, 720));

        theFrame.setContentPane(thePanel);
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.pack();
        theFrame.setVisible(true);

    }
    // Main Method
    public static void main(String[] args){
        new testCodenameFrame();

    }
}