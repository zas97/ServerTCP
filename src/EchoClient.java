import java.io.*;
import java.net.*;


public class EchoClient {

    private String username;
    private Socket server;
    private PrintStream socOut;
    private BufferedReader socIn ;

    /**
     * constructor client
     * @param username
     * @param server socket for communication with the server
     */
    EchoClient(String username, Socket server){
        this.server = server;
        this.username = username;
    }


    /**
     * initializes the thread ReadServer
     * checks constantly if the user has written something
     * and it sends the messsage to the server
     * @throws IOException
     */
    public void startClient() throws IOException{
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
        ReadServer rs = new ReadServer();
        rs.start();
        String line;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            line=stdIn.readLine();
            SendMessage(line);
            if (line.equals("QUIT")) break;
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
            while (true) {
                try {
                    String line = socIn.readLine();
                    System.out.println(line);
                } catch (IOException e) {
                    break;

                }
            }
        }
    }



    public static void main(String[] args) throws IOException {

        Socket echoSocket = null;
        if (args.length != 2) {
          System.out.println("Usage: java EchoClient <EchoServer host> <EchoServer port>");
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

        EchoClient c = new EchoClient("Joan",echoSocket);
        c.startClient();
                             


    }


}


