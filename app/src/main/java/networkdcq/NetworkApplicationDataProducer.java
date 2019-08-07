package networkdcq;

/**
 * Classes implementing this interface are in charge of keeping
 * the local information up-to-date every time the framework needs
 * an update in order to send the information to the other hosts.
 */

public interface NetworkApplicationDataProducer {

	/**
	 * Load and set the local information to be sent to other hosts
	 * @return A subclass of {@link NetworkApplicationData} with the
	 * 			updated information about the local status of the app 
	 */
	public NetworkApplicationData produceNetworkApplicationData();
}
