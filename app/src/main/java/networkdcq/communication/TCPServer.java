package networkdcq.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;


import networkdcq.Host;
import networkdcq.NetworkDCQ;
import networkdcq.discovery.HostDiscovery;
import networkdcq.qos.QoSMonitor;
import networkdcq.qos.QoSMonitorTestMessage;
import networkdcq.util.Logger;

public class TCPServer extends TCPListener implements Runnable {

	/**
	 * Constructor
	 */
	public TCPServer(Socket socket, ObjectInputStream fromBuffer, ObjectOutputStream toBuffer) {
		Logger.i("Creating connection to: " + socket.getInetAddress());
		this.socket = socket;
		this.fromBuffer = fromBuffer;
		this.toBuffer = toBuffer;
	}

	
	/**
	 * Constructor
	 */
	public TCPServer(Socket socket, InputStream fromBufferSerializable, OutputStream toBufferSerializable) {
		Logger.i("Creating connection to: " + socket.getInetAddress());
		this.socket = socket;
		this.fromBufferSerializable = fromBufferSerializable;
		this.toBufferSerializable = toBufferSerializable;
	}
	
	
	/**
	 * Receives and notifies incoming messages
	 */
	public synchronized void run() {
		boolean ok = true;
        while (listenerRunning && ok) {
            try {
                // Wait for incoming messages
            	data = receive();
                if (data == null)
                    continue;

                // Is this a QoS-related message?
                if (data instanceof QoSMonitorTestMessage) {
                	// Return object or calculate elapsed loop time
                	if (((QoSMonitorTestMessage)data).state == QoSMonitorTestMessage.STATE_FROM_SOURCE) {
                		((QoSMonitorTestMessage)data).state = QoSMonitorTestMessage.STATE_TO_SOURCE;
                		sendMessage(data.getSourceHost(), data);
                	}
                	else {
                		// Notify QoS
                		synchronized (QoSMonitor.LOCK) {
	                		QoSMonitor.LOCK.notify();
                		}
                	}
                	continue;
                }
                
                // Update data to be consumed
                NetworkDCQ.getCommunication().getConsumer().newData(data);
                data = null;
            }
            catch (IOException ex) {
                // Tell the app that the connection with the host is lost, or has too many errors
            	String ip = socket.getInetAddress().toString().substring(1);
            	NetworkDCQ.getCommunication().getConsumer().byeHost(new Host(ip, false));
            	HostDiscovery.removeHost(ip);
            	ok = false;
            }
            catch (Exception e) { 
            	Logger.e(e.getMessage());
            }
        }
        try {
        	socket.close();
        }
        catch (Exception e) {
        	Logger.e(e.getMessage());
        }
    }
}
