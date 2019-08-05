package ar.com.federicocristina.gapcap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

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
    // Video Frame rate
    boolean customVideoFrameRate = false;
    int videoFrameRate = 30;
    // Capture Frame rate
    boolean customCaptureFrameRate = false;
    int captureFrameRate = 30;
    // Limit File Size
    int limitSizeMB = 0;
    // Limit Record Time
    int limitTimeSecs = 0;
    // Vide size
    String videoSize = "640x480";
    // Focus mode
    String focusMode = "Auto";
    // File path
    String filePath;
    // File prefix
    String filePrefix;
    // File date format
    String fileDateFormat;
    // Repeat at limit
    boolean repeatAtLimit;

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
            Bundle extras = intent.getExtras();
            if (extras == null) {
                throw new Exception("Failed to start service");
            } else {
                messenger =  (Messenger)extras.get(Constants.MESSENGER);
                delayStartSecs = (Integer)extras.get(Constants.PREFERENCE_DELAY_START);
                frontalCamera = (Boolean)extras.get(Constants.PREFERENCE_FRONT_CAMERA);
                recordAudio = (Boolean)extras.get(Constants.PREFERENCE_RECORD_AUDIO);
                quality = (Integer)extras.get(Constants.PREFERENCE_QUALIY);
                customVideoFrameRate = (Boolean)extras.get(Constants.PREFERENCE_CUSTOM_VIDEO_FRAME_RATE);
                videoFrameRate = (Integer)extras.get(Constants.PREFERENCE_VIDEO_FRAME_RATE);
                customCaptureFrameRate = (Boolean)extras.get(Constants.PREFERENCE_CUSTOM_CAPTURE_FRAME_RATE);
                captureFrameRate = (Integer)extras.get(Constants.PREFERENCE_CAPTURE_FRAME_RATE);
                limitSizeMB = (Integer)extras.get(Constants.PREFERENCE_LIMIT_SIZE);
                limitTimeSecs = (Integer)extras.get(Constants.PREFERENCE_LIMIT_TIME);
                videoSize = (String)extras.get(Constants.PREFERENCE_VIDEO_SIZE);
                focusMode =  (String)extras.get(Constants.PREFERENCE_FOCUS_MODE);
                filePath =  (String)extras.get(Constants.PREFERENCE_FILEPATH);
                filePrefix =  (String)extras.get(Constants.PREFERENCE_FILEPREFIX);
                fileDateFormat =  (String)extras.get(Constants.PREFERENCE_FILETIMESTAMP);
                repeatAtLimit = (Boolean)extras.get(Constants.PREFERENCE_REPEAT_AT_LIMIT);
            }
            if (mRecordingStatus == false)
                mRecordingStatus = startRecording();
        } catch (RuntimeException e) {
            notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
        } catch (Exception e2) {
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

            mServiceCamera.getParameters().getSupportedFocusModes();
            Camera.Parameters parameters = mServiceCamera.getParameters();
            parameters.setFocusMode(getSelectedFocusMode());
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
            if (customVideoFrameRate) {
                mMediaRecorder.setVideoFrameRate(videoFrameRate);
            }

            // Capture frame rate (timelapse mode)
            if (customCaptureFrameRate) {
                mMediaRecorder.setCaptureRate(captureFrameRate);
            }

            // Max file size in bytes // Max duration in seconds
            mMediaRecorder.setMaxFileSize(limitSizeMB*(1024*1024));
            mMediaRecorder.setMaxDuration(limitTimeSecs*1000);

            // ORIENTACION DEL DISPOSITIVO
            mMediaRecorder.setOrientationHint(Utils.getRotationForPreview(getBaseContext()));

            // OUTPUT FILE
            mMediaRecorder.setOutputFile(Utils.getRecordingFileName(filePath, filePrefix, fileDateFormat));

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            mRecordingStatus = true;
            notifyEvent(Constants.NOTIFY_START, null);

            // Inicio del servicio
            Notification note=new Notification();
            startForeground(id, note);

            return true;
        } catch (RuntimeException e2) {
            notifyEvent(Constants.NOTIFY_ERROR, e2.getMessage());
            stopRecording(true);
            return false;
         } catch (Exception e) {
            notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
            stopRecording(true);
            return false;
        }
    }

    public void stopRecording(boolean withError) {
        stopRecording(withError, false);
    }

    public void stopRecording(boolean withError , boolean respawn) {
        try {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
            } catch (Exception e) {
                notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
            }

            try {
                mServiceCamera.reconnect();
                mServiceCamera.release();
                mServiceCamera = null;
            } catch (Exception e) {
                notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
            }

            // Si debe repetir, reiniciar nuevamente
            if (!withError && respawn) {
                startRecording();
                return;
            }

            stopForeground(true);
            mRecordingStatus = false;
            notifyEvent(Constants.NOTIFY_STOP, null);
        } catch (RuntimeException e2) {
            notifyEvent(Constants.NOTIFY_ERROR, e2.getMessage());
        } catch (Exception e) {
            notifyEvent(Constants.NOTIFY_ERROR, e.getMessage());
        }

    }

    /** Retorna el tamaño de grabacion segun la seleccion del usuario */
    protected int getRecordingVideoSize(int dimension) {
        return Integer.parseInt(videoSize.split("x")[dimension]);
    }

    /** Retorna el modo de enfoque */
    protected String getSelectedFocusMode() {
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

}
