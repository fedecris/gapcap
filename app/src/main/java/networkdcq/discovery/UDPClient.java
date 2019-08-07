package networkdcq.discovery;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import networkdcq.util.Logger;



class UDPClient extends UDPDiscovery implements Runnable {
	
	/**
	 * Discovery client main loop.  Send a status broadcast message periodically.  
	 */
	public void run() {
		
        try {
        	// Get UDP group and socket 
        	group = InetAddress.getByName(UDP_GROUP);
            socket = new MulticastSocket(UDP_PORT);
        	
        	while (running) {

        		// Send current status
	    		sendPing();

	            // Sleep for a while and resend status
                Thread.sleep(DISCOVERY_INTERVAL_MS);
	    	}
        	
        	// Close socket
			if (socket != null)
				socket.close();

	    }
	    catch (Exception e) { 
	    	Logger.e(e.getMessage()); 
	    }
	}
	
	/**
	 * Sends datagram with host information (IP-Active(Y/N))
	 * @throws Exception
	 */
	protected void sendPing() throws Exception {
		// Send current status
        buf = (thisHost.getHostIP() + DATAGRAM_FIELD_SPLIT + (thisHost.isOnLine()?"Y":"N") + DATAGRAM_FIELD_SPLIT).getBytes();
        if (packet==null)
        	packet = new DatagramPacket(buf, buf.length, group, UDP_PORT);
        packet.setData(buf);
        socket.send(packet);
	}
	
}

