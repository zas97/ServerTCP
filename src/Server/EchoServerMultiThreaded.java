package Server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Server for a chat system that allows sending messages to all users or to just one user.
 * It contains a history with all the previously written public messages and it sends this history
 * to users that are connecting. The history is persistent, that means that if the server is stopped this
 * history is kept in the memory and will reappear once we restart the server.
 * Users have a username and an id as unique identifier.
 */
public class EchoServerMultiThreaded {


    private ArrayList<ClientThread> clients;
    private int port;
    private int nextId;
    private String history;
    private String historyFile;

    /**
     * Constructor of the server
     * initializes the list of threads for the clients
     * reads the persistent history allocated in history.txt.
     *
     * @param port port for the connexion
     */
    public EchoServerMultiThreaded(int port) {
        clients = new ArrayList<ClientThread>();
        this.port = port;
        nextId = 1;
        historyFile = "files/history.txt";
        try {
            history = new String(Files.readAllBytes(Paths.get(historyFile)));
        } catch (IOException e1) {
            System.out.println("error reading history");
        }

    }

    /**
     * starts listening for clients trying to connect
     * when a client connects it creates a thread for the client
     * and it sends the history of the chat to that client
     */
    public void start() {
        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(port); //port
            System.out.println("Server ready...");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                ClientThread ct = new ClientThread(clientSocket, this, nextId);
                nextId++;
                ct.start();
                if (history != null) {
                    ct.writeMsg(history);
                }
                broadcast(ct.getUsername() + " connected to the server", "Server");
                clients.add(ct);
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

    /**
     * Sends a message to every user with the time that that message has been sent
     * and the username who has send it. It also saves this message in the history.
     * this message is called by the Server.ClientThread
     *
     * @param msg    message to send
     * @param sender username of the person sending the message
     */
    public synchronized void broadcast(String msg, String sender) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(new Date());
        msg = time + " " + sender + ": " + msg;
        System.out.println(msg);

        history += msg;
        history += "\n";
        try {
            PrintWriter out = new PrintWriter(new FileWriter(historyFile, true));
            out.println(msg);
            out.close();
        } catch (IOException e1) {
            System.out.println("error writing in persistent history");
        }

        for (int i = 0; i < clients.size(); i++) {
            ClientThread c = clients.get(i);
            if (!c.writeMsg(msg)) {
                clients.remove(i);
                broadcast(c.getUsername() + " disconected", "Server");
            }
        }
    }

    /**
     * Sends a message to users with the name specified, this message is not saved in the history.
     *
     * @param msg     message
     * @param sender  username of the person sending
     * @param reciver username of the person who has to receive it
     */
    public synchronized void privateMessage(String msg, String sender, String reciver) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(new Date());
        msg = time + " " + sender + ": " + msg;
        System.out.println(msg);

        for (int i = 0; i < clients.size(); i++) {
            ClientThread c = clients.get(i);
            if (c.getUsername().equals(reciver)) {
                if (!c.writeMsg(msg)) {
                    clients.remove(i);
                    broadcast(c.getUsername() + " disconected", "Server");
                }
            }
        }
    }

    /**
     * @return String with the names of the users separated by an space
     */
    public String listUsernames() {
        String list = "";
        int len = clients.size();
        for (int i = 0; i < len; i++) {
            list += clients.get(i).getUsername() + " ";
        }
        return list;


    }

    /**
     * removes client from the server
     *
     * @param id of the client that needs to be removed
     */
    public synchronized void remove(int id) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread c = clients.get(i);
            if (c.getIdentification() == id) {
                clients.remove(i);
                broadcast(c.getUsername() + " disconected", "Server");
            }
        }
    }


    /**
     * Starts the server in the specified port
     *
     * @param args port for the connexion
     */
    public static void main(String args[]) {
        if (args.length != 1) {
            System.out.println("Usage: java Server.EchoServerMultiThreaded <EchoServer port>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        EchoServerMultiThreaded server = new EchoServerMultiThreaded(port);
        server.start();
    }
}

  
