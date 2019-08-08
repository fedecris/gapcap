package networkdcq.communication;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;


import ar.com.federicocristina.gapcap.NetworkData;
import networkdcq.Host;
import networkdcq.NetworkApplicationData;
import networkdcq.NetworkDCQ;
import networkdcq.discovery.HostDiscovery;
import networkdcq.util.Logger;

public class TCPClient extends TCPNetwork {

	/** Is this client already connected to a server? */
	protected boolean connected = false;
	
    /**
     * Constructor
     * @param ip del server al cual conectar
     */
    public TCPClient(String ip) {
        this.host = ip;
        this.port = TCP_PORT;
    }
    
    /**
     * Connects to the specified server
     * @return true if connection was successful, false otherwise
     */
    public boolean connect() {
    	Logger.i("Connecting to:" + host);
        try {
            socket = new Socket(host, port);
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();
            if (NetworkDCQ.getCommunication().getSerializableData() == null) {
            	toBuffer = new ObjectOutputStream(output);
            	fromBuffer = new ObjectInputStream(input);
            }
            else {
            	toBufferSerializable = output;
            	fromBufferSerializable = input;
            }
            connected = true;
        }
        catch (Exception ex) {
        	Logger.e(ex.getMessage());
            connected = false;
        }
        return connected;
    }  

    /** Close socket */
    public void disconnect() {
    	try {
			if (socket != null)
				socket.close();
			connected = false;
		} catch (Exception e) {

		} finally {
    		socket = null;
		}

	}

    /**
     * Prepares and send a message to the otherHosts
     * @param networkGameData message to send
     * @return true si pudo ser enviado o false en caso contrario
     */
    public void sendMessage(NetworkApplicationData networkGameData) throws Exception {
    	if (!connected) {
    		Logger.e("Cannot send message. Not connected to host:" + host);
    		return;
    	}
    	try {
			new BackgroundMessageSender().execute(networkGameData);
    	}
    	catch (Exception e) {
            // Tell the app that the connection with the host is lost
        	NetworkDCQ.getCommunication().getConsumer().byeHost(new Host(host, false));
        	HostDiscovery.removeHost(host);
        	throw e;
    	}
    }

	/**
	 * Default Getter
	 * @return true if this server is already connected to a server or false otherwise
	 */
	public boolean isConnected() {
		return connected;
	}



	private class BackgroundMessageSender extends AsyncTask<NetworkApplicationData, Void, String> {

		@Override
		protected String doInBackground(NetworkApplicationData[] params) {
			try {
				write(params[0]);
			} catch (Exception e) {
				e.printStackTrace();;
			}
			return "";
		}

		@Override
		protected void onPostExecute(String message) {
			//process message
		}
	}


}