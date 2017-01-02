package test.com.crusnikatelier.interceptor.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import com.crusnikatelier.interceptor.server.InterceptorServer;
import com.crusnikatelier.interceptor.server.TextHandler;

public class InterceptorServerTest {


	/**
	 * Basic test to ensure constructor works as intended
	 * @throws IOException 
	 */
	@Test
	public void ctorTest() throws IOException {
		InterceptorServer server = new InterceptorServer();
		int port = server.getPort();
		assertTrue(port < 65535);
		assertTrue(port > 0);
		server.close();
	}
	
	
	/**
	 * Test to ensure that server starts can stop gracefully if no clients are received
	 * @throws InterruptedException 
	 */
	@Test
	public void interruptTest() throws InterruptedException{
		InterceptorServer server = new InterceptorServer();
		Thread t = new Thread(server);

		//Start, wait and interrupt the thread
		t.start();
		assertEquals(State.RUNNABLE, t.getState());
		
		t.interrupt();
		Thread.sleep(1500);
	
		assertEquals(State.TERMINATED, t.getState());
	}
	
	/**
	 * Connects to the server with a client socket and writes a message then
	 * checks to see if the message the server received is the same as the message sent
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void clientTest() throws UnknownHostException, IOException, InterruptedException{
		final Queue<String> receivedMessages = new LinkedList<String>();
		InterceptorServer server = new InterceptorServer();
		server.setHandler(new TextHandler() {
			@Override
			public void handle(String msg) {
				receivedMessages.add(msg);
			}
		});
		
		
		Thread serverThread = new Thread(server, "Interceptor Server");
		serverThread.start();

		String msg = "Hello World";
		
		try(Socket client = new Socket("127.0.0.1", server.getPort())){ 
			OutputStream outStream = client.getOutputStream();
			PrintWriter writer = new PrintWriter(outStream);
			
			writer.println(msg);
			writer.flush();
		}
		
		Thread.sleep(1000);
		assertEquals(1, receivedMessages.size());
		assertEquals(msg, receivedMessages.poll());
	}

}
