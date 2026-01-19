package sockets;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import java.net.*;

public class socketgametest implements ActionListener {

    // Properties
    JFrame theFrame = new JFrame("Sockets Chat");
    JPanel thePanel = new JPanel();
    JTextField theField = new JTextField();
    JTextArea theArea = new JTextArea();
    JScrollPane theScroll = new JScrollPane(theArea);

    JButton butClient = new JButton("Client mode");
    JButton butServer = new JButton("Server mode");

    SuperSocketMaster ssm;

    final int PORT = 12345;          // fixed port
    final String SERVER_IP = "localhost"; // change to other person's IP

    // Methods
    public void actionPerformed(ActionEvent evt) {

        if (evt.getSource() == theField) {
            String message = theField.getText();
            theArea.append("Me: " + message + "\n");
            ssm.sendText(message);
            theField.setText("");
        }

        else if (evt.getSource() == butServer) {
            theArea.append("Starting server...\n");
            ssm = new SuperSocketMaster(PORT, this);
        }

        else if (evt.getSource() == butClient) {
            theArea.append("Connecting to server...\n");
            ssm = new SuperSocketMaster(SERVER_IP, PORT, this);
        }

        else if (evt.getSource() == ssm) {
            String incoming = ssm.readText();
            theArea.append("Them: " + incoming + "\n");
        }
    }

    // Constructor
    public socketgametest() {

        thePanel.setPreferredSize(new Dimension(300, 600));
        thePanel.setLayout(null);

        theArea.setEditable(false);

        theScroll.setSize(300, 300);
        theScroll.setLocation(0, 0);
        thePanel.add(theScroll);

        theField.setSize(300, 50);
        theField.setLocation(0, 300);
        theField.addActionListener(this);
        thePanel.add(theField);

        butServer.setSize(300, 100);
        butServer.setLocation(0, 350);
        butServer.addActionListener(this);
        thePanel.add(butServer);

        butClient.setSize(300, 100);
        butClient.setLocation(0, 450);
        butClient.addActionListener(this);
        thePanel.add(butClient);

        theFrame.setContentPane(thePanel);
        theFrame.pack();
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setVisible(true);
    }

    // Main
    public static void main(String[] args) {
        new socketgametest();
    }
}