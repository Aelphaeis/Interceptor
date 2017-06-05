package com.crusnikatelier.interceptor.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterceptorServer implements Runnable, Closeable {
	
	private static final Logger logger = LoggerFactory.getLogger(InterceptorServer.class);
	
	public final Observable clientAccepted;
	ServerSocket serverSocket;
	Map<Integer, Socket> clients;
	
	public InterceptorServer(){
		//Setting up observer
		clientAccepted = new Observable(){
			@Override
			public void notifyObservers(Object arg) {
				setChanged();
				super.notifyObservers(arg);
			}
		};
		
		clientAccepted.addObserver(new clientAcceptedObserver());
		
		//Setting up client map
		clients = new HashMap<Integer, Socket>();
		try {
			serverSocket = new ServerSocket(0);
		} 
		catch (IOException e) {
			String err = "Unable to create server socket";
			throw new RuntimeException(err, e);
		}
		
	}

	@Override
	public void run() {
		try {
			serverSocket.setSoTimeout(500);
			logger.trace("Waiting to accept client socket on port " + serverSocket.getLocalPort());
			while (!Thread.interrupted()){
				try{
					//Accept client and grab local address.
					Socket client  = serverSocket.accept();
					
					String address = String.valueOf(client.getRemoteSocketAddress());
					
					//Log and pass to observers
					logger.trace("client accepted @ " + address);
					clientAccepted.notifyObservers(new EventArgs(client));
				}
				catch(SocketTimeoutException e){
					//No clients received. Check thread interrupt
					continue;
				}
			}
			logger.trace("No longer waiting for client sockets");
		}
		catch (IOException e) {
			logger.error("Unable to handle client socket", e);
		}
		finally{
			close();
			logger.info("Interceptor Server has successfully closed");
		}
	}
	
	@Override
	public void close() {
		if(serverSocket != null && !serverSocket.isClosed()){
			try{
				serverSocket.close();	
			}
			catch(IOException e){
				logger.error("Error closing socket", e);
			}
		}
	}
	
	public int getPort(){
		return serverSocket.getLocalPort();
	}

	public Set<Integer> getConnectedClientPorts(){
		return clients.keySet();
	}
	
	public Socket getSocket(int port){
		return clients.get(port);
	}

	static class EventArgs {
		private Socket socket;
		
		public EventArgs(Socket socket){
			this.socket = socket;
		}

		public Socket getSocket() {
			return socket;
		}
	}
	
	class clientAcceptedObserver implements  Observer{
		
		@Override
		public void update(Observable o, Object arg) {
			EventArgs args = (EventArgs)arg;
			Socket client = args.getSocket();
			clients.put(client.getLocalPort(), client);
			logger.trace("Added client socket to client map");
		}
	}
}
