package sockets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class sockettest implements ActionListener{
    //Properties
    JFrame theFrame = new JFrame("Sockets");
    JPanel thePanel = new JPanel();
    JTextField theField = new JTextField();
    JTextArea theArea = new JTextArea();
    JScrollPane theScroll = new JScrollPane(theArea);
    JButton butClient = new JButton("Client mode");
    JButton butServer = new JButton("Server mode");
    JButton butConnect = new JButton("Connect");
    SuperSocketMaster ssm = new SuperSocketMaster(0, null);

    //Methods
    public void actionPerformed (ActionEvent evt){
        if(evt.getSource() == theField){
            System.out.println("Text Field Action");
        }else if(evt.getSource() == butClient){
            System.out.println("Client Button Action");
        }else if(evt.getSource() == butServer){
            System.out.println("Server button Action");
        }else if(evt.getSource() == butConnect){
            System.out.println("Connect Button Action");
        }else if(evt.getSource() == ssm){
            String strLine = ssm.readText();
            theArea.append(strLine+"\n");
        }
    }

    //Constructor
    public sockettest(){
        thePanel.setPreferredSize(new Dimension(300, 600));
        thePanel.setLayout(null);

        theScroll.setSize(300, 200);
        theScroll.setLocation(0,0);
        thePanel.add(theScroll);

        theField.setSize(300,100);
        theField.setLocation(0,200);
        theField.addActionListener(this);
        thePanel.add(theScroll);

        butClient.setSize(300, 100);
        butClient.setLocation(0,300);
        butClient.addActionListener(this);
        thePanel.add(butClient);

        butServer.setSize(300,100);
        butServer.setLocation(0,400);
        butServer.addActionListener(this);
        thePanel.add(butServer);

        butConnect.setSize(300,100);
        butConnect.setLocation(0,500);
        butConnect.addActionListener(this);
        thePanel.add(butConnect);

        theFrame.setContentPane(thePanel);
        theFrame.pack();
        theFrame.setVisible(true);

    }

    //Main Method
    public static void main(String[] args){
        new sockettest();
    }
    
}
