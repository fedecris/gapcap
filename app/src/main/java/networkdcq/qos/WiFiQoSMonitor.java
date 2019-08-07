package networkdcq.qos;

import networkdcq.util.Logger;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WiFiQoSMonitor extends QoSMonitor {
	

	/**
	 * Returns the WiFi network speed in Mbps
	 * @param context
	 * 		the Android context
	 * @return
	 * 		the Mbps value, or -1 in case of an error
	 */
	public int getNetworkSpeed() {
		// Retrieve wifi manager and return value if possible 
        final WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null || !wifi.isWifiEnabled() || wifi.getConnectionInfo() == null) {
        	Logger.e("Wifi is null, disabled or no Connection Info");
        	return -1;
        }
        return wifi.getConnectionInfo().getLinkSpeed();		
	}

	/**
	 * Returns the WiFi network signal strength
	 * @param context
	 * 		the Android context
	 * @return
	 * 		the strength value, raging from 0 to <code>WIFI_STRENGTH_LEVELS-1</code>, or -1 in case of an error
	 */
	public int getNetworkSignalStrength() {
		// Retrieve wifi manager 
        final WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        if(wifi == null || !wifi.isWifiEnabled()) {
        	Logger.e("WiFi is null or disabled");
        	return -1;
        }

        WifiInfo info = wifi.getConnectionInfo();   
        if (info == null) {
        	Logger.e("No Connection Info");
        	return -1;
        }
        int wifiSignalStrength = WifiManager.calculateSignalLevel(info.getRssi(), WIFI_STRENGTH_LEVELS);
        return wifiSignalStrength; 
	}
	
}
