package Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Thread that constantly listens to one client and calls
 * the methods of server according to the messages sent by the user
 */
public class ClientThread extends Thread {

    private Socket clientSocket;
    private String username;
    private EchoServerMultiThreaded server;
    private PrintStream socOut;
    private BufferedReader socIn;
    private int id;
    private boolean working;

    /**
     * Create a thread for communication with the client
     * each thread is identified by an unique id
     *
     * @param s      socket of the client
     * @param server object server
     * @param id     id that identifies uniquely this thread
     */
    ClientThread(Socket s, EchoServerMultiThreaded server, int id) {
        this.clientSocket = s;
        this.server = server;
        this.id = id;
        this.username = "Anonymous" + String.valueOf(id);
        working = true;

        try {
            socOut = new PrintStream(clientSocket.getOutputStream());
            socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (Exception e) {
            System.err.println("Error in Server:" + e);
        }
        //sends the username to the client
        writeMsg(username);

    }

    /**
     * @return name of the user in this thread
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return unique id of the thread
     */
    public int getIdentification() {
        return id;
    }

    /**
     * It constantly listens to the client messages, depending on the client message it can
     * do 3 things
     * QUIT: Stops the connection with the client
     * SET USERNAME {@code <username>}: sets the name of the user to 'username'
     * USERS?: Sends a list of the users to the client
     * {@code @<username>}:Changes the username of the user to {@code @<username>}
     */
    public void run() {
        try {
            while (working) {
                String line = socIn.readLine();
                if (line.equals("QUIT")) {
                    server.remove(id);
                    working = false;
                } else if (line.startsWith("SET USERNAME ")) {
                    String aux[] = line.split(" ");
                    server.broadcast(username + " changed name to " + aux[2], "Server");
                    username = aux[2];
                } else if (line.equals("USERS?")) {
                    String listUsers = server.listUsernames();
                    writeMsg("USERS: " + listUsers);
                } else if (line.charAt(0) == '@') {
                    String arr[] = line.split(" ", 2);
                    String reciver = arr[0].substring(1);
                    server.privateMessage(line, username, reciver);
                } else {
                    server.broadcast(line, username);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in Server:" + e);
        }
        close();
    }

    /**
     * closes the communication with the client
     */
    public void close() {
        working = false;
        try {
            if (socOut != null) {
                socOut.close();
            }
        } catch (Exception e) {
        }

        try {
            if (socIn != null) {
                socIn.close();
            }
        } catch (Exception e) {
        }

        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
        }
        server.remove(id);


    }

    /**
     * sends a message to the client using the socket
     *
     * @param msg message
     * @return true if message sent succesfully
     */
    public boolean writeMsg(String msg) {
        if (!clientSocket.isConnected()) {
            return false;
        }
        try {
            socOut.println(msg);

        } catch (Exception e) {
            System.err.println("Error in Server:" + e);
            return false;
        }
        return true;
    }

}

  
