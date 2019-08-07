package networkdcq;

/**
 * This class must be extended in order to augment the information to send/receive 
 */

import java.io.Serializable;

public abstract class NetworkApplicationData implements Serializable {

	/** Serial version UID */
	private static final long serialVersionUID = 3198624240224703312L;
	/** Host that sends the message */
	protected Host sourceHost = null;
	
    public Host getSourceHost() {
		return sourceHost;
	}

    public Host setSourceHost(Host sourceHost) {
		return this.sourceHost = sourceHost;
	}
    
    /**
     * Subclasses related with internal messages (such a QoS test message classes) must override this method.
     */
    public boolean isQoSMessage() {
    	return false;
    }
}
