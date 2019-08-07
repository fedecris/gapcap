package networkdcq;

/**
 * Network support main class.  Applications interested in using this framework should
 * invoke the static methods configureStartup() and doStartup() in that order.
 */

import java.io.Serializable;

import android.content.Context;

import networkdcq.communication.NetworkCommunication;
import networkdcq.communication.NetworkCommunicationFactory;
import networkdcq.discovery.HostDiscovery;
import networkdcq.discovery.HostDiscoveryFactory;
import networkdcq.qos.QoSMonitor;
import networkdcq.qos.QoSMonitorFactory;
import networkdcq.util.Logger;
import networkdcq.util.NetworkSerializable;

public class NetworkDCQ {

	/**
	 * NetworkCommunication implementation logic
	 */
	protected static NetworkCommunication networkCommunication = null;
	
	/**
	 * NetworkDicovery implementation logic
	 */
	protected static HostDiscovery networkDiscovery = null;
	
	/**
	 * QoSMonitor implementation logic
	 */
	protected static QoSMonitor qoSMonitor = null;
	
	
	/**
	 * Network main application entry point for configuration.
	 * @param consumer 
	 * 		instance in charge of updating the local model based on the received messages from other hosts
	 * @param producer
	 * 		instance in charge of setting the local information to be broadcasted periodically to the other hosts (optional)
	 * @param serializableData
	 * 		subclass of NetworkApplicationData that also implements NetworkSerializable (for multi-platform exchange) (optional)
	 * 		if this parameter is null, then default Android {@link Serializable} logic is used
	 * @param context
	 * 		Android context.  Required for QoS features only.  A null value can be passed if not needed.
	 * @return
	 * 		true of configuration was OK, or false otherwise
	 * //@throws
	 * //		Exception if an application is trying to configure an already configured startup
	 */
	public static boolean configureStartup(NetworkApplicationDataConsumer consumer, NetworkApplicationDataProducer producer, NetworkSerializable serializableData, Context context) throws Exception {
		
		try {
	    	// Discovery handler
	        networkDiscovery = getDiscovery();
	        // Communication listener
	        networkCommunication = getCommunication();
	        networkCommunication.setConsumer(consumer);
	        networkCommunication.setProducer(producer);
	        networkCommunication.setSerializableData(serializableData);
	        // QoS monitor
	        qoSMonitor = getQoS();
	        qoSMonitor.setContext(context);
	        return true;
		} 
		catch (Exception e) {
			Logger.e(e.getMessage());
			return false;
		}
        
	}
	
	/**
	 * Network main application entry point for starting host discovery and host communication logic.
	 * This method will broadcast local host status every {@code HostDiscovery.DISCOVERY_INTERVAL_MS}, and  
	 * will create the server thread in charge of receiving and establish remote connections from other hosts. 
	 * Also, will send local host application-level information every {@code NetworkCommunication.BROADCAST_LOCAL_STATUS_INTERVAL_MS}
	 * Must execute the proper configuration first!
	 *   
	 * @param startHostDicovery
	 * 		Set to true if host discovery service is needed  
	 * @param startCommunicationService
	 * 		Set to true if network communication service for receiving application-level information from other hosts is needed
	 * @param startNetworkBroadcast
	 * 		Set to true if network broadcast application-level information must be sent periodically to other hosts 
	 * 
	 * @return 
	 * 		true if startup was OK, or false otherwise
	 * //@throws
	 * //		 Exception case of error or misconfiguration
	 */
	public static boolean doStartup(boolean startHostDicovery, boolean startCommunicationService, boolean startNetworkBroadcast) throws Exception {
		
		// Was the startup correctly configured?
		if (networkCommunication==null || (networkDiscovery==null))
			throw new Exception ("NetworkDCQ not configured.  Invoke configureStartup() first.");
		if (startCommunicationService && networkCommunication.getConsumer() == null)
			throw new Exception ("Cannot start communication service without a consumer");
		if (startNetworkBroadcast && networkCommunication.getProducer() == null)
			throw new Exception ("Cannot start broadcast without a producer");
		
		try {
			if (HostDiscovery.NO_NETWORK_IP.equals(HostDiscovery.thisHost.getHostIP())) {
				Logger.w("Cannot start network services.  No network detected");
				return false;
			}
			// Communication server
			if (startCommunicationService && !networkCommunication.startService())
				throw new Exception ("Error starting network communication service");
	        
	        // Discovery service
	        if (startHostDicovery && !networkDiscovery.startDiscovery())
	        	throw new Exception ("Error starting network discovery service");
	        
	        // Communication client
	        if (startNetworkBroadcast && !networkCommunication.startBroadcast())
	        	throw new Exception ("Error starting network communication broadcast");
	        	
	        return true;
		}
		catch (Exception e) {
			Logger.e(e.getMessage());
			return false;
		}		
        
	}

	/**
	 * Shortcut for:
	 * <code>HostDiscoveryFactory.getHostDiscovery(HostDiscoveryFactory.getDefaultDiscoveryMethod())</code>
	 * @return the default discovery method instance
	 */
	public static HostDiscovery getDiscovery() {
		return HostDiscoveryFactory.getHostDiscovery(HostDiscoveryFactory.getDefaultDiscoveryMethod());
	}
	
	/**
	 * Shortcut for:
	 * <code>NetworkCommunicationFactory.getNetworkCommunication(NetworkCommunicationFactory.getDefaultNetworkCommunication())</code>
	 * @return the default network communication instance
	 */
	public static NetworkCommunication getCommunication() {
		return NetworkCommunicationFactory.getNetworkCommunication(NetworkCommunicationFactory.getDefaultNetworkCommunication());
	}

	/**
	 * Shortcut for:
	 * <code>QoSMonitorFactory.getQosMonitor(QoSMonitorFactory.getDefaultQoSMonitor())</code>
	 * @return the default QoS Monitor instance
	 */
	public static QoSMonitor getQoS() {
		return QoSMonitorFactory.getQosMonitor(QoSMonitorFactory.getDefaultQoSMonitor());
	}	
	
}

