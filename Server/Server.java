/**
 * A server application that runs as a GUI, with multiple clients
 *
 * @author Dr Richard Jiang; minor edits by Dr Mark C. Sinclair
 * further edits by Jo Binns
 * @version 6th January 2020
 */

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    // a unique ID for each connection
    private static int uniqueId;
    // an ArrayList to keep the list of Clients
    private ArrayList<ClientThread> al;
    // to display date and time
    private SimpleDateFormat sdf;
    // the port number to listen for connection
    private int port;
    // the boolean that will be turned off to stop the server
    private boolean keepGoing;
    // the server GUI
    private myGUI mg;
    // the chat list log
    private ArrayList<String> chatList;
    
    /**
     * the server constructor
     * @param port, mg the port to listen for connection, the server GUI
     */
    public Server(int port, myGUI mg){
        //the port 
        this.port = port;
        //the server GUI
        this.mg   = mg;
        // to display dd-mm-yyy hh:mm:ss
        sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        // ArrayList for the Client list
        al = new ArrayList<ClientThread>();
        //chat list
        chatList = new ArrayList<String>();
    }
    
    //starts the Server
    public void start() {
        keepGoing = true;
        // create socket server and wait for connection requests
       try {
            // the socket used by the server
           ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while (keepGoing) {
            //message: the server is waiting on the port number
               displayEvent("Server waiting for Clients on port " + port + ".");
            //accept connection to client socket
               Socket clientsocket = serverSocket.accept(); 
            
            // in the event of the server stopping
            if (!keepGoing)
                break;
            
            //make a thread of the client connection
            ClientThread t = new ClientThread(clientsocket);
            //save it to the ArrayList
            al.add(t); 
            //start the client thread
            t.start();
            }
            
            //Server is asked to disconnect a client
            try {
                 serverSocket.close();
                 /**
                  * server scans the arraylist for the client that has asked to disconnect
                  * then closes the streams and socket for that client
                  */
                for (int i = 0; i < al.size(); ++i) {
                    ClientThread tc = al.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch(IOException ioE) {
                       //exception handling
                    }
                  }
            } catch(Exception e) {
                displayEvent("Exception closing the server and client: " + e);
            }
       }
            // something went wrong setting up the server socket
            catch (IOException e) {
                String msg = sdf.format(new Date()) + " Exception on new ServerSocket: "
                + e + "\n";
                //display in events section of GUI
                displayEvent(msg);
       }
    }

    //to stop the server
    protected void stop() {
        keepGoing = false;
        // connect to myself as Client to exit
        try {
            new Socket("localhost", port);
        } catch(Exception e) {
            // exception handling
        }
    }

    /**
     * display an event(not a message)to the GUI
     * @param msg the message passed as a string
     */
    private void displayEvent(String msg) {
        //format the event with date and time before it
        String e = sdf.format(new Date()) + " : " + msg;
        //add the event to the chatList array
        chatList.add(e);
        //display newly formatted event in GUI
        mg.appendEvent(e + "\n");
    }
    
    /**
     * display a message to the GUI
     * @param msg the message passed as a string
     */
    private void displayMsg(String msg) {
        //add message to chatList array
        chatList.add(msg);
        //display message in GUI
        mg.appendRoom(msg);    
    }
    
    //save the chatList array
    private void chatSave() {
        try{
            //create a text file to write the chatList to
            PrintStream print =
                new PrintStream(new FileOutputStream("log.txt", true));
            //write each line of the array to the file    
            for (String str:chatList)
                print.println(str);
            //once each record has been written to the file, close it.
            print.close();
        } catch (IOException e) {
            // exception handling
        }
    }
    
    /**
     * method to broadcast a message to all clients
     * @param message the message passed in as a string
     */
    private synchronized void broadcast(String message){
         //add dd-MM-yyyy HH:mm:ss to the message, each starting on a new line
         String messageLf = sdf.format(new Date()) + " " + message + "\n";
         // display message in GUI
         displayMsg(messageLf);
         //loop in reverse order in case a client disconnects & needs to be removed
         for (int i = al.size(); --i >= 0;){
         ClientThread ct = al.get(i);
            // try to write to the client, if fail, remove client from the list
            if (!ct.writeMsg(messageLf)) {
                al.remove(i);
                displayEvent("Disconnected Client " + ct.username +
                " removed from list.");
          }
        }
    }

    /**
     * for a client who logs off using the LOGOUT message
     * @param id the unique client id
     */
    synchronized void remove(int id) {
        // server scans array list until id of the client that has logged off is found
        for (int i = 0; i < al.size(); ++i) {
            ClientThread ct = al.get(i);
            // found it, now remove the client with that id
            if (ct.id == id) {
                al.remove(i);
                return;
            }
        }
     }
     
    /**
     * main method
     */
    public static void main(String[] args) throws Exception {
        // start server on port 1500 unless another portNumber is specified 
        int portNumber = 1500;
        switch(args.length) {
            case 1:
            try {
                portNumber = Integer.parseInt(args[0]);
            } catch(Exception e) {
                System.out.println("Invalid port number.");
                System.out.println("Usage is: > java Server [portNumber]");
                return;
            }
            case 0:
            break;
            default:
            System.out.println("Usage is: > java Server [portNumber]");
            return;           
        }
        // create a server object and start it
        Server server = new Server(portNumber, null);
        server.start();
    }
    
    /**
     * one instance of this thread will run for each client
     */
    class ClientThread extends Thread {
        // the socket where to listen/talk
        Socket socket;
        //the streams receiving and sending data
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // the client's unique id (makes disconnecting easier)
        int id;
        // the username of the Client
        String username;
        // the only type of message the server will receive
        String msg;
        // the date the client connects
        String date;
        /**
         * constructor for the client thread
         */
        ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            //the socket used to connect
            this.socket = socket;
            //creating both data streams
            displayEvent("Thread trying to create Object Input/Output Streams");
            try {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                //create input
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the client's username
                username = (String) sInput.readObject();
                //display event saying client has just connected
                displayEvent(username + " just connected.");
            } catch (IOException e) {
                displayEvent("Exception creating new Input/output Streams: " + e);
                return;
            }
            //have to catch ClassNotFoundException,but server reads a String,
            //so it should work
            catch (ClassNotFoundException e) {
                // handle exception
            }
            //string representation created of a new date object 
            date = new Date().toString() + "\n";
        }
        
        /**
         * run method for class ClientThread
         */
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while (keepGoing) {
                // read a String (which is an object)
                try {
                    msg = (String) sInput.readObject();
                } catch (IOException e) {
                    //exception message displayed and then loop broken
                    displayEvent(username + " Exception reading Streams: " + e);
                    break;              
                } catch(ClassNotFoundException e2) {
                    //exception handling, loop broken
                    break;
                }

                // Switch on the type of message received
                if (msg.equals("LOGOUT")) {
                    //logout message displayed in events section of server
                    displayEvent(username + " disconnected with a LOGOUT message. \n \n");
                    //goodbye message displayed client side
                    writeMsg("Goodbye " +username + "! \n \n");
                    //client disconnected
                    keepGoing = false;
                } else if (msg.equals("WHOISIN")) { 
                    //message displayed client side with date
                    writeMsg("List of the users connected at " + sdf.format(new Date()) +
                        "\n");
                   
                    //arraylist scanned for clients connected, 
                    //message displayed client side with clients' names & the date connected
                    for (int i = 0; i < al.size(); ++i) {
                        ClientThread ct = al.get(i);
                        writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
                    }
                } else {
                    //all other messages from client displayed client side
                    broadcast(username + " : " + msg);
                }
            }
            //remove this client from arrayList
            remove(id);
            close();
        }
        
        /**
         * try to close everything
         */
        private void close() {
            // try to close the connection
            try {
                if (sOutput != null)
                    sOutput.close();
            } catch(Exception e) {
                //handle exception
            }
            try {
                if (sInput != null)
                    sInput.close();
            } catch(Exception e) {
                //handle exception
            }
            try {
                if (socket != null)
                    socket.close();
            } catch (Exception e) {
                //handle exception
            }
            //save the chatlist array
            chatSave();
        }
        
        /**
         * pass a message to the Client output stream
         * @param msg a String message
         */
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the server GUI
            catch(IOException e) {
                displayEvent("Error sending message to " + username);
                displayEvent(e.toString());
            }
            return true;
        }
    }
}

