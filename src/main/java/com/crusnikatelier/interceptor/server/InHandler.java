package com.crusnikatelier.interceptor.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InHandler implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(InHandler.class);
	
	InputStream in;
	TextHandler handler;
	
	public InHandler(InputStream inStream) {
		in = inStream;
	}
	

	@Override
	public void run() {

		BufferedReader bufferedReader = null;
		InputStreamReader inStreamReader = null;
		try{
			logger.debug("Creating InputStreamReader");
			inStreamReader = new InputStreamReader(in);
			
			logger.debug("Creating BufferedReader");
			bufferedReader = new BufferedReader(inStreamReader);
			
			
			while(!Thread.interrupted()){
				try {
					while(bufferedReader.ready()){
						logger.debug("BufferedReader ready, reading message");
						String msg = bufferedReader.readLine();
						
						logger.debug("Handling Message");
						if(handler != null){
							handler.handle(msg);
						}
					}
				} 
				catch (IOException e) {
					logger.error("Unable to read content", e);
				}
			}
		}
		finally{
			logger.debug("Closing cleaning up inhandler");
			
			try{
				if(bufferedReader!= null){
					bufferedReader.close();
				}
				
				if(inStreamReader != null){
					inStreamReader.close();
				}	
			}
			catch(IOException e){
				logger.error("Unable to clean up inhandler");
			}
		}
	}


	public TextHandler getHandler() {
		return handler;
	}

	public void setHandler(TextHandler handler) {
		this.handler = handler;
	}
}
