package com.crusnikatelier.interceptor.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.crusnikatelier.interceptor.core.InHandler;

public class InterceptorServer implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(InterceptorServer.class);
	ServerSocket serverSocket;
	List<Thread> inHandlers;
	
	public InterceptorServer(){
		try {
			serverSocket = new ServerSocket(0);
		} 
		catch (IOException e) {
			String err = "Unable to create server socket";
			throw new RuntimeException(err, e);
		}
		inHandlers = new ArrayList<Thread>();
	}

	@Override
	public void run() {
		try {
			serverSocket.setSoTimeout(500);
			while (!Thread.interrupted()){
				try{
					logger.trace("Waiting to accept client socket");
					Socket client  = serverSocket.accept();
					
					logger.trace("Retrieving input stream");
					InputStream inStream = client.getInputStream();
					
					logger.trace("Setting up input hanlder");
					InHandler handler = new InHandler(inStream);
					Thread  handleThread = new Thread(handler);
					inHandlers.add(handleThread);
					handleThread.start();
				}
				catch(SocketTimeoutException e){
					//No clients received. Check thread interrupt
					continue;
				}
			}
		}
		catch (IOException e) {
			logger.error("Unable to handle client inputStream", e);
		}
		finally{
			closeSocket();
		}
	}
	
	public int getPort(){
		return serverSocket.getLocalPort();
	}
	
	protected void closeSocket(){
		if(serverSocket != null && !serverSocket.isClosed()){
			try {
				serverSocket.close();
			}
			catch (IOException e) {
				logger.error("Unable to close server socket", e);
			}
		}
	}
}
