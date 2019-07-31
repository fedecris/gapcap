package ar.com.federicocristina.gapcap;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.view.Surface;
import android.view.WindowManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Utils {

    // Tag Logcat
    private static final String TAG = "Utils";

    /** Retorna la fecha y hora actual */
    public static String getDateTime() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    }

    /** Ubicacions de la grabacion */
    public static String getRecordingFileName() {
        String retValue = Environment.getExternalStorageDirectory().getPath() + File.separator + MainActivity.filePath.getText().toString() + File.separator +  MainActivity.filePrefix.getText().toString() + Utils.getDateTime() + ".mp4";
        // Quitar dobles slashes
        retValue = retValue.replace("//", "/");
        return retValue;
    }

    /** Brinda un detalle de cada una de las camaras */
    public static ArrayList<String> getCameraList() {
        ArrayList retValue = new ArrayList<String>();
        // Iterar por todas las camaras
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo newInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, newInfo);
            retValue.add(newInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? Constants.FRONT_CAM_NAME : Constants.BACK_CAM_NAME);
        }
        return retValue;
    }

    /** Existe una camara frontal? */
    public static boolean existsFrontalCamera() {
        for (String camera : getCameraList()) {
            if (Constants.FRONT_CAM_NAME.equals(camera))
                return true;
        }
        return false;
    }

    /** Devuelve la rotacion del dispositivo en grados, a partir de la posicion inicial (horizontal, botón inferior a la derecha) */
    public static int getRotationForPreview(Context context){
        final int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (rotation) {
            case Surface.ROTATION_0:    // El celular esta en modo portrait
                return 90;
            case Surface.ROTATION_90:   // El celular esta en modo landscape, con el boton inicio a la derecha
                return 0;
            case Surface.ROTATION_270:  // El celular esta en modo landscape, con el boton inicio a la izquierda
                return 180;
            default:
                return 270;             // El celular esta boca abajo?
        }
    }

    /** Retorna el listado de posibles tamaños de captura (por cada camara) */
    public static HashMap<Integer, List<Camera.Size>> getSupportedVideoSizes() {
        HashMap retValue = new HashMap<Integer, List<Camera.Size>>();

        Camera cam = Camera.open();
        retValue.put(Camera.CameraInfo.CAMERA_FACING_BACK, cam.getParameters().getSupportedVideoSizes()!=null ? cam.getParameters().getSupportedVideoSizes() : cam.getParameters().getSupportedPreviewSizes());
        cam.release();

        if (existsFrontalCamera()) {
            cam = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            retValue.put(Camera.CameraInfo.CAMERA_FACING_FRONT, cam.getParameters().getSupportedVideoSizes() != null ? cam.getParameters().getSupportedVideoSizes() : cam.getParameters().getSupportedPreviewSizes());
            cam.release();
        }

        return retValue;
    }

    /** Retorna los FPS disponibles (por cada camara) */
    public static HashMap<Integer, List<Integer>> getSupportedFps() {
        HashMap retValue = new HashMap<Integer, List<Integer>>();

        Camera cam = Camera.open();
        Camera.Parameters params = cam.getParameters();
        retValue.put(Camera.CameraInfo.CAMERA_FACING_BACK, params.getSupportedPreviewFrameRates());
        cam.release();

        if (existsFrontalCamera()) {
            cam = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            params = cam.getParameters();
            retValue.put(Camera.CameraInfo.CAMERA_FACING_FRONT, params.getSupportedPreviewFrameRates());
            cam.release();
        }

        return retValue;
    }


}


