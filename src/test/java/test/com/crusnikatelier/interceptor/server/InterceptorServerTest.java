package test.com.crusnikatelier.interceptor.server;

import static org.junit.Assert.*;

import java.lang.Thread.State;

import org.junit.Test;

import com.crusnikatelier.interceptor.server.InterceptorServer;

public class InterceptorServerTest {


	/**
	 * Basic test to ensure constructor works as intended
	 */
	@Test
	public void ctorTest() {
		InterceptorServer server = new InterceptorServer();
		int port = server.getPort();
		assertTrue(port < 65535);
		assertTrue(port > 0);
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

}
