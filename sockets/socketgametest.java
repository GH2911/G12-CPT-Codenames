package sockets;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class socketgametest implements ActionListener {

    // Properties
    JFrame theFrame = new JFrame("Chat");
    JPanel thePanel = new JPanel();
    JTextArea theArea = new JTextArea();
    JTextField theField = new JTextField();
    JButton butServer = new JButton("Start Server");
    JButton butClient = new JButton("Connect as Client");

    SuperSocketMaster ssm = null;

    // Constructor
    public socketgametest() {
        thePanel.setLayout(null);
        thePanel.setPreferredSize(new Dimension(300, 400));

        theArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(theArea);
        scroll.setBounds(0, 0, 300, 250);
        thePanel.add(scroll);

        theField.setBounds(0, 250, 300, 50);
        theField.addActionListener(this);
        thePanel.add(theField);

        butServer.setBounds(0, 300, 150, 50);
        butServer.addActionListener(this);
        thePanel.add(butServer);

        butClient.setBounds(150, 300, 150, 50);
        butClient.addActionListener(this);
        thePanel.add(butClient);

        theFrame.setContentPane(thePanel);
        theFrame.pack();
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        theFrame.setVisible(true);
    }

    // Event Handling
    public void actionPerformed(ActionEvent evt) {

        // ENTER pressed → send message
        if (evt.getSource() == theField && ssm != null) {
            ssm.sendText(theField.getText());
            theField.setText("");
        }

        // Start server
        else if (evt.getSource() == butServer) {
            ssm = new SuperSocketMaster(1337, this);
            ssm.connect();
            theArea.append("Server started\n");
        }

        // Connect as client
        else if (evt.getSource() == butClient) {
            String ip = JOptionPane.showInputDialog("Enter Server IP:");
            ssm = new SuperSocketMaster(ip, 1337, this);
            ssm.connect();
            theArea.append("Connected to server\n");
        }

        // Incoming network message
        else if (evt.getSource() == ssm) {
            theArea.append(ssm.readText() + "\n");
        }
    }

    // Main
    public static void main(String[] args) {
        new sockettest();
    }
}