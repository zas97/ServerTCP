package Client;

import java.io.*;
import java.net.*;

/**
 * This class allows the communication to the server via TCP/IP using sockets. It allows the user to send messages
 * to the server via the terminal. It has a thread that constantly listens to the server for messages and prints
 * them in the terminal. It also contains a graphical interface that allows to better control the chat.
 * We recommend using the chat via the graphical interface for commodity and facility to use.
 */
public class EchoClient {


    private Socket server;
    private PrintStream socOut;
    private BufferedReader socIn;
    private ClientInterface gui;
    private String host;
    private boolean running;

    /**
     * Constructor of EchoClient
     * @param server socket that connects to the server
     * @param host String containing the ip address to the server, this parameter is only there to show it to the user
     */
    EchoClient(Socket server, String host){
        this.server = server;
        this.host=host;

    }


    /**
     * Initializes the thread that listens to the server.
     * Initializes the GUI.
     * Checks constantly if the user has written something in the terminal,
     * if the users writes something, the message is send directly to the server.
     * Note that messages can also be sent by the GUI.
     * @throws IOException in case of error writing messages to the server
     */
    public void startClient() throws IOException{
        running = true;
        Socket echoSocket = server;
        try {
            socIn = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
            socOut = new PrintStream(echoSocket.getOutputStream());
        }

        catch (UnknownHostException e) {
            System.err.println("Don't know about host:" );
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                    + "the connection to:");
            System.exit(1);
        }
        gui = new ClientInterface(this,host,socIn.readLine());

        ReadServer rs = new ReadServer();
        rs.start();
        String line;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        //it runs while the user doesn't write QUIT
        while (running) {
            try {
                line = stdIn.readLine();
                SendMessage(line);
            } catch(IOException e){
                break;
            }
        }
        socOut.close();
        socIn.close();
        stdIn.close();
        echoSocket.close();
    }

    /**
     * Sends a message to the server, if the message its "QUIT",
     * it indicates the class that it has to stop running
     * @param msg message to be sent
     */
    public void SendMessage(String msg){
        socOut.println(msg);
        if(msg.equals("QUIT")){
            running = false;
        }
    }

    /**
     * Thread that listens the server and puts this messages
     * in in the terminal and in the graphical interface
     */
    public class ReadServer extends Thread {
        /**
         * checks constantly if the server has sent something to this socket
         * in case of reciving a message it prints it in the terminal and in the GUI
         */
        public void run() {
            running = true;
            while (running) {

                try {
                    String line = socIn.readLine();
                    System.out.println(line);
                    String parts[] = line.split(" ");
                    //if the server sends the list of users we don't really need to post it
                    //we just need to update the list of users showed in the GUI
                    if(parts[0].equals("USERS:")){
                        gui.updateUsers(parts);
                    }
                    else {
                        gui.printMessage(line);
                    }

                } catch (IOException e) {
                    System.out.println(e);
                    break;
                }
            }
        }
    }


    /**
     *
     * @param args contains the host and port at which the client will connect
     * @throws IOException error creating the socket
     */
    public static void main(String[] args) throws IOException {

        Socket echoSocket = null;
        if (args.length != 2) {
          System.out.println("Usage: java Client.EchoClient <EchoServer host> <EchoServer port>");
          System.exit(1);
        }

        try {
      	    // creation socket ==> connexion
      	    echoSocket = new Socket(args[0],new Integer(args[1]).intValue());

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + args[0]);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for "
                               + "the connection to:"+ args[0]);
            System.exit(1);
        }

        EchoClient c = new EchoClient(echoSocket,args[0]);
        c.startClient();
                             


    }


}


