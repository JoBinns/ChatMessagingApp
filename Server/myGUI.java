/**
 * The server as a GUI
 *
 * @author Dr Richard Jiang; minor edits by Dr Mark C. Sinclair
 * further edits by Jo Binns
 * @version 6th January 2020
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class myGUI extends JFrame implements ActionListener, WindowListener {
    //for serialization of the class
    private static final long serialVersionUID = 1L;
    //the stop/start button
    private JButton stopStart;
    //JTextArea for the chat room and the events
    private JTextArea chat, event;
    //the port number
    private JTextField tPortNumber;
    //the server
    private Server server;

    /**
     * server constructor
     * @param port the port to listen to for connection
     */ 
    myGUI(int port) {
        super("Chat Server");
        server = null;
        // in the NorthPanel - the PortNumber and Start/Stop button
        JPanel north = new JPanel();
        //red background colour for the JPanel
        north.setBackground(Color.RED);
        north.add(new JLabel("Port number: "));
        tPortNumber = new JTextField("  " + port);
        tPortNumber.setEditable(false);
        north.add(tPortNumber);
        // to stop or start the server, we start with "Start"
        stopStart = new JButton("Start");
        stopStart.addActionListener(this);
        north.add(stopStart);
        add(north, BorderLayout.NORTH);

        // the event and chat room
        JPanel center = new JPanel(new GridLayout(2,1));
        chat = new JTextArea(80,80);
        chat.setEditable(false);
        //title for where client messages are displayed
        appendRoom("Chat room.\n");
        center.add(new JScrollPane(chat));
        event = new JTextArea(80,80);
        event.setEditable(false);
        //title for where events are displayed 
        appendEvent("Events log.\n");
        center.add(new JScrollPane(event)); 
        add(center);

        // need to be informed when the user clicks the close button on the frame
        addWindowListener(this);
        setSize(600, 600);
        setVisible(true);
    }       

    /**
     * display a message in the chat room, add a new message after the last one
     * @param str a string 
     */
    void appendRoom(String str) {
        chat.append(str);
        chat.setCaretPosition(chat.getText().length() - 1);
    }
    
    /**
     * display an event in the event log area, add a new one after the last one
     * @param str a string
     */
    void appendEvent(String str) {
        event.append(str);
        event.setCaretPosition(chat.getText().length() - 1);
    }

    /**
     * start or stop server when button clicked
     * @param e the widget with an actionListener attached
     */
    public void actionPerformed(ActionEvent e) {
        // if running we have to stop
        if (server != null) {
            server.stop();
            server = null;
            tPortNumber.setEditable(true);
            stopStart.setText("Start");
            return;
        }
        // OK start the server  
        int port;
        try {
            /**
             * get the string from the tPortNumber JTextField, convert it to an integer
             * and pass to port
             */
            port = Integer.parseInt(tPortNumber.getText().trim());
        } catch(Exception er) {
            //display the exception in the events area
            appendEvent("Invalid port number");
            return;
        }
        // create a new Server
        server = new Server(port, this);
        // and start it as a thread
        new ServerRunning().start();
        stopStart.setText("Stop");
        tPortNumber.setEditable(false);
    }
    
    /**
     * If the X button is clicked to close the window,
     * need to close the connection with the server to free the port
     */

    public void windowClosing(WindowEvent e) {
        // if Server exits
        if (server != null) {
            try {
                //ask the server to close the connection
                server.stop(); 
            } catch(Exception eClose) {
                // exception handling
            }
            server = null;
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
    
    //a thread to run the Server
    class ServerRunning extends Thread {
        public void run() {
            //should execute until the server fails
            server.start();
            //if the server fails
            stopStart.setText("Start");
            tPortNumber.setEditable(true);
            appendEvent("Server stopped.");
            server = null;
        }
    }
}
