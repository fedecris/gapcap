package networkdcq;


/**
 * The classes implementing this interface are in charge of processing  
 * the received messages from other hosts at the application level and   
 * update the local status based on the received information.
 * Also, the class should implement the logic for handling messages
 * sent by Discovery about new hosts.
 */

public interface NetworkApplicationDataConsumer {

	/**
	 * A new host has joined the group.  If this host is a target
	 * for receiving local status, then an invoke to  
	 * <code>networkCommunication.connectToServerHost()</code> is needed.
	 * @param aHost the new host
	 */
	public void newHost(Host aHost);
	

	/**
	 * A new host has left the group (expectedly or not).
	 * @param aHost the gone host 
	 */
	public void byeHost(Host aHost);
	
	
	/**
	 * New communication message received.
	 * @param receivedData is a subclass of {@link NetworkApplicationData} containing the received information
	 */
	public void newData(NetworkApplicationData receivedData);	
}
