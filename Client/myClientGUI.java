/**
 * The client as a GUI
 *
 * @author Dr Richard Jiang; minor edits by Dr Mark C. Sinclair
 * further edits by Jo Binns
 * 
 * @version 6th January 2020
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class myClientGUI extends JFrame implements ActionListener, WindowListener {
    //for serialization of the class
    private static final long serialVersionUID = 1L;
    //to show connected
    private boolean connected;
    // the login, logout and who is in buttons
    private JButton login, logout, whoIsIn, readFile;
    // JTextArea for the chat room
    private JTextArea chat;
    // The port number, server address, user name and message
    private JTextField tPortNumber, tServerAddress, tUserName, tMessage;
    //label for the message instruction
    private JLabel label;
    //the port
    private int port;
    //the server and username;
    private String server, username;
    //the client
    private Client client;
    //message
    private String msg;
    

    //client constructor 
    myClientGUI(String server, int port, String username) {
        super("Chat Client");
        client = null;
        //the NorthPanel
        JPanel north = new JPanel(new GridLayout(1,1));
        //the port number
        JPanel sAndP = new JPanel(new GridLayout(1,6));
        //background colour of green
        sAndP.setBackground(Color.GREEN);
        sAndP.add(new JLabel("Port Number: "));
        tPortNumber  = new JTextField(" " + port);
        tPortNumber.setEditable(false);
        sAndP.add(tPortNumber);
        //the server address
        sAndP.add(new JLabel("Server address: "));
        tServerAddress = new JTextField(server);
        tServerAddress.setEditable(false);
        sAndP.add(tServerAddress);
        //the username
        sAndP.add(new JLabel("Username: "));
        tUserName = new JTextField(" " + username);
        tUserName.setEditable(false);
        sAndP.add(tUserName);
        //position at the top of the page
        north.add(sAndP);
        add(north, BorderLayout.NORTH);

        // the chat room
        JPanel center = new JPanel(new GridLayout(1,1));
        chat = new JTextArea(80,80);
        chat.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        chat.setEditable(false);
        //add a title to the chat room
        append("Chat Room. \n");
        center.add(new JScrollPane(chat));
        add(center, BorderLayout.CENTER);
        
        //the south panel
        JPanel south = new JPanel(new GridLayout(3,1));
        //background colour of green
        south.setBackground(Color.GREEN);
        //instruction message
        label = new JLabel("Please enter your message below: ");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Comic Sans MS", Font.PLAIN, 14));
        south.add(label);
        //the text input field for the client's message
        tMessage = new JTextField(" ");
        tMessage.setHorizontalAlignment(SwingConstants.CENTER);
        tMessage.addActionListener(this);
        tMessage.setEnabled(false);
        south.add(tMessage);
        
        //the login, logout and who is in buttons
        JPanel LAndW = new JPanel(new GridLayout(1,3));
        //background colour green
        LAndW.setBackground(Color.GREEN);
        login = new JButton("Log in");
        login.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        login.addActionListener(this);
        logout = new JButton("Log out");
        logout.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        logout.addActionListener(this);
        logout.setEnabled(false);
        whoIsIn = new JButton("Who is in?");
        whoIsIn.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);
        readFile = new JButton("Load previous chats");
        readFile.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
        readFile.addActionListener(this);
        readFile.setEnabled(false);
        LAndW.add(login);
        LAndW.add(logout);
        LAndW.add(whoIsIn);
        LAndW.add(readFile);
        south.add(LAndW);
        add(south, BorderLayout.SOUTH);
        
        //informs when the user clicks the close button on the frame
        addWindowListener(this);
        setSize(800, 800);
        setVisible(true);
        tMessage.requestFocus();
    }       
    
    /**
     * adds a message to the chat room, after the last one
     * @param str a string
     */
    void append(String str) {
        chat.append(str);
        chat.setCaretPosition(chat.getText().length() - 1);
    }
    
    //resets the widgets when connection fails
    void connectionFailed(){
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        readFile.setEnabled(false);
        tMessage.setText(" ");
        tMessage.setEnabled(false);
        tMessage.removeActionListener(this);
        tPortNumber.setEditable(false);
        tServerAddress.setEditable(false);
        tUserName.setEditable(false);
        connected = false;
    }

    /**
     * responds to actionListeners
     * @param e the widget clicked upon
     */
    public void actionPerformed(ActionEvent e) {
       Object c = e.getSource();
        //if 'log out' button
        if(c == logout){
            msg = "LOGOUT";
            client.sendMessage(msg);
            login.setEnabled(true);
            logout.setEnabled(false);
            whoIsIn.setEnabled(false);
            readFile.setEnabled(false);
            tMessage.setEnabled(false);
            return;
       }
        //if 'who is in' button
        if(c == whoIsIn){
            msg = "WHOISIN";
            client.sendMessage(msg);
            return;
       }
        //if 'load previous chats' button
        if(c == readFile){
            client.readChat();
            return;
       }
        //if user has typed a message and pressed return
        if(connected){
            msg = tMessage.getText();
            client.sendMessage(msg);
            tMessage.setText(" ");
            return;
       }
        //if 'log in' button
       if(c == login){
       // OK start the server  
       int port;
       try {
            port = Integer.parseInt(tPortNumber.getText().trim());
        } catch(Exception er) {
            append("Invalid port number");
            return;
       }
        String server;
       try{
            server = tServerAddress.getText().trim();
        }catch(Exception er){
            append("Invalid server address");
            return;
       }
        String username;
       try{
            username = tUserName.getText().trim();
        }catch(Exception er){
            append("Invalid user name");
            return;
        }
        // create a new client GUI
        client = new Client(server, port, username, this);
       if(!client.run())
        return;
        tMessage.setText("");
        //show the client they are connected with a welcome message
        append("Welcome " + username + "\n");
        connected = true;
        
        
        //disable login button
        login.setEnabled(false);
        //enable logout button
        logout.setEnabled(true);
        //enable whoisin button
        whoIsIn.setEnabled(true);
        //enable loadpreviouschats button
        readFile.setEnabled(true);
        //enable text field for messages
        tMessage.setEnabled(true);
        //disable editing of serveraddress, port number and username
        tServerAddress.setEditable(false);
        tPortNumber.setEditable(false);
        tUserName.setEditable(false);
       }
    }

    /**
     * If the user clicks the X button to close the window
     * client needs to close the connection with the server to free the port
     * @param e the X in the corner of the frame
     */

    public void windowClosing(WindowEvent e) {
        // if client exits
        if (client != null) {
            try {
                client.disconnect(); // ask the server to close the conection
            } catch(Exception eClose) {
                //handle exception
            }
            client = null;
        }
        // dispose of the frame
        dispose();
        System.exit(0);
    }
    
    // Ignore the other WindowListener methods
    public void windowClosed(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
}
