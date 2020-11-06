
/**
 * Driver to start the Client GUI
 * Contains the main method
 * 
 * @author Jo Binns 
 * @version 6th January 2020
 */
public class ClientDriver
{
   // entry point to start the client GUI
   public static void main(String[] arg) {
        // start client port - 1500, server address - localhost, username - Jo
        new myClientGUI("localhost",1500, "Jo");
   }
}
