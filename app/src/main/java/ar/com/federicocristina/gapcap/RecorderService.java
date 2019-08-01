package ar.com.federicocristina.gapcap;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.widget.Toast;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;
import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED;


public class RecorderService extends Service {

    // Tag Logcat
    private static final String TAG = "RecorderService";
    // Servicio de camara
    private static Camera mServiceCamera;
    // Grabacion de medios
    private MediaRecorder mMediaRecorder;

    private int id = 6789;

    @Override
    public void onCreate() {
        try {
            super.onCreate();
            if (MainActivity.mRecordingStatus == false)
                MainActivity.mRecordingStatus = startRecording();
        } catch (RuntimeException e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e2) {
            Toast.makeText(getBaseContext(), e2.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

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
        return START_STICKY;
    }

    public boolean startRecording(){
        try {
            // Demorar el inicio?
            int delay = Integer.parseInt(MainActivity.delayStartSecsEditText.getText().toString());
            if (delay > 0) {
                Thread.sleep(delay * 1000);
            }

            // Si se selecciono utilizar la camara frontal
            if (MainActivity.frontalCameraSwitch.isChecked())
                mServiceCamera = Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            else
                mServiceCamera = Camera.open();

            mServiceCamera.unlock();

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                                                 @Override
                                                 public void onInfo(MediaRecorder mr, int what, int extra) {
                                                     // Se llego al tiempo o tamaño
                                                     if (what == MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED || what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                                                         stopService(MainActivity.intent);
                                                     }
                                                 }
                                             });

            mMediaRecorder.setCamera(mServiceCamera);

            // AUDIO AND VIDEO SOURCE
            if (MainActivity.recordAudioSwitch.isChecked()) {
                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            }
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            // AUDIO AND VIDEO ENCODERS
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            if (MainActivity.recordAudioSwitch.isChecked()) {
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            }

            // VIDEO SIZE
            int width = getRecordingVideoSize(0);
            int height = getRecordingVideoSize(1);
            mMediaRecorder.setVideoSize(width, height);

            // ENCONDING QUALITY
            mMediaRecorder.setVideoEncodingBitRate(CamcorderProfile.get(MainActivity.lowQualitySwitch.isChecked() ? CamcorderProfile.QUALITY_LOW : CamcorderProfile.QUALITY_HIGH).videoBitRate);

            // Video frame rate
            if (MainActivity.customVideoFrameRateSwitch.isChecked()) {
                mMediaRecorder.setVideoFrameRate(Integer.parseInt(MainActivity.videoFrameRateSpinner.getSelectedItem().toString()));
            }

            // Capture frame rate (timelapse mode)
            if (MainActivity.customCaptureFrameRateSwitch.isChecked()) {
                mMediaRecorder.setCaptureRate(Integer.parseInt(MainActivity.captureFrameRateSpinner.getSelectedItem().toString()));
            }

            // Max file size in bytes // Max duration in seconds
            mMediaRecorder.setMaxFileSize(Integer.parseInt(MainActivity.limitSizeMBEditText.getText().toString())*(1024*1024));
            mMediaRecorder.setMaxDuration(Integer.parseInt(MainActivity.limitTimeSecsEditText.getText().toString())*1000);

            // ORIENTACION DEL DISPOSITIVO
            mMediaRecorder.setPreviewDisplay(MainActivity.mSurfaceHolder.getSurface());
            mMediaRecorder.setOrientationHint(Utils.getRotationForPreview(getBaseContext()));

            // OUTPUT FILE
            mMediaRecorder.setOutputFile(Utils.getRecordingFileName());

            mMediaRecorder.prepare();
            mMediaRecorder.start();

            MainActivity.mRecordingStatus = true;
            MainActivity.updateComponentsStatus(MainActivity.mRecordingStatus);

            // Notificar sobre el inicio del servicio
            Toast.makeText(getBaseContext(), R.string.ServiceStarted, Toast.LENGTH_SHORT).show();
            Notification note=new Notification();
            startForeground(id, note);

            return true;
        } catch (RuntimeException e2) {
            Toast.makeText(getBaseContext(), e2.getMessage(), Toast.LENGTH_LONG).show();
            stopRecording(true);
            return false;
         } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            stopRecording(true);
            return false;
        }
    }

    public void stopRecording(boolean withError ) {
        try {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            try {
                mServiceCamera.reconnect();
                mServiceCamera.release();
                mServiceCamera = null;
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }

            stopForeground(true);
            MainActivity.mRecordingStatus = false;
            MainActivity.updateComponentsStatus(MainActivity.mRecordingStatus);


        } catch (RuntimeException e2) {
            Toast.makeText(getBaseContext(), e2.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (!withError) {
                Toast.makeText(getBaseContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
            } else {
                stopSelf();
            }
        }

    }

    /** Retorna el tamaño de grabacion segun la seleccion del usuario */
    protected int getRecordingVideoSize(int dimension) {
        return Integer.parseInt(MainActivity.videoSizeSpinner.getSelectedItem().toString().split("x")[dimension]);
    }

}
