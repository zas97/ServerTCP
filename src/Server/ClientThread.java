package Server; /***
 * Server.ClientThread
 * Example of a TCP server
 * Date: 14/12/08
 * Authors:
 */

import java.io.*;
import java.net.*;

public class ClientThread
	extends Thread {
	
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
	 * @param s socket
	 * @param server
	 * @param id
	 */
	ClientThread(Socket s,EchoServerMultiThreaded server, int id) {
		this.clientSocket = s;
		this.server = server;
		this.id=id;
		this.username = "Anonymous"+String.valueOf(id);
		working = true;

		try {
			socOut = new PrintStream(clientSocket.getOutputStream());
			socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch (Exception e) {
			System.err.println("Error in Server:" + e);
		}
		//sends the username to the client
		writeMsg(username);

	}

	public String getUsername(){
		return username;
	}

	public int getIdentification(){
		return id;
	}

	/**
	 * it sends the username to the client and
	 * checks constantly if the user is sending messages
	 * in case of a message it checks if its a command or a plain message
	 * and acts accordingly
	 */
	public void run() {
    	  try {
    		while (working) {
    		  String line = socIn.readLine();
    		  if(line.equals("QUIT")){
				  server.remove(id);
				  working=false;
			  }
			  else if(line.startsWith("SET USERNAME ")){
    		  	String aux[]=line.split(" ");
    		  	server.broadcast(username +" changed name to "+aux[2],"Server");
    		  	username = aux[2];
			  }
			  else if(line.equals("USERS?")){
			  	String listUsers=server.listUsernames();
			  	writeMsg("USERS: "+listUsers);
			  }
			  else if(line.charAt(0)=='@'){
			  	String arr[] = line.split(" ",2);
			  	String reciver = arr[0].substring(1);
			  	server.privateMessage(line,username,reciver);
			  }
			  else if(line.equals("QUIT")){
			      close();
              }
			  else {
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
	public void close(){
		working=false;
		try{
			if(socOut != null){
				socOut.close();
			}
		}
		catch(Exception e) {}

		try{
			if(socIn != null){
				socIn.close();
			}
		}
		catch(Exception e) {}

		try{
			if(clientSocket != null){
				clientSocket.close();
			}
		}
		catch(Exception e) {}
		server.remove(id);


	}

	/**
	 * sends a message to the client
	 * @param msg message
	 * @return
	 */
	public boolean writeMsg(String msg){
		if(!clientSocket.isConnected()){
			return false;
		}
		try {
			socOut.println(msg);

		}
		catch (Exception e) {
			System.err.println("Error in Server:" + e);
			return false;
		}
		return true;
	}
  
}

  