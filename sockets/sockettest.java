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

// Chat message format
// Currently - No format just raw text
// Ex: "Jasper is always late for class"
// Future - username,date,chatmessage
// Ex: "cadawas,Dec17,Jasper is always late for class"

// Ex Encoded message
// bbszbo jt nje

public class sockettest implements ActionListener {
    // Properties
    JFrame theframe = new JFrame("Sockets");
    JPanel thepanel = new JPanel();
    JTextField theField = new JTextField();
    JTextArea theArea = new JTextArea();
    JScrollPane theScroll = new JScrollPane(theArea);
    JButton butClient = new JButton("Client mode");
    JButton butServer = new JButton("Server mode");
    JButton butConnect = new JButton("Connect");
    SuperSocketMaster ssm = null;

    // Methods
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == theField) {
            System.out.println("Text Field Action");
            String strLine = theField.getText();
            ssm.sendText(strLine);
            theArea.append(strLine + "\n");
        } else if (evt.getSource() == butClient) {
            System.out.println("Client Button Action");
            ssm = new SuperSocketMaster(theField.getText(), 6767, this);
            theField.setText("");
        } else if (evt.getSource() == butServer) {
            System.out.println("Server Button Action");
            ssm = new SuperSocketMaster(6767, this);
        } else if (evt.getSource() == butConnect) {
            System.out.println("Connect Button Action");
            ssm.connect();
        } else if (evt.getSource() == ssm) {
            String strLine = ssm.readText();
            theArea.append(strLine + "\n");

        }

    }

    // Constructor
    public sockettest() {
        thepanel.setPreferredSize(new Dimension(300, 600));
        thepanel.setLayout(null);

        theScroll.setSize(300, 200);
        theScroll.setLocation(0, 0);
        thepanel.add(theScroll);

        theField.setSize(300, 100);
        theField.setLocation(0, 200);
        theField.addActionListener(this);
        thepanel.add(theField);

        butClient.setSize(300, 100);
        butClient.setLocation(0, 300);
        butClient.addActionListener(this);
        thepanel.add(butClient);

        butServer.setSize(300, 100);
        butServer.setLocation(0, 400);
        butServer.addActionListener(this);
        thepanel.add(butServer);

        butConnect.setSize(300, 100);
        butConnect.setLocation(0, 500);
        butConnect.addActionListener(this);
        thepanel.add(butConnect);

        theframe.setContentPane(thepanel);
        theframe.pack();
        theframe.setVisible(true);
    }

    // Main method
    public static void main(String[] args) {
        new sockettest();
    }

}
