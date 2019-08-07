package networkdcq.qos;

import java.util.HashMap;

public class QoSLogCounter {

	/** Starting time */
	long startTime = System.currentTimeMillis();
	/** Host IP -> Message Count */
	HashMap<String, Integer> messagesPerHost = new HashMap<String, Integer>(); 
}
