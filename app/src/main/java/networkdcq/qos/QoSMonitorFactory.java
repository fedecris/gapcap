package networkdcq.qos;


public class QoSMonitorFactory {
	
	/** Wireless monitor implementation */
	public static final int QOS_MONITOR_WIFI = 1;
	/** Selected QosMonitor implementation instance */
	private static QoSMonitor instance = null;

	/**
	 * Default QoSMonitor
	 * @return method identifier for default QoSMonitor
	 */
	public static int getDefaultQoSMonitor() {
		return QOS_MONITOR_WIFI;
	}
	
	/**
	 * Factory for QosMonitor implementations
	 * @param 
	 * 		type identifier for default QosMonitor
	 * @return 
	 * 		an instance of the selected QosMonitor implementation, or null otherwise
	 */
	public static QoSMonitor getQosMonitor(int type) {
		if (instance != null)
			return instance;
		switch (type) {
			case QOS_MONITOR_WIFI:
				instance = new WiFiQoSMonitor(); 
				break;
			default:
				return null;
		}
		return instance;
	}


}
