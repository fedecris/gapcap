package ar.com.federicocristina.gapcap;

import networkdcq.Host;
import networkdcq.NetworkApplicationData;
import networkdcq.NetworkDCQ;

public class NetworkData extends NetworkApplicationData {

    // Iniciar la grabacion
    public static final int ACTION_STOP             = 0;
    // Detener la grabacion
    public static final int ACTION_START            = 1;
    // Conectarse
    public static final int ACTION_CONNECT          = 2;
    // Desconectarse
    public static final int ACTION_DISCONNECT       = 3;

    // Status OK de la actividad
    public static final int STATUS_OK               = 10;
    // Status KO de la actividad
    public static final int STATUS_KO               = 11;

    // Accion a realizar
    public int event = -1;


    public NetworkData(int event) {
        this.sourceHost = NetworkDCQ.getDiscovery().thisHost;
        this.event = event;
    }


}
