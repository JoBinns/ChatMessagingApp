/**
 * A client application that connects to a server and runs as a GUI
 *
 * @author Dr Richard Jiang; minor edits by Dr Mark C. Sinclair
 * further edits by Jo Binns
 * 
 * @version 6th January 2020
 */

import java.util.*;
import java.io.*;
import java.net.*;

public class Client {
    //the server address and client username
    private String server, username;
    //the port number used to connect to the server
    private int port;
    //the socket used to connect to the server
    private Socket             clientsocket;
    //the input and output data streams
    private ObjectInputStream  sInput;
    private ObjectOutputStream sOutput;
    //the chatList arraylist
    private ArrayList<String> chatList;
    //the client GUI
    private myClientGUI cg;
    
    /**
     * the client constructor
     * @param server, port, username, cg the server address, port number
     * client username, client GUI
     */
    public Client(String server, int port, String username, myClientGUI cg) {
        //the server
        this.server   = server;
        //the port
        this.port     = port;
        //the username
        this.username = username;
        //the clientGUI
        this.cg       = cg;
        //the chatlist array
        chatList = new ArrayList<String>();
    }
    
    /**
     * display a message to the client GUI
     * @param msg a String message
     */
    private void display(String msg) {
        cg.append(msg);
    }

    // saves the chat list to a log file with the username's 
    private void chatSave() {
        try{
            PrintStream print = 
                new PrintStream(new FileOutputStream(username + ".txt", true));
            for(String str:chatList)
                print.println(str);
                print.close();
             } catch (IOException e) {
                //as logged out unable to do anything on the client side.
                }
    }
    
    //loads the text file containing previous chats to the client GUI
    void readChat() {
        try (BufferedReader br = new BufferedReader(new FileReader(username + ".txt"))){
        String line = null;
            while((line = br.readLine()) !=null){
            cg.append(line + "\n");
           }
        }catch (IOException er) {
        } 
    }
        
    //starts the Client
    public boolean run() {
        //Try to connect to server
        try {
            clientsocket = new Socket(server, port);
        } 
        catch(Exception ec) {
            display("Error connectiong to server:" + ec);
            return false;
        }
        String msg = "Connection accepted " + clientsocket.getInetAddress() + ":" +
            clientsocket.getPort() + "\n";
            display(msg);

        // Create both data streams
        try {
            sInput  = new ObjectInputStream(clientsocket.getInputStream());
            sOutput = new ObjectOutputStream(clientsocket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // create the thread to listen from the server 
        new RunClientThread().start();

        // send client's username to the server
        try {
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        // success
        return true;
    }
    
    //starts the client thread
    class RunClientThread extends Thread {
        public void run() {
            while(true) {
                try {
                    //read the message from the server
                    String msg = (String) sInput.readObject();
                    //add message to chatlist array
                    chatList.add(msg);
                    //display message on client GUI
                    display(msg);
                } catch(IOException e) {
                    display("Server has closed the connection: " + e + "\n");
                    break;
                }
                // can't happen with a String object but need the exception
                catch(ClassNotFoundException e2){}
            }
        }
    }

    /**
     * To send a message to the server
     * @param msg a String message
     */

    void sendMessage(String msg) {
        try {
            sOutput.writeObject(msg);
        } catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    /**
     * When something goes wrong,
     * close the Input/Output streams and the socket, 
     * which means the client is disconnected
     */
    void disconnect() {
        try { 
            if (sInput != null) 
                sInput.close();
        } catch(Exception e) {
            // do nothing
        } 
        try {
            if (sOutput != null) 
                sOutput.close();
        } catch(Exception e) {
            // do nothing
        } 
        try {
            if (clientsocket != null) 
                clientsocket.close();
        } catch(Exception e) {
            // do nothing
        } 
        if(cg!= null)
        //close the GUI
        cg.connectionFailed();
        chatSave();
    }
    
    //the main method
    public static void main(String[] args) {
        int portNumber       = 1500;
        String serverAddress = "localhost";
        String userName      = "Jo";
        myClientGUI  cg      = null;

        Client thisClient = new Client(serverAddress, portNumber, userName, cg);
        if (!thisClient.run())
            return;
    }
}
