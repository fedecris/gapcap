package networkdcq.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


import networkdcq.NetworkApplicationData;
import networkdcq.NetworkDCQ;
import networkdcq.util.Logger;
import networkdcq.util.NetworkSerializable;


public class TCPNetwork extends TCPCommunication {

	/** TCP Port */
	public static final int TCP_PORT = 9999;
	
    /** Configured Host IP */
    protected String host;
    /** Configured Host port */
    protected int port;     

    /** Client socket */
    protected Socket socket;
    /** Server socket */
    protected ServerSocket serverConn;
    
    /** Buffer for writing objects */
    protected ObjectOutputStream toBuffer = null;
    /** Buffer for reading objects */
    protected ObjectInputStream fromBuffer = null;
    
    /** For writing NetworkSerializable data */
    protected OutputStream toBufferSerializable = null;
    /** For reading NetworkSerializable data */
    protected InputStream fromBufferSerializable = null;

	/**
     * Writes an object to the stream
     * @param data object to be written
     * @throws IOException in case of socket error 
     */
    public void write(NetworkApplicationData data) throws IOException {
        try {
        	// Native serialization?
        	if (NetworkDCQ.getCommunication().getSerializableData() == null) {
        		toBuffer.writeObject(data);
        		toBuffer.flush();
           		toBuffer.reset();
        	}
           	else {
           		// Multi-platform serialization?
           		toBufferSerializable.write((((NetworkSerializable)data).networkSerialize()).getBytes());
           		toBufferSerializable.flush();
           	}
        }
        catch (Exception ex) { 
        	Logger.w("Exception writing object:" + ex.getMessage());
       		throw new IOException("Socket error");
        }
    }   
    

    /**
     * Reads the next object from the stream
     * @return received data
     * @throws IOException in case of socket error
     */
    public NetworkApplicationData receive() throws IOException {
        try {
        	// Native serialization?
        	if (NetworkDCQ.getCommunication().getSerializableData() == null)
        		return (NetworkApplicationData)fromBuffer.readObject();
        	else {
        		// Multi-platform serialization?
    			int inputChar;
    			StringBuffer sb = new StringBuffer();
    		    // Read until end-of-variable-flag
    			while ((inputChar = fromBufferSerializable.read()) != NetworkSerializable.VARIABLE_END_OF_VARIABLES && inputChar != -1)
    		      sb.append((char)inputChar);
    			// Return the reconstructed instance 
    			return (NetworkApplicationData)NetworkDCQ.getCommunication().getSerializableData().networkDeserialize(sb.toString());
        	}
        }   
        catch (Exception ex) {
        	Logger.w("Exception reading object:" + ex.getMessage());
       		throw new IOException("Socket error");
        }
    }  
    

    /**
     * Closes the server socket 
     */
    public void closeServer() {
        try {       
            socket.close();
            serverConn.close();
        }
        catch (Exception ex) {
            Logger.w(ex.getMessage()); 
        }      
    }
    


    
}
