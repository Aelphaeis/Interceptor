package test.com.crusnikatelier.interceptor.server;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;

import com.crusnikatelier.interceptor.server.InterceptorServer;

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
	 * Connects to the server with a client socket 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void clientTest() throws UnknownHostException, IOException, InterruptedException{
		InterceptorServer server = new InterceptorServer();
		Thread serverThread = new Thread(server, "Interceptor Server");
		serverThread.start();

		String msg = "Hello World";
		
		try(Socket client = new Socket("127.0.0.1", server.getPort())){ 
			Thread.sleep(500);
			OutputStream outStream = client.getOutputStream();
			PrintWriter writer = new PrintWriter(outStream);
			
			//we need to ensure 1 is connected.	
			assertEquals(1, server.getConnectedClientPorts().size());
			
			writer.println(msg);
			writer.flush();
		}
		serverThread.interrupt();
	}
}
