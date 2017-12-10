package Client;

import java.io.*;
import java.net.*;


public class EchoClient {


    private Socket server;
    private PrintStream socOut;
    private BufferedReader socIn;
    private ClientInterface gui;
    private String host;
    private boolean running;

    /**
     * constructor client
     * @param server socket for communication with the server
     */
    EchoClient(Socket server, String host){
        this.server = server;
        this.host=host;

    }


    /**
     * initializes the thread ReadServer
     * checks constantly if the user has written something
     * and it sends the messsage to the server
     * @throws IOException
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
     * @param msg message
     */
    public void SendMessage(String msg){
        socOut.println(msg);
        if(msg.equals("QUIT")){
            running = false;
        }
    }

    /**
     * Thread that listens the server
     */
    public class ReadServer extends Thread {
        /**
         * checks constantly if the server has sent something to this socket
         * in case of reciving a message it prints it in the terminal and
         * in the GUI
         */
        public void run() {
            running = true;
            while (running) {

                try {
                    String line = socIn.readLine();
                    System.out.println(line);
                    String parts[] = line.split(" ");
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


