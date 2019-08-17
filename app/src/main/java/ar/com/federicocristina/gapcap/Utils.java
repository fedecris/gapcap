package ar.com.federicocristina.gapcap;

import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Environment;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public static HashMap<Integer, Boolean> autoFocusSupport = new HashMap<Integer, Boolean>();
    // Modos de autofocus
    public static HashMap<Integer, List<String>> focusModes = new HashMap<Integer, List<String>>();
    // Soporte Flash
    public static HashMap<Integer, Boolean> flashSupport = new HashMap<Integer, Boolean>();
    //  Previous ringer mode
    static int prevRingerMode = -1;


    /** Retorna la fecha y hora actual */
    public static String getDateTime(String format) {
        return new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
    }

    /** Retorna la fecha y hora actual para un timestamp en particular */
    public static String getDateTimeFor(String format, long time) {
        return new SimpleDateFormat(format).format(time);
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
    public static void retrieveCameraFeatures(Context context) {
        // Retrieve only once
        if (featuresRetrieved)
            return;
        featuresRetrieved = true;

        // == BACK CAMERA ==
        if (existsBackCamera()) {
            Camera cam = Camera.open();
            Camera.Parameters params = cam.getParameters();
            // Video sizes
            cameraVideoSizes.put(Camera.CameraInfo.CAMERA_FACING_BACK, cam.getParameters().getSupportedVideoSizes() != null ? cam.getParameters().getSupportedVideoSizes() : cam.getParameters().getSupportedPreviewSizes());
            // Video frame rates
            videoFrameRates.put(Camera.CameraInfo.CAMERA_FACING_BACK, params.getSupportedPreviewFrameRates());
            // Autofocus modes
            List<String> supportedFocusModes = cam.getParameters().getSupportedFocusModes();
            boolean hasAutoFocus = (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO));
            autoFocusSupport.put(Camera.CameraInfo.CAMERA_FACING_BACK, hasAutoFocus);
            focusModes.put(Camera.CameraInfo.CAMERA_FACING_BACK, supportedFocusModes);
            // Flash
            flashSupport.put(Camera.CameraInfo.CAMERA_FACING_BACK, params.getSupportedFlashModes() == null || params.getSupportedFlashModes().size() == 1 ? false : true);
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
            autoFocusSupport.put(Camera.CameraInfo.CAMERA_FACING_FRONT, hasAutoFocus);
            focusModes.put(Camera.CameraInfo.CAMERA_FACING_FRONT, supportedFocusModes);
            // Flash
            flashSupport.put(Camera.CameraInfo.CAMERA_FACING_FRONT, params.getSupportedFlashModes() == null || params.getSupportedFlashModes().size() == 1 ? false : true);
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


    /** Habilitar o deshabilitar sonidos */
    public static void muteNotificationSounds(Context context, boolean mute) {
        // Recuperar el audio manager
        AudioManager audioManager = ((AudioManager)context.getSystemService(Context.AUDIO_SERVICE));

        // Si vamos a mutear, primero guardar el estado anterior
        if (mute) {
            prevRingerMode = audioManager.getRingerMode();
        }

        // Al mutear/desmutear, primero verificar si el estado anterior era mute (entonces no hacer nada)
        if (prevRingerMode == AudioManager.RINGER_MODE_SILENT)
            return;

        // Mutear o bien volver al modo original?
        int mode = (mute ? AudioManager.RINGER_MODE_SILENT : prevRingerMode);
        audioManager.setRingerMode(mode);

        // Reiniciar el prevRingerMode
        if (!mute) {
            prevRingerMode = 1;
        }
    }

    /** Retorna un set con todas las opciones del spinner */
    public static Set<String> retrieveAllItems(Spinner spinner) {
        Set<String> set = new HashSet<String>();
        for (int i = 0; i < spinner.getCount(); i++) {
            set.add(spinner.getItemAtPosition(i).toString());
        }
        return set;
    }

    public static void setAllItems(Context context, Spinner spinner, Set<String> set) {
        ArrayList<String> opciones = new ArrayList<String>();
        if (set!=null)
            opciones.addAll(set);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>( context, R.layout.spinner_item_custom, opciones);
        spinner.setAdapter(adapter);
    }


}


