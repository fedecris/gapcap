package networkdcq.communication;

import networkdcq.Host;
import networkdcq.NetworkApplicationData;
import networkdcq.discovery.HostDiscovery;
import networkdcq.util.IterateableConcurrentHashMap;
import networkdcq.util.Logger;

public class TCPCommunication extends NetworkCommunication implements Runnable{

	/** TCP Listener */
	protected static TCPListener listener = null;
	/** TCP Clients: Target Host IP - TCPConnection */
	protected static IterateableConcurrentHashMap<String, TCPClient> clientPool = new IterateableConcurrentHashMap<String, TCPClient>();
	/** Communication listener is running */
	protected static boolean listenerRunning = false;
	/** Communication broadcast is running */
	protected static boolean broadcastRunning = false;

	private int i = 0;
	
	@Override
	public boolean startService() {

		try {
	        // Create a new listener (server)
			listenerRunning = true;
	        listener = new TCPListener();
	        Logger.i("Nuevo TCPListener");
	        // Starts the listener in a new thread
	        new Thread(listener).start();
	        return true;
		} 
		catch (Exception e) {
			Logger.e(e.getMessage());
			return false;
		}
	}

	
	@Override
	public boolean startBroadcast() {

		try {
	        // Create the client for broadcasting
			broadcastRunning = true;
			new Thread(this).start();
	        return true;
		} 
		catch (Exception e) {
			Logger.e(e.getMessage());
			return false;
		}
	}
	
	
	@Override
	public boolean stopService() {
		listenerRunning = false;
		return false;
	}
	
	@Override
	public boolean stopBroadcast() {
		broadcastRunning = false;
		return false;
	}


	@Override
	public boolean connectToServerHost(Host target) {
		if (clientPool.get(target.getHostIP()) == null) 
			clientPool.put(target.getHostIP(), new TCPClient(target.getHostIP()));

		TCPClient client = clientPool.get(target.getHostIP());
		if (client!=null && !client.connected) {
			client.connect();
			HostDiscovery.otherHosts.put(target.getHostIP(), target);
			return client.connected;
		}
		return client.connected; 
	}
	
	@Override
	public synchronized void sendMessage(Host targetHost, NetworkApplicationData data) {

		// dont send nulls
		if (data==null)
			return;
		// retrieve host IP
		String targetIP = targetHost.getHostIP();
		if (targetIP==null) {
			Logger.w("Cannot send!  targetHost IP is null");
			return;
		}
		// get client from pool
		if (clientPool.get(targetIP)==null) {
			Logger.e("Client is null");
			return;
		}
		if (!clientPool.get(targetIP).connected) {
			Logger.w("Client not connected!");
			return;
		}
		try {
			clientPool.get(targetIP).sendMessage(data);
		}
		catch (Exception e) {
			clientPool.remove(targetIP);
		}
	}

	
	@Override
	public synchronized void sendMessageToAllHosts(NetworkApplicationData data) {
		for (i=0; i<clientPool.getKeyList().size(); i++)
			if (HostDiscovery.otherHosts.get(clientPool.getKeyList().get(i))!=null && HostDiscovery.otherHosts.get(clientPool.getKeyList().get(i)).isOnLine())
				sendMessage(HostDiscovery.otherHosts.get(clientPool.getKeyList().get(i)), data);
	}


    /** 
     * In charge of sending local status to the other hosts periodically
     */
    public void run() {
    	while (broadcastRunning) {
    		if (clientPool.size() > 0)
    			sendMessageToAllHosts(producer.produceNetworkApplicationData());
        	try {
        		Thread.sleep(BROADCAST_LOCAL_STATUS_INTERVAL_MS, BROADCAST_LOCAL_STATUS_INTERVAL_NS);
        	}
        	catch (Exception e) { 
        		Logger.w(e.getMessage()); 
        	}
        }
    }


}
