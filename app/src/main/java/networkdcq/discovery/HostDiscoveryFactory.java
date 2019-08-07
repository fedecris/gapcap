package networkdcq.discovery;

public class HostDiscoveryFactory {
	
	/** Host discovery through UDP Sockets */
	public static final int DISCOVERY_METHOD_UDP = 1;
	/** Selected host discovery implementation instance */
	private static HostDiscovery instance = null;

	/**
	 * Default method for host discovery
	 * @return method identifier for default host discovery method
	 */
	public static int getDefaultDiscoveryMethod() {
		return DISCOVERY_METHOD_UDP;
	}
	
	/**
	 * Factory for host discovery methods
	 * @param method identifier for default host discovery method
	 * @return an instance of the discovery method selected, or null otherwise
	 */
	public static HostDiscovery getHostDiscovery(int method) {
		if (instance != null)
			return instance;
		switch (method) {
			case DISCOVERY_METHOD_UDP:
				instance = new UDPDiscovery(); 
				break;
			default:
				return null;
		}
		return instance;
	}

}
