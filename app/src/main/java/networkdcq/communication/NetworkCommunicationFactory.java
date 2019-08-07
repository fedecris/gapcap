package networkdcq.communication;

public class NetworkCommunicationFactory {
	
	/** Network communication through TCP Sockets */
	public static final int COMMUNICATION_METHOD_TCP = 1;
	/** Selected network communication implementation instance */
	protected static NetworkCommunication instance = null;
	
	/**
	 * Default method for network communication
	 * @return method identifier for default network communication method
	 */
	public static int getDefaultNetworkCommunication() {
		return COMMUNICATION_METHOD_TCP;
	}
	
	/**
	 * Factory for network communication
	 * @param method identifier for default network communication method
	 * @return an instance of the network communication method selected, or null otherwise
	 */
	public static NetworkCommunication getNetworkCommunication(int method) {
		if (instance != null)
			return instance;
		switch (method) {
			case COMMUNICATION_METHOD_TCP:
				instance = new TCPCommunication();
				break;
			default:
				return null;
		}
		return instance;
	}
}
