package ar.com.federicocristina.gapcap;

import networkdcq.Host;
import networkdcq.NetworkApplicationData;
import networkdcq.NetworkApplicationDataConsumer;
import networkdcq.NetworkDCQ;

public class NetworkConsumer implements NetworkApplicationDataConsumer {

    MainActivity owner;

    @Override
    public void newHost(Host aHost) {
        NetworkDCQ.getCommunication().connectToServerHost(aHost);
        // Estoy en modo normal y llego un nuevo host?
        if (MainActivity.appMode == Constants.APP_MODE_NORMAL) {
            owner.reloadAvailableServers();
        }
    }

    @Override
    public void byeHost(Host aHost) {
        owner.reloadAvailableServers();
    }

    @Override
    public void newData(NetworkApplicationData receivedData) {
        NetworkData data = (NetworkData)receivedData;
        // Si estamos en modo server
        if (MainActivity.appMode == Constants.APP_MODE_SERVER) {
            // Conexion: No aceptar otro host si ya hay uno conectado
            if (data.event == NetworkData.ACTION_CONNECT && !RecorderService.mRecordingStatus && owner.remoteHost == null) {
                owner.remoteHost = data.getSourceHost();
                owner.clientConnected();
            }
            // Conexion: Liberar la referencia al server
            else if (data.event == NetworkData.ACTION_DISCONNECT && !RecorderService.mRecordingStatus) {
                owner.clientDisconnected();
                owner.remoteHost = null;
            }
            // Iniciar la grabacion disparada remotamente
            else if (data.event == NetworkData.ACTION_START && !RecorderService.mRecordingStatus) {
                NetworkData response = new NetworkData(NetworkData.STATUS_OK);
                NetworkDCQ.getCommunication().sendMessage(owner.remoteHost,  response);
                owner.iniciar();
            } // Detener la grabacion disparada remotamente
            else if (data.event == NetworkData.ACTION_STOP && RecorderService.mRecordingStatus) {
                NetworkData response = new NetworkData(NetworkData.STATUS_OK);
                NetworkDCQ.getCommunication().sendMessage(owner.remoteHost,  response);
                owner.detener();
            }
        } else if (MainActivity.appMode == Constants.APP_MODE_CLIENT) {
            if (data.event == NetworkData.STATUS_OK) {
                owner.remoteOK();
            } else if (data.event == NetworkData.STATUS_KO) {
                owner.remoteKO();
            }
        }

    }

    public NetworkConsumer(MainActivity activity) {
        owner = activity;
    }
}
