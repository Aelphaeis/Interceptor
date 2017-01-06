package com.crusnikatelier.interceptor.core;

import java.io.Closeable;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PacketListener;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;

public class ClientSniffer implements Runnable, Closeable {
	
	Socket client;
	PcapHandle handle;
	
	public ClientSniffer(Socket socket){
		client = socket;
	}


	public void run(){
		if(!client.isConnected()){
			String err = "Unable to run if client socket is closed";
			throw new IllegalStateException(err);
		}
		
		try{
			init();
			PacketListener listener = new PacketLoggingListener();
			handle.loop(-1, listener);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Initializes the handle
	 * @throws Exception
	 */
	private void init() throws Exception {
		//We need to find a network device to listen from, lets pick a default
		List<PcapNetworkInterface> devices = Pcaps.findAllDevs();
		if(devices.isEmpty()){
			String err = "No Network interfaces found";
			throw new IllegalStateException(err);
		}
		PcapNetworkInterface device = devices.get(0);
		
		//We need to open device we will listen on
		int timeout = 1000;
		int snapLen = 1024;
		PcapNetworkInterface.PromiscuousMode mode = PromiscuousMode.NONPROMISCUOUS;
		handle = device.openLive(snapLen, mode, timeout);

		
		//Create and use filter
		String exp = createFilterExpression(client.getInetAddress(), client.getPort());
		Inet4Address mask = (Inet4Address) Inet4Address.getByName("0.0.0.0");
		BpfProgram filter = handle.compileFilter(exp, BpfCompileMode.OPTIMIZE, mask);
		handle.setFilter(filter);
	}
	

	@Override
	public void close() throws IOException {
		try {
			handle.breakLoop();
			handle.close();
		} 
		catch (NotOpenException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	private String createFilterExpression(InetAddress address, int port){
		String format = "(dst host %d and port %s)";
		return String.format(format, address.toString(), port);
	}
	
}
