package networkdcq.qos;

/**
 * This class calculates/estimates how many messages can be sent to other hosts (or a specific host), 
 * considering (or not) the wireless signal strength, the network bandwith and the message object size  
 * 
 * This class considers or base its information on:
 * """"""""""""""""""""""""""""""""""""""""""""""""
 *  - The network bandwidth
 * 	- The NetworkApplicationData object size to be sent
 *  - The amount of hosts in the network
 *  - The wireless signal strength   
 *  - Historical MPS
 */

import java.util.ArrayList;

import networkdcq.Host;
import networkdcq.NetworkApplicationData;
import networkdcq.NetworkDCQ;
import networkdcq.discovery.HostDiscovery;
import networkdcq.util.Logger;
import networkdcq.util.MemoryUtils;
import android.content.Context;

public abstract class QoSMonitor {

	/** Lots of bits, bytes or whatever */
	public static final int MEBI = 1024 * 1024;
	/** Strength levels count for a wireless network */
	public static final int WIFI_STRENGTH_LEVELS = 10;
	/** Current network scan */
	public static QoSScanResult currentScan = null; 
	public static final Object LOCK = new Object();
	
	/** Application context */
	protected Context context = null;
	
	/** Collection of scan results */
	public ArrayList<QoSScanResult> scanResults = new ArrayList<QoSScanResult>();
	/** Collection of sent messages counters */
	protected ArrayList<QoSLogCounter> logCounters = new ArrayList<QoSLogCounter>();


	/**
	 * Calculates the total amount of <code>message</code> messages per second  
	 * that can be sent to the other hosts, considering bandwidth only
	 * 
	 * @param 
	 * 		message the base object message to use for calculation
	 * @param 
	 * 		targetHostQty the amount of target hosts to send the message.  The total amount of hosts
	 * 		in the network can be obtained through <code>HostDiscovery.otherHosts.size()</code>
	 * @return 
	 * 		a value equal or greater than 0, or -1 in case of an error
	 */
	public int calculateIdealMPS(NetworkApplicationData message) {
		return getMPS(message, getNetworkSpeed());
	}
	
	
	/**
	 * Estimates the total amount of <code>message</code> messages per second
	 * that can be sent to other hosts, based on a simple test
	 * 
	 * @param 
	 * 		message the base object message to use for calculation
	 * @param 
	 * 		targetHost the target host for estimation, null if all hosts must be considered
	 * @param 
	 * 		reusePreviousScan search for previous scans with similar scenario (speed/hosts) in order to avoid pawning a new scan
	 * @return 
	 * 		a value equal or greater than 0, or -1 in case of an error
	 */
	public int estimateRealMPS(NetworkApplicationData message, Host targetHost, boolean reusePreviousScan) {
		try {
			// no other hosts? nothing to do
			if (HostDiscovery.otherHosts.size() == 0)
				return -1;

			// can we use a previous scan or the network/hosts/etc changed too much?
			if (reusePreviousScan) {
				int previousScan = reusePreviousScan(getNetworkSpeed(), HostDiscovery.otherHosts.size(), 30);
				if (previousScan >= 0)
					return scanResults.get(previousScan).estimatedMPS;
			}
	
			// only one current scan at a time
			if (currentScan != null)
				return -1;
			currentScan = new QoSScanResult();
			
			// Colleccion of hosts to scan
			ArrayList<Host> targetHosts = new ArrayList<Host>();   
			if (targetHost != null)
				targetHosts.add(targetHost);
			else
				targetHosts = HostDiscovery.otherHosts.getValueList();

			// Iterate the hosts collection
			long intervalMS = 0;
			QoSMonitorTestMessage testMessage = new QoSMonitorTestMessage();
			for (Host aHost : targetHosts) {
				testMessage = new QoSMonitorTestMessage();
				testMessage.setSourceHost(HostDiscovery.thisHost);
				long start = System.currentTimeMillis();
				NetworkDCQ.getCommunication().sendMessage(aHost, testMessage);
				// Wait for answer with a timeout in order to avoid blocking this thread
				synchronized(LOCK){
				    LOCK.wait(1000);
				}
				long finish = System.currentTimeMillis();
				intervalMS += finish - start;
			}

			// Average intervals and calculate speed depending on testMessage
			intervalMS = intervalMS / targetHosts.size();
			long testMessageSizeBits = MemoryUtils.sizeOf(testMessage) * 8;
			long speedBitsPerMS = testMessageSizeBits / intervalMS; 
			float speedBPS = speedBitsPerMS * 1000;
			float speedMbps = (float)speedBPS / (float)MEBI;
			// Save the scan result, and calculate MPS based on speed and message object size
			currentScan.networkSignalStrength = getNetworkSignalStrength();
			currentScan.networkSpeedMbps = getNetworkSpeed();
			currentScan.estimatedMPS = getMPS(message, speedMbps);
			currentScan.targetHosts = targetHosts;
			scanResults.add(currentScan);
			currentScan = null;
			return getMPS(message, speedMbps);
		}
		catch (InterruptedException e) {
			Logger.e(e.toString());
			return -1;
		}
		catch (Exception e) {
			Logger.e(e.toString());
			return -1;
		}
		finally {
			currentScan = null;
		}
		
	}
	
	
	/**
	 * Retrieves the total amount of <code>message</code> messages per second
	 * that where sent to other hosts, based on the corresponding logged information
	 * 
	 * @param 
	 * 		message the base object message to use for calculation
	 * @param 
	 * 		targetHost the target host for estimation, null if all hosts must be considered
	 * @return 
	 * 		a value equal or greater than 0, or -1 in case of an error
	 */
	public int retrieveLoggedMPS(NetworkApplicationData object, Host targetHost) {
		// TODO: Implementation pending
		return 1;
	}
	
	
	/**
	 * Returns the total amount of <code>object</code> messages per second that can be sent
	 * 
	 * @return  
	 * 		a value equal or greater than 0, or -1 in case of an error
	 */
	protected int getMPS(NetworkApplicationData message, float networkSpeedMbps) {
		// Initial values
		int messageSizeBits = MemoryUtils.sizeOf(message) * 8;
		// Calulate ideal o estimate real
		return (int)(networkSpeedMbps * (float)MEBI / (float)messageSizeBits);
	}
	
	
	/**
	 * Returns the position in <code>scanResults</code> which can be 
	 * reused without having to spawn a new scan, or -1 otherwise  
	 */
	protected int reusePreviousScan(int currentNetworkSpeedMbps, int hostCount, int searchTimeLimitMinutes) {
		int retValue = -1;
		double bestDelta = 999999;
		// Search for a possible match
		double networkSpeedThreshold = currentNetworkSpeedMbps * .1;
		for (int i = scanResults.size()-1; i >= 0; i--) {
			// ommit host count changes
			if (hostCount != scanResults.get(i).targetHosts.size())
				continue;
			double networkSpeedMbpsDelta = Math.abs(currentNetworkSpeedMbps - scanResults.get(i).networkSpeedMbps);
			// ommit considerable large network speed deltas 
			if (networkSpeedMbpsDelta > networkSpeedThreshold )
				continue;
			// ommit too old results
			if (System.currentTimeMillis() - scanResults.get(i).startTime > searchTimeLimitMinutes * 60000)
				break;
			// keep best match (min delta)
			if (networkSpeedMbpsDelta < bestDelta) {
				bestDelta = networkSpeedMbpsDelta; 
				retValue = i;
			}
		}
		return retValue;
	}
	
	
	/**
	 * This method must return the established network speed in Mbps
	 * 
	 * @return
	 * 		the corresponding value in Mbps, or -1 in case of an error
	 */
	public abstract int getNetworkSpeed();
	
	/**
	 * In a wireless network, this method must return a normalized 
	 * signal strength raging from 0 to {@code WIFI_STRENGTH_LEVELS}
	 * In a wired network this method must return {@code WIFI_STRENGTH_LEVELS}
	 * 
	 * @return
	 * 		the corresponding value, or -1 in case of an error
	 */
	public abstract int getNetworkSignalStrength();

	

	public void setContext(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}
	
	public ArrayList<QoSScanResult> getScanResults() {
		return scanResults;
	}

	public ArrayList<QoSLogCounter> getLogCounters() {
		return logCounters;
	}

	


}
