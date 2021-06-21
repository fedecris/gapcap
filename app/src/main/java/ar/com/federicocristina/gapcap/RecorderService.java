package ar.com.federicocristina.gapcap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.renderscript.RenderScript;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.reflect.Array;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED;


public class RecorderService extends Service {

    // Tag Logcat
    private static final String TAG = "RecorderService";
    // Estado de grabacion
    public static boolean mRecordingStatus = false;
    // Servicio de camara
    private Camera mServiceCamera;
    // Grabacion de medios
    private MediaRecorder mMediaRecorder;
    // Surface texture for preview
    protected SurfaceTexture surfaceTexture;
    // ID de referencia
    private int id = 6789;
    // Messenger para interaccion con Activity
    Messenger messenger;

    // Delay?
    int delayStartSecs = 0;
    // Use frontal cam?
    boolean frontalCamera = false;
    // Record Audio?
    boolean recordAudio = true;
    // Low quality
    int quality = 10;
    // Video Frame rate back
    boolean customVideoFrameRateBack = false;
    // Video Frame rate back
    boolean customVideoFrameRateFront = false;
    // Video Frame Rate back
    int videoFrameRateBack = 30;
    // Video Frame Rate front
    int videoFrameRateFront = 30;
    // Custom Capture Frame rate
    boolean customCaptureFrameRate = false;
    // Capture frame rate
    int captureFrameRate = 30;
    // Limit File Size
    int limitSizeMB = 0;
    // Limit Record Time
    int limitTimeSecs = 0;
    // Vide size back
    String videoSizeBack = "640x480";
    // Vide size front
    String videoSizeFront = "640x480";
    // Focus mode back
    String focusModeBack = "Auto";
    // Focus mode front
    String focusModeFront = "Auto";
    // File path
    String filePath;
    // File prefix
    String filePrefix;
    // File date format
    String fileDateFormat;
    // Repeat at limit
    boolean repeatAtLimit;
    // Repeat at limit
    boolean swapCamAtRepeat;
    // Stealth
    boolean stealthMode;
    // Flash?
    boolean useFlash;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording(false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStart(intent, startId);
        try {
            if (intent==null)
                return START_STICKY;
            Bundle extras = intent.getExtras();
            if (extras == null) {
                return START_STICKY;
            } else {
                messenger =  (Messenger)extras.get(Constants.MESSENGER);
                try { delayStartSecs = (Integer)extras.get(Constants.PREFERENCE_DELAY_START); } catch (Exception e) { /* Ignore */ }
                try { frontalCamera = (Boolean)extras.get(Constants.PREFERENCE_FRONT_CAMERA); } catch (Exception e) { /* Ignore */ }
                try { recordAudio = (Boolean)extras.get(Constants.PREFERENCE_RECORD_AUDIO); } catch (Exception e) { /* Ignore */ }
                try { quality = (Integer)extras.get(Constants.PREFERENCE_QUALIY); } catch (Exception e) { /* Ignore */ }
                try { customVideoFrameRateBack = (Boolean)extras.get(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_BACK); } catch (Exception e) { /* Ignore */ }
                try { customVideoFrameRateFront = (Boolean)extras.get(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE_FRONT); } catch (Exception e) { /* Ignore */ }
                try { videoFrameRateBack = (Integer)extras.get(Constants.PREFERENCE_VIDEO_FRAME_RATE_BACK); } catch (Exception e) { /* Ignore */ }
                try { videoFrameRateFront = (Integer)extras.get(Constants.PREFERENCE_VIDEO_FRAME_RATE_FRONT); } catch (Exception e) { /* Ignore */ }
                try { customCaptureFrameRate = (Boolean)extras.get(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE); } catch (Exception e) { /* Ignore */ }
                try { captureFrameRate = (Integer)extras.get(Constants.PREFERENCE_CAPTURE_FRAME_RATE); } catch (Exception e) { /* Ignore */ }
                try { limitSizeMB = (Integer)extras.get(Constants.PREFERENCE_LIMIT_SIZE); } catch (Exception e) { /* Ignore */ }
                try { limitTimeSecs = (Integer)extras.get(Constants.PREFERENCE_LIMIT_TIME); } catch (Exception e) { /* Ignore */ }
                try { videoSizeBack = (String)extras.get(Constants.PREFERENCE_VIDEO_SIZE_BACK); } catch (Exception e) { /* Ignore */ }
                try { videoSizeFront = (String)extras.get(Constants.PREFERENCE_VIDEO_SIZE_FRONT); } catch (Exception e) { /* Ignore */ }
                try { focusModeBack =  (String)extras.get(Constants.PREFERENCE_FOCUS_MODE_BACK); } catch (Exception e) { /* Ignore */ }
                try { focusModeFront =  (String)extras.get(Constants.PREFERENCE_FOCUS_MODE_FRONT); } catch (Exception e) { /* Ignore */ }
                try { filePath =  (String)extras.get(Constants.PREFERENCE_FILEPATH); } catch (Exception e) { /* Ignore */ }
                try { filePrefix =  (String)extras.get(Constants.PREFERENCE_FILEPREFIX); } catch (Exception e) { /* Ignore */ }
                try { fileDateFormat =  (String)extras.get(Constants.PREFERENCE_FILETIMESTAMP); } catch (Exception e) { /* Ignore */ }
                try { repeatAtLimit = (Boolean)extras.get(Constants.PREFERENCE_REPEAT_AT_LIMIT); } catch (Exception e) { /* Ignore */ }
                try { swapCamAtRepeat = (Boolean)extras.get(Constants.PREFERENCE_SWAP_CAM_AT_REPEAT); } catch (Exception e) { /* Ignore */ }
                try { stealthMode = (Boolean)extras.get(Constants.PREFERENCE_STEALTH_MODE); } catch (Exception e) { /* Ignore */ }
                try { useFlash = (Boolean)extras.get(Constants.PREFERENCE_USE_FLASH); } catch (Exception e) { /* Ignore */ }
            }
            if (mRecordingStatus == false)
                mRecordingStatus = startRecording();
        } catch (RuntimeException e) {
            e.printStackTrace();
            notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
        } catch (Exception e2) {
            e2.printStackTrace();
            notifyEvent(Constants.NOTIFY_ERROR, e2.getMessage());
        }

        return START_STICKY;
    }

    public boolean startRecording(){
        try {
            // Si se selecciono utilizar la camara frontal
            if (frontalCamera)
                mServiceCamera = Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            else
                mServiceCamera = Camera.open();

            Camera.Parameters parameters = mServiceCamera.getParameters();
            // Forzar modo de foco solo si es soportado
            if (Utils.focusModes.get(getCurrentCamera()).contains(getSelectedFocusMode())) {
                parameters.setFocusMode(getSelectedFocusMode());
            }
            if (useFlash && Utils.flashSupport.get(getCurrentCamera())) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
            mServiceCamera.setParameters(parameters);

            surfaceTexture = new SurfaceTexture(0);
            mServiceCamera.setPreviewTexture(surfaceTexture);
            mServiceCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                                                 @Override
                                                 public void onInfo(MediaRecorder mr, int what, int extra) {
                                                     // Se llego al tiempo o tamaño
                                                     if (what == MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED || what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                                                         stopRecording(false, repeatAtLimit);
                                                     }
                                                 }
                                             });

            mMediaRecorder.setCamera(mServiceCamera);

            // AUDIO AND VIDEO SOURCE
            if (recordAudio) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // AUDIO AND VIDEO ENCODERS
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            if (recordAudio) {
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            }

            // VIDEO SIZE
            mMediaRecorder.setVideoSize(getRecordingVideoSize(0), getRecordingVideoSize(1));

            // ENCONDING QUALITY
            mMediaRecorder.setVideoEncodingBitRate(Constants.ENCODING_BITRATE_STEP * quality + 1);

            // Video frame rate
            if ((!frontalCamera && customVideoFrameRateBack) || (frontalCamera && customVideoFrameRateFront)) {
                mMediaRecorder.setVideoFrameRate(frontalCamera ? videoFrameRateFront : videoFrameRateBack);
            }

            // Capture frame rate (timelapse mode)
            if (customCaptureFrameRate) {
                mMediaRecorder.setCaptureRate(captureFrameRate);
            }

            // Max file size in bytes // Max duration in seconds
            mMediaRecorder.setMaxFileSize(limitSizeMB*(1024*1024));
            mMediaRecorder.setMaxDuration(limitTimeSecs*1000);

            // ORIENTACION DEL DISPOSITIVO
            mMediaRecorder.setOrientationHint(Utils.getRotationForPreview(getBaseContext(), frontalCamera));

            // OUTPUT FILE
            mMediaRecorder.setOutputFile(Utils.getRecordingFileName(filePath, filePrefix, fileDateFormat));

            // START RECORDING
            if (stealthMode) {
                Utils.muteNotificationSounds(getBaseContext(), true);
            }
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mRecordingStatus = true;
            notifyEvent(Constants.NOTIFY_START, null);

            // Inicio del servicio
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForeground();
            } else {
                Notification note=new Notification();
                startForeground(id, note);
            }

            return true;
        } catch (RuntimeException e2) {
            e2.printStackTrace();
            notifyEvent(Constants.NOTIFY_ERROR, e2.getMessage());
            stopRecording(true);
            return false;
         } catch (Exception e) {
            e.printStackTrace();
            notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
            stopRecording(true);
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForeground() {
        String channelId = createNotificationChannel("my_service", "My Background Service");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId );
        // NotificationCompat. notification = notificationBuilder.setOngoing(true);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setPriority(NotificationManager.IMPORTANCE_MAX);
        notificationBuilder.setCategory(Notification.CATEGORY_SERVICE);
        notificationBuilder.build();
        Notification notification = notificationBuilder.build();
        startForeground(101, notification);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    public void stopRecording(boolean withError) {
        stopRecording(withError, false);
    }

    public void stopRecording(boolean withError , boolean respawn) {
        try {
            try {
                if (mMediaRecorder!=null) {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();
                }
            } catch (Exception e) {
                e.printStackTrace();
                notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
            }

            try {
                if (mServiceCamera!=null) {
                    mServiceCamera.reconnect();
                    mServiceCamera.release();
                    mServiceCamera = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
            }

            // Si debe repetir, reiniciar nuevamente
            if (!withError && respawn) {
                // Swap camera at each restart
                if (swapCamAtRepeat && Utils.existsFrontalCamera()) {
                    frontalCamera = !frontalCamera;
                }
                startRecording();
                return;
            }

            stopForeground(true);
            mRecordingStatus = false;
            notifyEvent(Constants.NOTIFY_STOP, null);
            if (stealthMode) {
                Utils.muteNotificationSounds(getBaseContext(), false);
            }
        } catch (RuntimeException e2) {
            e2.printStackTrace();
            notifyEvent(Constants.NOTIFY_ERROR, e2.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
        }

    }

    /** Retorna el tamaño de grabacion segun la seleccion del usuario */
    protected int getRecordingVideoSize(int dimension) {
        String videoSize = (frontalCamera ? videoSizeFront : videoSizeBack);
        return Integer.parseInt(videoSize.split("x")[dimension]);
    }

    /** Retorna el modo de enfoque */
    protected String getSelectedFocusMode() {
        String focusMode = (frontalCamera ? focusModeFront : focusModeBack);
        switch (focusMode) {
            case Constants.OPTION_FOCUS_MODE_AUTO:
                return Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
            case Constants.OPTION_FOCUS_MODE_INFINITY:
                return Camera.Parameters.FOCUS_MODE_INFINITY;
            case Constants.OPTION_FOCUS_MODE_MACRO:
                return Camera.Parameters.FOCUS_MODE_MACRO;
            case Constants.OPTION_FOCUS_MODE_FIXED:
                return Camera.Parameters.FOCUS_MODE_FIXED;
            default:
                return Camera.Parameters.FOCUS_MODE_FIXED;
        }
    }


    /** Notificar una actividad en particular */
    protected void notifyEvent(int what, String content) {
        Message message = Message.obtain(null, what, content);
        message.replyTo = messenger;
        try {
            if (messenger!=null) {
                messenger.send(message);
            }
        }
        catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Retorna la constante relacionada con la camara actual */
    protected int getCurrentCamera() {
        return frontalCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;
    }


}
