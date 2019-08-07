package networkdcq.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


import networkdcq.Host;
import networkdcq.NetworkDCQ;
import networkdcq.util.Logger;

class UDPListener extends UDPDiscovery implements Runnable {

	/** Datagram Splitted parts */
	String[] values;
	
	/**
	 * Discovery server main loop.  Receives and processes status messages periodically.
	 */
	public void run() {
		try {
			socket = new MulticastSocket(UDP_PORT);
			group = InetAddress.getByName(UDP_GROUP);
			socket.joinGroup(group);

			while (running) {
				// Receive datagramas
				if (packet==null)
					packet = new DatagramPacket(buf, buf.length);
			    socket.receive(packet);

			    String received = new String(packet.getData());
			    managePing(received);
			}
			socket.leaveGroup(group);
			socket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Received datagramas processing.  Updates hosts list.
	 * @param received String containing IP:..:onLine(Y/N)
	 */
	protected void managePing(String received) {
		// Parse the datagram: values[0]=IP, values[1]=onLine 
	    values = received.split(DATAGRAM_FIELD_SPLIT);
	    
	    // Omit this host
	    if (thisHost.getHostIP().equals(values[0]))
	    	return;
	    
	    // Is the host already included in the list?
	    if (otherHosts.get(values[0])==null) {
	    	Host host = new Host(values[0], "Y".equals(values[1])?true:false);
	    	otherHosts.put(values[0], host);
	    	Logger.i("Agregado host:" + host.getHostIP());
	    	NetworkDCQ.getCommunication().getConsumer().newHost(host);
	    }
	    else {
	    	// Update host status
	    	otherHosts.get(values[0]).updateHostStatus("Y".equals(values[1])?true:false);
	    }
	}

}