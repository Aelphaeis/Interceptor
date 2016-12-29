package com.crusnikatelier.interceptor.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterceptorServer implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(InterceptorServer.class);
	ServerSocket serverSocket;
	
	public InterceptorServer(){
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
			logger.trace("Waiting to accept client socket");
			Socket client  = serverSocket.accept();
		
			logger.trace("Retrieving input stream");
			InputStream inStream = client.getInputStream();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public int getPort(){
		return serverSocket.getLocalPort();
	}
}
