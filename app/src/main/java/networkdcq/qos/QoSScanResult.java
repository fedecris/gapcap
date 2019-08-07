package networkdcq.qos;

import java.util.ArrayList;

import networkdcq.Host;

public class QoSScanResult {

	/** Scan timestamp */
	public long startTime = System.currentTimeMillis();
	/** Network SignalStrength during this scan */
	public int networkSignalStrength = -1;
	/** Network Speed (Mbps) during this scan */
	public int networkSpeedMbps = -1;
	/** Hosts involved in this scan */
	public ArrayList<Host> targetHosts = new ArrayList<Host>(); 
	/** Final value for this scan */
	public int estimatedMPS = -1;
	
}
