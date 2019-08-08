package networkdcq.communication;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;

import networkdcq.NetworkDCQ;
import networkdcq.util.Logger;



public class TCPListener extends TCPNetwork implements Runnable {

    ArrayList<TCPServer> tcpServers = new ArrayList<TCPServer>();

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
     * Close listeners
     */
    public void closeServer() {
        super.closeServer();
        for (TCPServer tcpServer: tcpServers) {
            try {
                if (socket != null)
                    tcpServer.socket.close();
            } catch (Exception e) {
            } finally {
                tcpServer.socket = null;
            }
        }
        tcpServers.clear();
    }
    
    /**
     * Waits for new connections and spawns a new thread each time
     */
    public boolean listen() {
        try {   
        	Logger.i("Esperando client connections...");
        	if (serverConn.isClosed())
        	    serverConn = new ServerSocket(port);
            socket = serverConn.accept();
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();
            if (NetworkDCQ.getCommunication().getSerializableData() == null) {
            	toBuffer = new ObjectOutputStream(output);
            	fromBuffer = new ObjectInputStream(input);
                TCPServer newTCP = new TCPServer(socket, fromBuffer, toBuffer);
                tcpServers.add(newTCP);
            	new Thread(newTCP).start();
            }
            else {
            	fromBufferSerializable = input;
            	toBufferSerializable = output;
                TCPServer newTCP = new TCPServer(socket, fromBufferSerializable, toBufferSerializable);
                tcpServers.add(newTCP);
            	new Thread(newTCP).start();
            }
            return true;
        }
        catch (Exception ex) { 
        	Logger.e(ex.toString());
            return false;
        }
    }  
    

}