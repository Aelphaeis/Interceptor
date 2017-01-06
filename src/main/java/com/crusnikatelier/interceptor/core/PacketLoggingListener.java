package com.crusnikatelier.interceptor.core;

import java.util.Random;

import org.pcap4j.core.PacketListener;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.EthernetPacket.EthernetHeader;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.IpV4Packet.IpV4Header;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.TcpPacket.TcpHeader;
import org.pcap4j.packet.namednumber.IpNumber;
import org.pcap4j.packet.namednumber.IpVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Used to simply forward packets to logging implementation
 * @author Aelphaeis
 *
 */
public class PacketLoggingListener implements PacketListener {
	
	private static final Logger logger = LoggerFactory.getLogger(PacketLoggingListener.class);
	
	

	@Override
	public void gotPacket(Packet packet) {
		logger.trace(packet.toString());
	}
	
	Packet buildResponse(Packet packet){
		Packet response = null;
		
		EthernetPacket ethpk = packet.get(EthernetPacket.class);
		EthernetHeader eth = ethpk.getHeader();

		EthernetPacket.Builder ethBuilder = new EthernetPacket.Builder();
		ethBuilder.dstAddr(eth.getSrcAddr());
		ethBuilder.srcAddr(eth.getDstAddr());
		ethBuilder.type(eth.getType());
		
		IpV4Packet ip4pk = packet.get(IpV4Packet.class);
		IpV4Header ip4 = ip4pk.getHeader();
		
		IpV4Packet.Builder ip4Builder = new IpV4Packet.Builder();
		ip4Builder.version(IpVersion.IPV4);
		ip4Builder.ihl((byte)5);
		
		ip4Builder.tos(ip4.getTos());
		ip4Builder.correctChecksumAtBuild(true);
		ip4Builder.correctLengthAtBuild(true);
		ip4Builder.dontFragmentFlag(true);
		
		Random r = new Random();
		short id = (short) r.nextInt(32767);
		ip4Builder.identification(id);
		ip4Builder.moreFragmentFlag(false);
		ip4Builder.fragmentOffset((short)0);
		ip4Builder.ttl((byte)200);
		ip4Builder.protocol(IpNumber.TCP);
		ip4Builder.srcAddr(ip4.getDstAddr());
		ip4Builder.dstAddr(ip4.getSrcAddr());
		
		
		TcpPacket tcppk = packet.get(TcpPacket.class);
		TcpHeader tcp = tcppk.getHeader();

		TcpPacket.Builder tcpBuilder = new TcpPacket.Builder();
		
		tcpBuilder.srcPort(tcp.getDstPort());
		tcpBuilder.dstPort(tcp.getSrcPort());
		tcpBuilder.sequenceNumber(tcp.getSequenceNumber());
		tcpBuilder.acknowledgmentNumber(Math.abs(r.nextInt()));
		
		tcpBuilder.correctLengthAtBuild(true);
		tcpBuilder.dataOffset((byte)5);
		tcpBuilder.reserved((byte)0);
		tcpBuilder.urg(false);
		tcpBuilder.ack(true);
		tcpBuilder.psh(true);
		tcpBuilder.rst(false);
		tcpBuilder.syn(false);
		tcpBuilder.fin(false);
		tcpBuilder.urgentPointer((short)0);
		
		
		ip4Builder.payloadBuilder(tcpBuilder);
		ethBuilder.payloadBuilder(ip4Builder);
		
		return response;
	}
}
