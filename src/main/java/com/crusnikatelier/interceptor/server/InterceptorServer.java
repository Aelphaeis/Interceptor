package com.crusnikatelier.interceptor.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterceptorServer implements Runnable, Closeable {
	
	private static final Logger logger = LoggerFactory.getLogger(InterceptorServer.class);
	ServerSocket serverSocket;
	List<Thread> inHandlers;
	TextHandler textHandle;
	
	public InterceptorServer(){
		inHandlers = new ArrayList<Thread>();
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
			
			logger.trace("Waiting to accept client socket");
			while (!Thread.interrupted()){
				try{
					Socket client  = serverSocket.accept();
					
					logger.trace("Retrieving input stream");
					InputStream inStream = client.getInputStream();
					
					logger.trace("Setting up input handler");
					InHandler handler = new InHandler(inStream);
					handler.setHandler(new defaultTextHandler());
					Thread handleThread = new Thread(handler);
					inHandlers.add(handleThread);
					handleThread.start();
				}
				catch(SocketTimeoutException e){
					//No clients received. Check thread interrupt
					continue;
				}
			}
			logger.trace("No longer waiting for client sockets");
		}
		catch (IOException e) {
			logger.error("Unable to handle client inputStream", e);
		}
		finally{
			try {
				close();
			} 
			catch (IOException e) {
				String msg = "Unable to close socket";
				logger.error(msg, e);
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public void close() throws IOException {
		for(Thread t : inHandlers){
			t.interrupt();
		}
		
		if(serverSocket != null && !serverSocket.isClosed()){
			serverSocket.close();
		}
	}
	
	
	public int getPort(){
		return serverSocket.getLocalPort();
	}
	
	public TextHandler getHandler() {
		return textHandle;
	}

	public void setHandler(TextHandler textHandle) {
		this.textHandle = textHandle;
	}
	
	private class defaultTextHandler implements TextHandler{
		@Override
		public void handle(String msg) {
			TextHandler serverTextHandler = getHandler();
			if(serverTextHandler != null){
				serverTextHandler.handle(msg);
			}
		}
	}


}
