/***
 * ClientThread
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

	ClientThread(Socket s,EchoServerMultiThreaded server, int id) {
		this.clientSocket = s;
		this.server = server;
		this.id=id;
		this.username = "Anonymous"+String.valueOf(id);

		try {
			socOut = new PrintStream(clientSocket.getOutputStream());
			socIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch (Exception e) {
			System.err.println("Error in Server:" + e);
		}

	}

	public String getUsername(){
		return username;
	}

	public int getIdent(){
		return id;
	}

	//ask de client for the username

 	/**
  	* receives a request from client then sends an echo to the client
  	*  the client socket
  	**/
	public void run() {
    	  try {
    		while (true) {
    		  String line = socIn.readLine();
    		  if(line.equals("QUIT")){
				  server.remove(id);
			  }
			  else if(line.startsWith("SET USERNAME ")){
    		  	String aux[]=line.split(" ");
    		  	server.broadcast(username +" changed name to "+aux[2],"Server");
    		  	username = aux[2];
			  }
			  else if(line.charAt(0)=='@'){
			  	String arr[] = line.split(" ",2);
			  	String reciver = arr[0].substring(1);
			  	server.privateMessage(arr[1],username,reciver);
			  }
			  else {
				  server.broadcast(line, username);
			  }
    		}
    	} catch (Exception e) {
        	System.err.println("Error in Server:" + e);
        }
	}

	public void close(){
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


	}

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

  
