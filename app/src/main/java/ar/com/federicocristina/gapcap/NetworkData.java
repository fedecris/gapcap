package ar.com.federicocristina.gapcap;

import networkdcq.Host;
import networkdcq.NetworkApplicationData;
import networkdcq.NetworkDCQ;

public class NetworkData extends NetworkApplicationData {

    // Iniciar la grabacion
    public static final int ACTION_STOP             = 0;
    // Detener la grabacion
    public static final int ACTION_START            = 1;
    // Pedido del cliente Conectarse
    public static final int ACTION_CONNECT          = 2;
    // Pedido del cliente de Desconectarse
    public static final int ACTION_DISCONNECT       = 3;

    // Aviso del server que se cierra
    public static final int NOTIFY_SERVER_CLOSE     = 7;

    // Status OK de la actividad
    public static final int STATUS_OK               = 10;
    // Status KO de la actividad
    public static final int STATUS_KO               = 11;

    // Status inicio grabacion remota
    public static final int STATUS_RECORDING        = 20;
    // Status fin de grabacion remota
    public static final int STATUS_STOPPED          = 21;

    // Accion a realizar
    public int event = -1;


    public NetworkData(int event) {
        this.sourceHost = NetworkDCQ.getDiscovery().thisHost;
        this.event = event;
    }


}
