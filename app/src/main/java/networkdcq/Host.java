package networkdcq;

import java.io.Serializable;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import networkdcq.util.NetworkSerializable;

public class Host implements Serializable, NetworkSerializable {

	/** Serial Version UID */
	private static final long serialVersionUID = 2807206367538864478L;
	/** Host IP */
	private String hostIP;
	/** Current status (online or not) */
	private boolean onLine;
	/** Last ping (for timeOut validation) */
	private long lastPing = System.currentTimeMillis();
    /** Last status */
    private boolean lastOnline;
	
	public String toString() {
	    return " (" + hostIP + ") - " + (onLine?"Online":"Offline");
	}

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public boolean isOnLine() {
        return onLine;
    }

    /** Set new online status and returns true if the status has changed */
    public boolean setOnLine(boolean onLine) {
        boolean changed = (onLine != this.lastOnline);
        this.lastOnline = this.onLine;
        this.onLine = onLine;
        return changed;
    }

    public boolean isLastOnLine() {
        return lastOnline;
    }

    public void setLastOnLine(boolean onLine) {
        this.lastOnline = onLine;
    }
    
    public Host(String hostIP, boolean onLine) {
        this.hostIP = hostIP;
        this.onLine = onLine;
        this.lastOnline = onLine;
    }

	public long getLastPing() {
		return lastPing;
	}

	public void setLastPing(long lastPing) {
		this.lastPing = lastPing;
	}
    
    /** 
     * Especificación de equals
     */
    public boolean equals(Object anObject) {
    	if (anObject == null)
    		return false;
    	
    	Host host = (Host)anObject;
    	if (getHostIP() == null && host.getHostIP() != null ||
    		getHostIP() != null && host.getHostIP() == null)
    	return false;
    	
    	return getHostIP().equals(((Host)host).getHostIP());
    }

    
    /**
     * Obtains this host IP
     * @return IPv4 or null otherwise
     */
    public static Host getLocalHostAddresAndIP() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        // IPv4 only
                        if (inetAddress instanceof Inet6Address)
                                continue;
                    if (!inetAddress.isLoopbackAddress()) {
                        Host thisHost = new Host(inetAddress.getHostAddress().toString(), true);
                        return thisHost;
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    

    /** Returns if new status changed online/offline */
    public boolean updateHostStatus(boolean onLine) {
        setLastPing(System.currentTimeMillis());
        return setOnLine(onLine);
    }

	@Override
	public String networkSerialize() {
		return ( hostIP + NetworkSerializable.VARIABLE_MEMBER_SEPARATOR +
				 onLine + NetworkSerializable.VARIABLE_MEMBER_SEPARATOR +		
		 		 lastPing + NetworkSerializable.VARIABLE_END_OF_VARIABLES);
	}

	@Override
	public Object networkDeserialize(String data) {
		String cadenas[] = data.split(""+NetworkSerializable.VARIABLE_MEMBER_SEPARATOR);	

		// Create and return new Host instance
		Host host = new Host(cadenas[0], cadenas[1]=="true");
		host.setLastPing(Long.parseLong(cadenas[2]));
		return host;
	}


}
