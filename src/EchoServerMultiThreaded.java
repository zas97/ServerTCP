/***
 * EchoServer
 * Example of a TCP server
 * Date: 10/01/04
 * Authors:
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class EchoServerMultiThreaded  {
  

 	private ArrayList<ClientThread> clients;
 	private int port;
 	private int nextId;
 	private String history;
 	private String historyFile;

    /**
     * Constructor of the server
     * initializes the list of threads for the clients
     * reads the persistent history allocated in historyfile
     * @param port
     */
    public EchoServerMultiThreaded(int port){
        clients = new ArrayList<ClientThread>();
        this.port=port;
        nextId = 1;
        historyFile="history.txt";
        try {
            history = new Scanner(new File(historyFile)).useDelimiter("\\A").next();
        }
        catch(IOException e1){
            System.out.println("error reading history");
        }

    }

    /**
     * starts listening for clients trying to connect
     * when a client connects it creates a thread for the client
     * and it sends the history of the chat to the client
     */
    public void start(){
        ServerSocket listenSocket;
        try {
            listenSocket = new ServerSocket(port); //port
            System.out.println("Server ready...");
            while (true) {
                Socket clientSocket = listenSocket.accept();
                System.out.println("Connexion from:" + clientSocket.getInetAddress());
                ClientThread ct = new ClientThread(clientSocket,this,nextId);
                nextId++;
                ct.start();
                if(history!=null) {
                    ct.writeMsg(history);
                }
                broadcast(ct.getUsername()+" connected to the server","Server");
                clients.add(ct);
            }
        } catch (Exception e) {
            System.err.println("Error in EchoServer:" + e);
        }
    }

    /**Sends a message to every user
     * this message is called by the ClientThread
     * @param msg message to send
     * @param sender username of the person sending the message
     */
    public synchronized void broadcast(String msg,String sender) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(new Date());
        msg = time + " "+sender+": " + msg;
        System.out.println(msg);

        history+=msg;
        history+="\n";
        try {
            PrintWriter out = new PrintWriter(new FileWriter(historyFile,true));
            out.println(msg);
            out.close();
        }
        catch(IOException e1) {
            System.out.println("error writing in persistent history");
        }

        for(int i=0;i<clients.size();i++){
            ClientThread c = clients.get(i);
            if(!c.writeMsg(msg)){
                clients.remove(i);
                broadcast(c.getUsername() +" disconected","Server");
            }
        }
    }

    /**
     * sends a message to one user
     * @param msg message
     * @param sender username of the person sending
     * @param reciver username of the person who has to recive it
     */
    public void privateMessage(String msg,String sender,String reciver){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(new Date());
        msg = time + " "+sender+": " + msg;
        System.out.println(msg);

        for(int i=0;i<clients.size();i++){
            ClientThread c = clients.get(i);
            if(c.getUsername().equals(reciver)) {
                if (!c.writeMsg(msg)) {
                    clients.remove(i);
                    broadcast(c.getUsername() + " disconected", "Server");
                }
            }
        }
    }

    /**
     * removes client
     * @param id of the client
     */
    public synchronized void remove(int id){
        for(int i=0;i<clients.size();i++){
            ClientThread c = clients.get(i);
            if(c.getIdentification()==id){
                clients.remove(i);
                broadcast(c.getUsername() +" disconected","Server");
            }
        }
    }



    public static void main(String args[]){
        int port = Integer.parseInt(args[0]);
        EchoServerMultiThreaded server = new EchoServerMultiThreaded(port);
        server.start();
    }
}

  
