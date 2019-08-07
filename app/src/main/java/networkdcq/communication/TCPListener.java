package networkdcq.communication;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

import networkdcq.NetworkDCQ;
import networkdcq.util.Logger;



public class TCPListener extends TCPNetwork implements Runnable {
            
    /**
     * Creates the TCP ServerSocket
     */
    public TCPListener() {
        try {
            port = TCP_PORT;
            // Subclasses should not create a new ServerSocket
            if (this.getClass().equals(TCPListener.class))
                if (serverConn == null)
            	    serverConn = new ServerSocket(port);
        }
        catch (Exception ex) { 
        	Logger.e(ex.getMessage()); 
        }
    }

    /**
     * Main loop, listens for new connection requests
     */
    public synchronized void run() {
    	while (listenerRunning) {
    		listen();
    	}
    }
    
    /**
     * Restarts sever socket
     */
    public void restartServer() {
        try {
                closeServer();
                serverConn = new ServerSocket(port);
        }
        catch (Exception e) { 
        	Logger.e(e.getMessage()); 
        }
    }
    
    /**
     * Waits for new connections and spawns a new thread each time
     */
    public boolean listen() {
        try {   
        	Logger.i("Esperando client connections...");
            socket = serverConn.accept();
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();
            if (NetworkDCQ.getCommunication().getSerializableData() == null) {
            	toBuffer = new ObjectOutputStream(output);
            	fromBuffer = new ObjectInputStream(input);
            	new Thread(new TCPServer(socket, fromBuffer, toBuffer)).start();
            }
            else {
            	fromBufferSerializable = input;
            	toBufferSerializable = output;
            	new Thread(new TCPServer(socket, fromBufferSerializable, toBufferSerializable)).start();
            }
            return true;
        }
        catch (Exception ex) { 
        	Logger.e(ex.toString());
            return false;
        }
    }  
    

}