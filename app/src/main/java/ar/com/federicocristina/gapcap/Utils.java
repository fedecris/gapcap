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

    // Features already retrieved?
    private static boolean featuresRetrieved = false;
    // Tamaños de grabacion
    public static HashMap<Integer, List<android.hardware.Camera.Size>> cameraVideoSizes = new HashMap<Integer, List<Camera.Size>>();
    // Tamaños de grabacion
    public static HashMap<Integer, List<Integer>> videoFrameRates = new HashMap<Integer, List<Integer>>();
    // Soporte Autofocus
    public static HashMap<Integer, Boolean> autofocusModes = new HashMap<Integer, Boolean>();


    /** Retorna la fecha y hora actual */
    public static String getDateTime(String format) {
        return new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
    }

    /** Ubicacions de la grabacion */
    public static String getRecordingFileName(String filePath, String filePrefix, String dateFormat) {
        String retValue = Environment.getExternalStorageDirectory().getPath() + File.separator + filePath + File.separator +  filePrefix + Utils.getDateTime(dateFormat) + ".mp4";
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

    /** Existe una camara trasera? */
    public static boolean existsBackCamera() {
        for (String camera : getCameraList()) {
            if (Constants.BACK_CAM_NAME.equals(camera))
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

    /** Guarda las caracteristicas de cada camara en las estructuras correspondientes (por cada camara) */
    public static void retrieveCameraFeatures() {
        // Retrieve only once
        if (featuresRetrieved)
            return;
        featuresRetrieved = true;

        // == BACK CAMERA ==
        if (existsFrontalCamera()) {
            Camera cam = Camera.open();
            Camera.Parameters params = cam.getParameters();
            // Video sizes
            cameraVideoSizes.put(Camera.CameraInfo.CAMERA_FACING_BACK, cam.getParameters().getSupportedVideoSizes() != null ? cam.getParameters().getSupportedVideoSizes() : cam.getParameters().getSupportedPreviewSizes());
            // Video frame rates
            videoFrameRates.put(Camera.CameraInfo.CAMERA_FACING_BACK, params.getSupportedPreviewFrameRates());
            // Autofocus modes
            List<String> supportedFocusModes = cam.getParameters().getSupportedFocusModes();
            boolean hasAutoFocus = (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO));
            autofocusModes.put(Camera.CameraInfo.CAMERA_FACING_BACK, hasAutoFocus);
            cam.release();
        }

        // == FRONTAL CAMERA ==
        if (existsFrontalCamera()) {
            Camera cam = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            Camera.Parameters params = cam.getParameters();
            // Video sizes
            cameraVideoSizes.put(Camera.CameraInfo.CAMERA_FACING_FRONT, cam.getParameters().getSupportedVideoSizes() != null ? cam.getParameters().getSupportedVideoSizes() : cam.getParameters().getSupportedPreviewSizes());
            // Video frame rates
            videoFrameRates.put(Camera.CameraInfo.CAMERA_FACING_FRONT, params.getSupportedPreviewFrameRates());
            // Autofocus modes
            List<String> supportedFocusModes = cam.getParameters().getSupportedFocusModes();
            boolean hasAutoFocus = (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO));
            autofocusModes.put(Camera.CameraInfo.CAMERA_FACING_FRONT, hasAutoFocus);
            cam.release();
        }
    }

    /** Verifica si existe el path especificado en donde almacenar la grabacion */
    public static boolean recordingPathExists(String path) {
        try {
            File f = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + path);
            return f.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }


}


