package networkdcq.discovery;

import networkdcq.Host;
import networkdcq.util.IterateableConcurrentHashMap;
import networkdcq.util.Logger;

public abstract class HostDiscovery {

	/** Status update interval (ms) */
	public static int DISCOVERY_INTERVAL_MS = 2000;
	/** TimeOut validation interval (ms) */
	public static int DISCOVERY_TIMEOUT_CHECK_INTERVAL_MS = DISCOVERY_INTERVAL_MS * 3;
	/** TimeOut acceptance limit (ms) */
	public static int DISCOVERY_TIMEOUT_LIMIT_MS = DISCOVERY_TIMEOUT_CHECK_INTERVAL_MS;	
	/** Host local */
	public static Host thisHost = null;
	/** The other hosts list. IP->Host details */
	public static IterateableConcurrentHashMap<String, Host> otherHosts = new IterateableConcurrentHashMap<String, Host>();
	/** No network constant */
	public static String NO_NETWORK_IP = "No network detected";
	
	/**
	 * Start the discovery method for finding hosts
	 * Subclasses must periodically update otherHosts data
	 */
	public abstract boolean startDiscovery();
 
	/**
	 * Stop the discovery method for finding hosts
	 */
	public abstract void stopDiscovery();
	

	/**
	 * Set this host IP
	 */
	static {
		// Get IPv4 IP
		thisHost = Host.getLocalHostAddresAndIP();
		if (thisHost == null || thisHost.getHostIP() == null || thisHost.getHostIP().length() == 0) {
			thisHost = new Host(NO_NETWORK_IP, false); 
			Logger.w(NO_NETWORK_IP);
		}
		// Initially conected (if there is network connection)
		if (!NO_NETWORK_IP.equals(thisHost.getHostIP()))
				thisHost.setOnLine(true);
	}
	
	/**
	 * Removes a host from de remote hosts list
	 * @param ip the host IP
	 */
	public static void removeHost(String ip) {
		otherHosts.remove(ip);
	}
	
	
	@Override
	public String toString() {
		if (thisHost != null && thisHost.getHostIP() != null)
			return thisHost.getHostIP();
		return "";
	}
}
